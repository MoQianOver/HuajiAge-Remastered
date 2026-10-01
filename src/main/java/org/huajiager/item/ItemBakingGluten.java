package org.huajiager.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

/**
 * 烤面筋（食物）。
 *  setAlwaysEdible 且回 5 种正面效果，Fabric 侧收敛为 FoodComponent（构造器块内设置，适配 1.20.1）。
 */
public class ItemBakingGluten extends Item {

	public ItemBakingGluten() {
		super(new Item.Settings()
				.maxCount(64)
				.food(new FoodComponent.Builder()
						.hunger(10)
						.saturationModifier(1.0f)
						.alwaysEdible()
						.statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1800, 0), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1800, 2), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1800, 2), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1800, 2), 1.0f)
						.build()));
	}
}
