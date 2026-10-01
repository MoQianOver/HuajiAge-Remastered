package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionRequiem。
 */
public class PotionRequiem extends StatusEffect {
    public PotionRequiem() {
        super(StatusEffectCategory.BENEFICIAL, 0x7d0f00);
    }
}
