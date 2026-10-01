package org.huajiager.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/**
 * 以太中子环流板。
 * 工具提示沿用 lang 键 item.ether_circumflux_board:unicode_tooltips.1.desc。
 */
public class ItemEtherCircumfluxBoard extends Item {

	public ItemEtherCircumfluxBoard() {
		super(new Item.Settings().maxCount(64));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.ether_circumflux_board:unicode_tooltips.1.desc"));
	}
}
