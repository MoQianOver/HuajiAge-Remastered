package org.huajiager.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * 挂者核心， 。
 *  ：头戴 ItemBlancedHelmet（五五开头盔）且未 active 时，
 * 将头盔置为 active（解除封印）、消耗核心，并发送 messege.huaji.blancedHelmet.active
 * （含玩家名）；未穿戴头盔或已激活时给出 failed 提示。
 */
public class ItemLordCore extends Item {

	public ItemLordCore() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.lord_core:tooltips.1"));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
		if (head.getItem() instanceof ItemBlancedHelmet && !ItemBlancedHelmet.isActive(head)) {
			head.getOrCreateNbt().putBoolean("active", true);
			stack.decrement(1);
			player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 1.0F, 1.0F);
			player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0F, 1.0F);
			if (world.isClient) {
				player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.active", player.getName()), false);
			}
			return TypedActionResult.success(stack);
		}
		if (world.isClient) {
			player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.failed"), false);
		}
		return TypedActionResult.pass(stack);
	}
}
