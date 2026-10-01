package org.huajiager.stand.events;

import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 替身状态机每 tick 驱动：
 * 对已触发替身的玩家调用 doStandPower，驱动状态机。
 * <p>Fabric 1.20.1 用 {@link ServerTickEvents#END_SERVER_TICK}，对每个「已拥有替身
 * 且已触发（isTriggered）」的在线玩家调用 {@code getType().doStandPower(player)}，
 * 执行当前状态机的 {@code doTask}（默认态攻击/弹幕/近战等），让召唤后的替身
 * 真正产生效果——此前从未有任何 tick 驱动状态机，替身召唤后形同虚设。</p>
 * <p>与 EventStandCharge 关注点分离：本类只管「替身能力驱动」，能量回充归
 * EventStandCharge 负责。两个 END_SERVER_TICK 回调各自独立注册、顺序执行。</p>
 */
public final class EventStandPower {

	private EventStandPower() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (ServerPlayerEntity player : world.getPlayers()) {
					IExposedData data = StandUtil.getStandData(player);
					if (data == null || !data.isTriggered()) {
						continue;
					}
					StandBase stand = StandUtil.getType(player);
					if (stand != null) {
						// 替身激活期间维持 potionStand（"替身在场"标记）。
						// 此前 potionStand 仅在 MessageStandUp 召唤瞬间一次性施加
						// stand.getDuration()（KQ=300tick≈15s），之后全工程无任何续期
						// （standEffectLoad 定义后从未接线，default/punch 状态 doTask 也不
						// 施加），15s 后效果过期 → EventKillerQueen.isKillerQueenActive
						// 的 potionStand 判定恒 false → KQ 替身攻击与玩家左键均不再发放
						// "点赞"。此处每 tick 补期，效果常驻，替身在场语义保持一致。
						StandUtil.standEffectLoad(player, false);
						stand.doStandPower(player);
					}
				}
			}
		});
	}
}
