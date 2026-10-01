package org.huajiager.stand.events;

import org.huajiager.capability.StandHandler;
import org.huajiager.network.StandNetWorkHandler;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.messages.SyncStandChargeMessage;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 替身能量（蓄力值）随时间回充驱动。
 * <p>承接挂起任务：StandHandler 初始 charge=0，且全工程无任何 tick 调用
 * {@link StandPowerHelper#MPCharge}，导致召唤（charge.canBeCost(1000)）必报能量不足。
 * 本类注册 {@link ServerTickEvents#END_SERVER_TICK}，对每个已拥有替身的在线玩家
 * 按固定节流节奏回充 {@link StandBase#getCharge()} 点后备能量（回充量受
 * StandHandler 自身 max 钳制，满值自动停充）。</p>
 */
public final class EventStandCharge {

    /** 回充节流：每多少服务器 tick 回充一次（5 tick = 0.25s），可按平衡调整。 */
    private static final int RECHARGE_INTERVAL = 5;

    private static int tickCounter = 0;

    private EventStandCharge() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter % RECHARGE_INTERVAL != 0) {
                return;
            }
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    StandBase stand = StandUtil.getType(player);
                    if (stand == null) {
                        continue;
                    }
                    // 当前蓄力 < max 时按替身基础回充值累加；满值 MPCharge 内部直接置 max，无副作用
                    StandPowerHelper.MPCharge(player, stand.getCharge());
                    // 回充后把能量同步给客户端（客户端 STAND_HANDLER 由 SyncStandChargeMessage
                    // 接收端 getAttachedOrCreate 挂载），保证召唤判定 / 能量 HUD 与服务端一致。
                    // 只同步 charge/max（chargeOnly=true），不携带 buffer/buffTag（技能状态通道）。
                    // buffer/buffTag 为各自专属链路（时停 EventTimeStop.syncEater 等）按精确值
                    // 单播驱动；若此处复用常驻广播把 0/"" 打进包，客户端会把时停状态周期性
                    // 打回未激活，导致滤镜/齿轮/反色一闪一闪。
                    StandHandler charge = StandUtil.getStandHandler(player);
                    if (charge != null) {
                        StandNetWorkHandler.sendTo(player, new SyncStandChargeMessage(
                                charge.getChargeValue(), charge.getMaxValue(), 0, "", true));
                    }
                }
            }
        });
    }
}
