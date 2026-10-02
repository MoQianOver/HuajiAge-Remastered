package org.huajiager.stand.events;

import org.huajiager.api.IStandState;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
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
						IStandState stateBase = StandStates.getStandState(data.getStand(), data.getState());
						StatusEffectInstance standEffect = player.getStatusEffect(PotionLoader.potionStand);
						// 超时判定（对齐原版 EventStand.standPotion）：替身在场标记
						// potionStand 缺失或剩余 <=5 tick 时，触发当前状态机的
						// doTaskOutOfTime（超时惩罚/闲置发光，方法内部读取
						// ConfigHuaji.Stands.allowStandPunish / allowStandGlow 决定
						// 饥饿/凋零/发光效果，并重新施加 5*20 标记）。
						// 注意：不可在此处每 tick 续期 potionStand——此前调用
						// standEffectLoad 的 "<20 续到 60" 使标记恒 >=20、永不到期，
						// doTaskOutOfTime 从未被调用，这正是 allowStandPunish /
						// allowStandGlow 调了没效果的根本原因。触发超时后标记被
						// 重新施加 5*20，下一 tick 起照常进入 doStandPower 分支。
						if (stateBase != null
								&& (standEffect == null || standEffect.getDuration() <= 5)) {
							stateBase.doTaskOutOfTime(player);
						} else {
							stand.doStandPower(player);
						}
					}
				}
			}
		});
	}
}
