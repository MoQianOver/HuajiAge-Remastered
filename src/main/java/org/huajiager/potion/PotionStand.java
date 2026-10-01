package org.huajiager.potion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 对应 版 PotionStand（替身召唤标志药水，图标绘制留待）。
 */
public class PotionStand extends StatusEffect {
    public PotionStand() {
        super(StatusEffectCategory.NEUTRAL, 0xffffff);
    }
}
