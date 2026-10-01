package org.huajiager.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * 真香蛋炒饭， （原 ItemFood）。
 *  setAlwaysEdible、回 4 种正面效果、吃完回复 10 点生命、发「真香」消息并返还碗。
 */
public class ItemEggRice extends Item {

	public ItemEggRice() {
		super(new Item.Settings()
				.maxCount(64)
				.food(new FoodComponent.Builder()
						.hunger(15)
						.saturationModifier(1.0f)
						.alwaysEdible()
						.statusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 2400, 0), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 2400, 2), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2400, 2), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 2400, 2), 1.0f)
						.build()));
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient() && user instanceof PlayerEntity player) {
			player.heal(10.0f);
			player.sendMessage(Text.translatable("message.huajiager.reo_cherry.reo"), false);
			player.getInventory().offerOrDrop(new ItemStack(Items.BOWL));
		}
		return super.finishUsing(stack, world, user);
	}
}
