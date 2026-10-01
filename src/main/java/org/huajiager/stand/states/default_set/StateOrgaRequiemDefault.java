package org.huajiager.stand.states.default_set;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.states.StandStateBase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * 黄金体验镇魂曲默认状态（ StateOrgaRequiemDefault）。
 */
public class StateOrgaRequiemDefault extends StandStateBase {

    public StateOrgaRequiemDefault() {
    }

    public StateOrgaRequiemDefault(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
        this.addExtraData("undead");
    }

    @Override
    public void doTask(LivingEntity user) {
        StandPowerHelper.MPCharge(user, StandLoader.ORGA_REQUIEM.getCharge() / 3);
    }

    @Override
    public void doTaskOutOfTime(LivingEntity user) {
        List<StatusEffectInstance> effects = new ArrayList<>();
        effects.add(new StatusEffectInstance(PotionLoader.potionStand, 5 * 20));
        effects.add(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20, 5));
        if (ConfigHuaji.Stands.allowStandGlow) {
            effects.add(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20));
        }
        StandPowerHelper.potionEffect(user, effects);
    }
}
