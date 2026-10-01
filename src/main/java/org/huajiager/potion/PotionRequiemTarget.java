package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 镇魂曲目标标记效果。
 */
public class PotionRequiemTarget extends StatusEffect {
    public PotionRequiemTarget() {
        super(StatusEffectCategory.HARMFUL, 0xfff600);
    }
}
