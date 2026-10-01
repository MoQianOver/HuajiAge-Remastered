package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 希望之花药水效果。
 */
public class PotionFlowerHope extends StatusEffect {
    public PotionFlowerHope() {
        super(StatusEffectCategory.BENEFICIAL, 930000);
    }
}
