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
 * 奥义·真香蛋炒饭（食物）。
 *  setAlwaysEdible、回 7 种强化效果、吃完回复 20 点生命、发「真香!」消息并返还碗。
 */
public class ItemEggRiceU extends Item {

	public ItemEggRiceU() {
		super(new Item.Settings()
				.maxCount(64)
				.food(new FoodComponent.Builder()
						.hunger(20)
						.saturationModifier(1.0f)
						.alwaysEdible()
						.statusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 12000, 1), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 5), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 12000, 5), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 12000, 3), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 12000, 3), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 12000, 3), 1.0f)
						.statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 12000, 3), 1.0f)
						.build()));
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient() && user instanceof PlayerEntity player) {
			player.heal(20.0f);
			player.sendMessage(Text.translatable("huajiager.nice"), false);
			player.getInventory().offerOrDrop(new ItemStack(Items.BOWL));
		}
		return super.finishUsing(stack, world, user);
	}
}
