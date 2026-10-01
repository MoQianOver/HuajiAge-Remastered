package org.huajiager.network;

import java.util.Map;

import org.huajiager.attachment.Attachments;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.stand.StandUtil;
import org.huajiager.network.messages.MessageBlanceHelmetMode;
import org.huajiager.network.messages.MessageExglutenburMode;
import org.huajiager.network.messages.MessageFiveBulletShoot;
import org.huajiager.stand.messages.MessageDoStandCapabilityServer;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.messages.MessageLeftClickModeChange;
import org.huajiager.stand.messages.MessageLeftClickRoadRoller;
import org.huajiager.stand.messages.MessagePerfromSkill;
import org.huajiager.stand.messages.MessageStandModeSwitch;
import org.huajiager.stand.messages.MessageStandUp;
import org.huajiager.stand.messages.SyncStandChargeMessage;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 替身网络注册器。
 *
 * Fabric 1.20.1 用 FabricPacket + ServerPlayNetworking.registerGlobalReceiver 注册
 * （1.20.2+ 的 PayloadTypeRegistry/CustomPayload 在此版本不可用）。
 * 客户端向服务端发送为 client-only API，
 * 已下沉到 client source set（见 HuajiAgeRemasteredClient）。

 */
public final class StandNetWorkHandler {

	private StandNetWorkHandler() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(MessageDoStandCapabilityServer.TYPE,
				MessageDoStandCapabilityServer::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageStandUp.TYPE, MessageStandUp::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessagePerfromSkill.TYPE, MessagePerfromSkill::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageStandModeSwitch.TYPE, MessageStandModeSwitch::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageLeftClickRoadRoller.TYPE,
				MessageLeftClickRoadRoller::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageLeftClickModeChange.TYPE,
				MessageLeftClickModeChange::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageBlanceHelmetMode.TYPE,
				MessageBlanceHelmetMode::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageExglutenburMode.TYPE,
				MessageExglutenburMode::handle);
		ServerPlayNetworking.registerGlobalReceiver(MessageFiveBulletShoot.TYPE,
				MessageFiveBulletShoot::handle);

		// 玩家进入服务器时同步替身数据（STAND_DATA）与能量（STAND_HANDLER）到客户端：
		// 客户端 EventStandKey 依赖 local player 的 STAND_DATA 判定，而该 persistent
		// attachment 默认实例不会随重进自动 attach，不同步将导致重新进世界后召唤键失效。
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (player == null) {
				return;
			}
			StandUtil.syncStandData(player);
			StandHandler charge = StandUtil.getStandHandler(player);
			if (charge != null) {
				StandNetWorkHandler.sendTo(player, new SyncStandChargeMessage(
						charge.getChargeValue(), charge.getMaxValue(),
						charge.getBuffer(), charge.getBuffTag(), false));
			}
			// 玩家重进后重建替身实体：登出瞬间实体已被 tick 清理（user==null discard），
			// 仅在数据仍为触发态（isTriggered）且同维无该宿主实体时重建，避免重复 spawn。
			// 延迟一个 tick 排队，等玩家完成正式入世后 spawn。
			server.execute(() -> {
				if (player.isRemoved() || player.getServerWorld() == null) {
					return;
				}
				StandUtil.spawnStandEntityIfMissing(player);
			});
		});

		// 玩家死亡重生 / 维度传送时把替身数据复制到新玩家实体：
		// STAND_DATA / STAND_HANDLER / ENTITY_DATA 虽是 persistent attachment，
		// 但 Fabric 重生流程创建新 ServerPlayerEntity 时不会自动从旧实体转移 attachment，
		// 不复制则新玩家 STAND_DATA 回退到 initializer 默认实例（stand=empty、
		// triggered=false）→ 死亡复活后"判定无替身"（技能键无响应、无召唤提示）。
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			IExposedData oldData = oldPlayer.getAttached(Attachments.STAND_DATA);
			if (oldData != null) {
				newPlayer.setAttached(Attachments.STAND_DATA, oldData);
			}
			StandHandler oldHandler = oldPlayer.getAttached(Attachments.STAND_HANDLER);
			if (oldHandler != null) {
				newPlayer.setAttached(Attachments.STAND_HANDLER, oldHandler);
			}
			Map<String, Object> oldEntityData = oldPlayer.getAttached(Attachments.ENTITY_DATA);
			if (oldEntityData != null) {
				newPlayer.setAttached(Attachments.ENTITY_DATA, oldEntityData);
			}
		});

		// 死亡重生后同步数据到客户端并重建替身实体：死亡瞬间实体已被
		// EntityStandBase.tick 以 !user.isAlive() discard，重生后若数据仍为触发态
		// 则原地重建（spawnStandEntityIfMissing 内部查重），实现"死亡后替身不消失"。
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (newPlayer == null || newPlayer.isRemoved()) {
				return;
			}
			StandUtil.syncStandData(newPlayer);
			StandHandler charge = StandUtil.getStandHandler(newPlayer);
			if (charge != null) {
				StandNetWorkHandler.sendTo(newPlayer, new SyncStandChargeMessage(
						charge.getChargeValue(), charge.getMaxValue(),
						charge.getBuffer(), charge.getBuffTag(), false));
			}
			newPlayer.getServer().execute(() -> {
				if (newPlayer.isRemoved() || newPlayer.getServerWorld() == null) {
					return;
				}
				StandUtil.spawnStandEntityIfMissing(newPlayer);
			});
		});
	}

	public static void sendTo(ServerPlayerEntity playerMP, FabricPacket payload) {
		ServerPlayNetworking.send(playerMP, payload);
	}
}
