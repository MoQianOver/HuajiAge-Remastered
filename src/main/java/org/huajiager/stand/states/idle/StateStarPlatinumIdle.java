package org.huajiager.stand.states.idle;

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
 * 白金之星待机状态（ StateStarPlatinumIdle）。
 */
public class StateStarPlatinumIdle extends StandStateBase {

    public StateStarPlatinumIdle() {
    }

    public StateStarPlatinumIdle(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    public StateStarPlatinumIdle(String stand, String stateName, boolean isHandPlay, boolean soundLoop, int stage) {
        super(stand, stateName, isHandPlay, soundLoop, stage);
    }

    @Override
    public void doTask(LivingEntity user) {
        List<StatusEffectInstance> effects = new ArrayList<>();
        StatusEffectInstance stand = user.getStatusEffect(PotionLoader.potionStand);
        if (stand != null && stand.getDuration() < 10) {
            effects.add(new StatusEffectInstance(PotionLoader.potionStand, 5 * 20));
            effects.add(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20, 5));
            if (ConfigHuaji.Stands.allowStandGlow) {
                effects.add(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20));
            }
            StandPowerHelper.potionEffect(user, effects);
        }
        StandPowerHelper.MPCharge(user, StandLoader.STAR_PLATINUM.getCharge());
    }
}
