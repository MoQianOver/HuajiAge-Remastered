package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionFiveBuff（五属性增幅，client 图标绘制留待）。
 */
public class PotionFiveBuff extends StatusEffect {
    public PotionFiveBuff() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFF00);
    }
}
