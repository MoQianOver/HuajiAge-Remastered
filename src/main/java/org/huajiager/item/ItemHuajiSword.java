package org.huajiager.item;

import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

/**
 * 滑稽剑。
 *   注册 ToolMaterial.HUAJI（采掘等级3/耐久1200/效率16.0/攻击4.0/附魔20），
 * Fabric 侧以匿名 ToolMaterial 等效实现，SwordItem 构造参数 (材料, +3攻击, 攻速-2.4, 设置)。
 */
public class ItemHuajiSword extends SwordItem {

	private static final ToolMaterial HUAJI_MATERIAL = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 1200;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return 16.0f;
		}

		@Override
		public float getAttackDamage() {
			return 4.0f;
		}

		@Override
		public int getMiningLevel() {
			return 3;
		}

		@Override
		public int getEnchantability() {
			return 20;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}
	};

	public ItemHuajiSword() {
		super(HUAJI_MATERIAL, 3, -2.4f, new Item.Settings().maxDamage(1200));
	}
}
