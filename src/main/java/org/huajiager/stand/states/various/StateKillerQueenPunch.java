package org.huajiager.stand.states.various;

import org.huajiager.stand.StandUtil;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;

import net.minecraft.entity.LivingEntity;

/**
 * Killer Queen 拳击态。
 * <p> 收尾：依赖的 StandPowerHelper.rangePunchAttack 已补全，本态真实启用。
 * 由 StandKillerQueen 在 doStandPower 的 punch 分支挂载调用（见 StandKillerQueen）。</p>
 */
public class StateKillerQueenPunch extends StandStateBase {

    public StateKillerQueenPunch() {
    }

    public StateKillerQueenPunch(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    @Override
    public void doTask(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        int stage = StandUtil.getStandStage(user);
        if (type != null) {
            StandPowerHelper.rangePunchAttack(user, 45, type.getDamage() * (1 + (float) (stage / 2)), 2);
        }
    }
}
