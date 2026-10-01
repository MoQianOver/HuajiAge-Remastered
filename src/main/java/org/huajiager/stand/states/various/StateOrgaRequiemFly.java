package org.huajiager.stand.states.various;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Orga Requiem 飞行态， 。
 * <p> 收尾：依赖的 StandPowerHelper.rangePunchAttack 已补全，本态真实启用。
 * 行为：范围拳击连打（按使用者 maxHealth/2 结算伤害）+ 周期性隐身。 * 超时退出（doTaskOutOfTime）施加替身标记/加速/饥饿（饥饿等级按
 * ConfigHuaji.Stands.allowStandPunish 是否开启额外惩罚档）。</p>
 */
public class StateOrgaRequiemFly extends StandStateBase {

    public StateOrgaRequiemFly() {
    }

    public StateOrgaRequiemFly(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
        this.addExtraData("undead");
        this.addExtraData("fly");
    }

    @Override
    public void doTask(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        if (type != null) {
            StandPowerHelper.rangePunchAttack(user, 45, user.getMaxHealth() / 2, 2);
        }
        StatusEffectInstance invis = user.getStatusEffect(StatusEffects.INVISIBILITY);
        if (invis == null || invis.getDuration() < 10) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 20 * 5));
        }
    }

    @Override
    public void doTaskOutOfTime(LivingEntity user) {
        user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 5 * 20));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 5 * 20, 2));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20,
                ConfigHuaji.Stands.allowStandPunish ? 24 : 49));
    }
}
