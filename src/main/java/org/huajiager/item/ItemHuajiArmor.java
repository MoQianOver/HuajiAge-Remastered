package org.huajiager.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * 滑稽套装 4 件， 。
 *
 *  ItemArmor + huajiArmorMaterial（36 耐久，{5,7,10,5} 保护，33 附魔，
 * 钻石装备音效，3 韧性）迁至 ArmorItem + ArmorMaterial。
 *  onArmorTick 药水效果在 Fabric 侧收敛为 inventoryTick + 已穿戴判定。
 */
public class ItemHuajiArmor extends ArmorItem {

	private static final ArmorMaterial HUAJI_MATERIAL = new ArmorMaterial() {
		@Override
		public int getDurability(ArmorItem.Type type) {
			return 36;
		}

		@Override
		public int getProtection(ArmorItem.Type type) {
			return switch (type) {
				case HELMET -> 5;
				case CHESTPLATE -> 7;
				case LEGGINGS -> 10;
				case BOOTS -> 5;
			};
		}

		@Override
		public int getEnchantability() {
			return 33;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}

		@Override
		public String getName() {
			return "huaji";
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

	private final EquipmentSlot slot;

	protected ItemHuajiArmor(ArmorItem.Type type, EquipmentSlot slot, Item.Settings settings) {
		super(HUAJI_MATERIAL, type, settings);
		this.slot = slot;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slotIndex, boolean selected) {
		super.inventoryTick(stack, world, entity, slotIndex, selected);
		if (entity instanceof PlayerEntity player && !world.isClient && player.getEquippedStack(this.slot) == stack) {
			tickEffects(player);
		}
	}

	protected void tickEffects(PlayerEntity player) {
	}

	public static class Helmet extends ItemHuajiArmor {
		public Helmet() {
			super(ArmorItem.Type.HELMET, EquipmentSlot.HEAD, new Item.Settings().maxDamage(HUAJI_MATERIAL.getDurability(ArmorItem.Type.HELMET)));
		}
	}

	public static class Chestplate extends ItemHuajiArmor {
		public Chestplate() {
			super(ArmorItem.Type.CHESTPLATE, EquipmentSlot.CHEST, new Item.Settings().maxDamage(HUAJI_MATERIAL.getDurability(ArmorItem.Type.CHESTPLATE)));
		}

		@Override
		protected void tickEffects(PlayerEntity player) {
			// 抗性提升
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0));
		}
	}

	public static class Leggings extends ItemHuajiArmor {
		public Leggings() {
			super(ArmorItem.Type.LEGGINGS, EquipmentSlot.LEGS, new Item.Settings().maxDamage(HUAJI_MATERIAL.getDurability(ArmorItem.Type.LEGGINGS)));
		}

		@Override
		protected void tickEffects(PlayerEntity player) {
			// 速度
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 0));
		}
	}

	public static class Boots extends ItemHuajiArmor {
		public Boots() {
			super(ArmorItem.Type.BOOTS, EquipmentSlot.FEET, new Item.Settings().maxDamage(HUAJI_MATERIAL.getDurability(ArmorItem.Type.BOOTS)));
		}

		@Override
		protected void tickEffects(PlayerEntity player) {
			// 跳跃提升
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 0));
		}
	}
}
