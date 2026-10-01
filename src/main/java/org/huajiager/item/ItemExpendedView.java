package org.huajiager.item;

import net.minecraft.item.Item;

/**
 * 虚空镜片（expended view）， 。
 * 为普通物品，被第二卷轴等机制作为击杀掉落物产出。
 */
public class ItemExpendedView extends Item {

	public ItemExpendedView() {
		super(new Item.Settings().maxCount(64));
	}
}
