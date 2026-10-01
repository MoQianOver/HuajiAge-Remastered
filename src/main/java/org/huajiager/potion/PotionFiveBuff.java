package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 五属性增幅药水效果。
 */
public class PotionFiveBuff extends StatusEffect {
    public PotionFiveBuff() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFF00);
    }
}
