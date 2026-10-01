package org.huajiager.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * 奥尔加的发型， 。
 *
 * orgasuit 套装头部件。 ItemOrgaArmorBase + ModelOrgaHair（客户端模型），
 * orgasuit 材质：150 耐久、{5,5,5,5} 保护、20 附魔、皮革音效、3 韧性。
 * 客户端模型渲染（ModelOrgaHair）与贴图（textures/models/armor/orga.png）
 * 在 HuajiAgeRemasteredClient 中以 Fabric ArmorRenderer 注册。
 * 由 ItemInfiniteCharm 的  检测 4 件套并写 NBT（orga 套装判断）。
 */
public class ItemOrgaHair extends ArmorItem {

	public static final ArmorMaterial ORGA_MATERIAL = new ArmorMaterial() {
		@Override
		public int getDurability(ArmorItem.Type type) {
			return 150;
		}

		@Override
		public int getProtection(ArmorItem.Type type) {
			return 5;
		}

		@Override
		public int getEnchantability() {
			return 20;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}

		@Override
		public String getName() {
			return "orga";
		}

		@Override
		public float getToughness() {
			return 3.0F;
		}

		@Override
		public float getKnockbackResistance() {
			return 0.0F;
		}
	};

	public ItemOrgaHair() {
		super(ORGA_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(150));
	}

	// 注意：四件套 tooltip（庆贺吧台词 + 套装列表四行，已装备行黄色高亮）统一由
	// client 源集 ItemTooltipHandlers（ItemTooltipCallback）追加——已装备高亮需要
	// 客户端玩家装备槽信息，main 源集拿不到 client player；若在此 override
	// appendTooltip 会与统一 tooltip 重复显示，故不覆写。
}
