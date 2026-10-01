package org.huajiager.stand.events;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.states.StandStateBase;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 替身飞行能力每 tick 授予/撤销（EventPlayerFlying 的
 * PlayerTickEvent，：生存模式"飞不起来"根因）。
 *
 * <p>逻辑：对在线玩家读取 STAND_DATA 的当前替身状态，若状态 extraData 含
 * "fly"（ORGA_REQUIEM 飞行态）且玩家当前无飞行能力，则授予 allowFlying 并同步
 * 能力包（sendPlayerAbilities）；若玩家此前因本机制获得飞行能力、但当前状态已
 * 不含 "fly"（退出飞行态/切换替身/召回复原），则撤销飞行能力并落地（仅限
 * 非旁观/非创造，不干扰创造模式自带飞行）。登出时清理 UUID 记录，避免泄漏。</p>
 *
 * <p>Fabric 1.20.1 映射：PlayerTickEvent → {@link ServerTickEvents#END_SERVER_TICK}
 * 遍历在线玩家；PlayerLoggedOutEvent → {@link ServerPlayConnectionEvents#DISCONNECT}。
 * 与 EventStandPower / EventStandCharge 同为独立 END_SERVER_TICK 回调。</p>
 */
public final class EventPlayerFlying {

	private static final Set<UUID> FLYING_PLAYERS = new HashSet<>();

	private EventPlayerFlying() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (ServerPlayerEntity player : world.getPlayers()) {
					boolean hasFly = false;
					IExposedData data = StandUtil.getStandData(player);
					if (data != null) {
						Object state = StandStates.getStandState(data.getStand(), data.getState());
						if (state instanceof StandStateBase stateBase) {
							hasFly = stateBase.hasExtraData("fly");
						}
					}

					// 处于 fly 态且暂无飞行能力 -> 授予
					if (!player.getAbilities().allowFlying && hasFly) {
						player.getAbilities().allowFlying = true;
						player.sendAbilitiesUpdate();
						FLYING_PLAYERS.add(player.getUuid());
					}

					// 曾被本机制授予过、当前已不在 fly 态 -> 撤销并落地（非旁观/非创造）
					if (FLYING_PLAYERS.contains(player.getUuid()) && !hasFly) {
						if (player.getAbilities().allowFlying && !player.isSpectator() && !player.isCreative()) {
							player.getAbilities().allowFlying = false;
							player.getAbilities().flying = false;
							player.sendAbilitiesUpdate();
						}
						FLYING_PLAYERS.remove(player.getUuid());
					}
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				FLYING_PLAYERS.remove(handler.player.getUuid()));
	}
}
