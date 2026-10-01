package org.huajiager.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/**
 * 以太粒子·伪。
 * 工具提示沿用 lang 键 item.ether:unicode_tooltips.2.desc。
 */
public class ItemEther extends Item {

	public ItemEther() {
		super(new Item.Settings().maxCount(64));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.ether:unicode_tooltips.2.desc"));
	}
}
