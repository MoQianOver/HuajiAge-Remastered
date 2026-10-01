package org.huajiager.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * 普通 Disc，注册 id 为 huajiager:disc。
 *
 *  HuajiAge 中 huajiage:disc 即 ItemDiscStand 实例（替身碟），本工程已将替身碟
 * 单独注册为 disc_stand，因此这里补一个独立的普通碟，供 singularity 等配方
 * （data/huajiager/recipes/singularity.json 引用 huajiager:disc）作为合成材料使用，
 * 消除 "Unknown item 'huajiager:disc'" 的配方解析错误。
 */
public class ItemDisc extends Item {

	public ItemDisc() {
		super(new Item.Settings());
	}

	@Override
	public Text getName(ItemStack stack) {
		return Text.translatable("item.huajiager.disc.name");
	}
}
