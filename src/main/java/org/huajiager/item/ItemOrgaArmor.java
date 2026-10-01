package org.huajiager.item;

import org.huajiager.capability.IExposedData;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 奥尔加四件套（头盔/胸甲/护腿/靴子）， 。
 *
 * 材质复用 {@link ItemOrgaHair#ORGA_MATERIAL}（orgasuit：150 耐久、5 保护、20 附魔、
 * 皮革音效、3 韧性），与 orga_hair 发型同一套护甲渲染管线（orga_layer_1/2.png）。
 *
 * 核心判定 {@link #hasAllOrgaArmor(LivingEntity)} 对齐两条满足其一：
 *   1) 实体替身为 StandOrgaRequiem。 *   2) 四个装备槽（HEAD/CHEST/LEGS/FEET）均为本类四件套。
 */
public class ItemOrgaArmor extends ArmorItem {

	private final EquipmentSlot slot;

	protected ItemOrgaArmor(ArmorItem.Type type, EquipmentSlot slot, Item.Settings settings) {
		super(ItemOrgaHair.ORGA_MATERIAL, type, settings);
		this.slot = slot;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slotIndex, boolean selected) {
		super.inventoryTick(stack, world, entity, slotIndex, selected);
		if (entity instanceof PlayerEntity player && !world.isClient
				&& player.getEquippedStack(this.slot) == stack) {
			tickEffects(player);
		}
	}

	protected void tickEffects(PlayerEntity player) {
	}

	/**  hasAllOrgaArmor：替身是 StandOrgaRequiem，或四装备槽均为 ORGA 四件套 */
	public static boolean hasAllOrgaArmor(LivingEntity living) {
		if (living == null) {
			return false;
		}
		StandBase stand = StandUtil.getType(living);
		if (stand != null && StandLoader.ORGA_REQUIEM.getName().equals(stand.getName())) {
			return true;
		}
		ItemStack head = living.getEquippedStack(EquipmentSlot.HEAD);
		ItemStack chest = living.getEquippedStack(EquipmentSlot.CHEST);
		ItemStack legs = living.getEquippedStack(EquipmentSlot.LEGS);
		ItemStack feet = living.getEquippedStack(EquipmentSlot.FEET);
		return head.getItem() instanceof ItemOrgaHair
				&& chest.getItem() instanceof Chestplate
				&& legs.getItem() instanceof Leggings
				&& feet.getItem() instanceof Boots;
	}

	/** 替身 ORGA_REQUIEM 已召唤触发 */
	public static boolean isStandTriggered(LivingEntity living) {
		IExposedData data = StandUtil.getStandData(living);
		if (data == null || !data.isTriggered()) {
			return false;
		}
		return StandLoader.ORGA_REQUIEM.getName().equals(data.getStand());
	}

	public static class Helmet extends ItemOrgaArmor {
		public Helmet() {
			super(ArmorItem.Type.HELMET, EquipmentSlot.HEAD,
					new Item.Settings().maxDamage(ItemOrgaHair.ORGA_MATERIAL.getDurability(ArmorItem.Type.HELMET)));
		}
	}

	public static class Chestplate extends ItemOrgaArmor {
		public Chestplate() {
			super(ArmorItem.Type.CHESTPLATE, EquipmentSlot.CHEST,
					new Item.Settings().maxDamage(ItemOrgaHair.ORGA_MATERIAL.getDurability(ArmorItem.Type.CHESTPLATE)));
		}
	}

	public static class Leggings extends ItemOrgaArmor {
		public Leggings() {
			super(ArmorItem.Type.LEGGINGS, EquipmentSlot.LEGS,
					new Item.Settings().maxDamage(ItemOrgaHair.ORGA_MATERIAL.getDurability(ArmorItem.Type.LEGGINGS)));
		}
	}

	public static class Boots extends ItemOrgaArmor {
		public Boots() {
			super(ArmorItem.Type.BOOTS, EquipmentSlot.FEET,
					new Item.Settings().maxDamage(ItemOrgaHair.ORGA_MATERIAL.getDurability(ArmorItem.Type.BOOTS)));
		}
	}
}
