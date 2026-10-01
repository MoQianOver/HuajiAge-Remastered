package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 替身召唤标志药水效果。
 */
public class PotionStand extends StatusEffect {
    public PotionStand() {
        super(StatusEffectCategory.NEUTRAL, 0xffffff);
    }
}
