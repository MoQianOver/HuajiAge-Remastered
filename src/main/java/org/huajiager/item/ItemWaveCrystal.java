package org.huajiager.item;

import net.minecraft.item.Item;

/**
 * 波澜结晶（Wave Crystal），独立编写，行为对齐全版本 。
 * 为极简 Item 子类：仅设创造标签页、无自定义行为，作为波澜怒涛之刃的修复/合成材料。
 * Fabric 侧创造标签页统一在 ItemLoader 的 FabricItemGroup 中注册，故本类不再处理标签页。
 */
public class ItemWaveCrystal extends Item {

    public ItemWaveCrystal() {
        super(new Item.Settings().maxCount(64));
    }
}
