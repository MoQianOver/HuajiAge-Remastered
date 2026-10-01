package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionFlowerHope。
 */
public class PotionFlowerHope extends StatusEffect {
    public PotionFlowerHope() {
        super(StatusEffectCategory.BENEFICIAL, 930000);
    }
}
