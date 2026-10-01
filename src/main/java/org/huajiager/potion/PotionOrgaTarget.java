package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionOrgaTarget（Orga 目标标记）。
 */
public class PotionOrgaTarget extends StatusEffect {
    public PotionOrgaTarget() {
        super(StatusEffectCategory.HARMFUL, 0x7d0f00);
    }
}
