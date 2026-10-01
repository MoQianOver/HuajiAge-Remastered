package org.huajiager.item;

import org.huajiager.util.NBTHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * 多重叠加态滑稽之星。
 * 工具提示读取 NBT poly 叠加数值（Fabric 侧用后的 NBTHelper.getTagCompoundSafe）。
 */
public class ItemHuajiStarPoly extends Item {

	public ItemHuajiStarPoly() {
		super(new Item.Settings().maxCount(64));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("message.huajiager.poly_huaji")
				.append(Text.literal("" + NBTHelper.getTagCompoundSafe(stack).getInt("poly")).formatted(Formatting.WHITE)));
	}
}
