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
 * 世界待机状态（ StateTheWorldIdle）。
 */
public class StateTheWorldIdle extends StandStateBase {

    public StateTheWorldIdle() {
    }

    public StateTheWorldIdle(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    @Override
    public void doTask(LivingEntity user) {
        StandPowerHelper.MPCharge(user, StandLoader.THE_WORLD.getCharge());
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
