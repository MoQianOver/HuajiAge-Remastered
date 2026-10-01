package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionOverdrive。
 */
public class PotionOverdrive extends StatusEffect {
    public PotionOverdrive() {
        super(StatusEffectCategory.BENEFICIAL, 0x7d0f00);
    }
}
