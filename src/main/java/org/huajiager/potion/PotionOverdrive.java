package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * OVERDRIVE 爆发效果。
 */
public class PotionOverdrive extends StatusEffect {
    public PotionOverdrive() {
        super(StatusEffectCategory.BENEFICIAL, 0x7d0f00);
    }
}
