package org.huajiager.screen;

import org.huajiager.util.HuajiUtils;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * 燃料槽：仅接受带指定 mod tag 前缀（如 huaji_blender/time_100）的物品。
 */
public class HuajiFuelSlot extends Slot {

	private final String tagPrefix;
	private final int tagIndex;

	public HuajiFuelSlot(Inventory inventory, int index, int x, int y, String tagPrefix, int tagIndex) {
		super(inventory, index, x, y);
		this.tagPrefix = tagPrefix;
		this.tagIndex = tagIndex;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return HuajiUtils.isTagFuel(stack, tagPrefix, tagIndex);
	}
}
