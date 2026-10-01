package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Orga 目标标记效果。
 */
public class PotionOrgaTarget extends StatusEffect {
    public PotionOrgaTarget() {
        super(StatusEffectCategory.HARMFUL, 0x7d0f00);
    }
}
