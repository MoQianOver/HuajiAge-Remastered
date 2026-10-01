package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionRequiemTarget（镇魂曲目标标记）。
 */
public class PotionRequiemTarget extends StatusEffect {
    public PotionRequiemTarget() {
        super(StatusEffectCategory.HARMFUL, 0xfff600);
    }
}
