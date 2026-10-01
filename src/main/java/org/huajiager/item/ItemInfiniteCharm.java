package org.huajiager.item;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * 无限耐久护身符。
 *
 *  addPropertyOverride("orga") 由客户端 ModelPredicateProvider 注册。 * onUpdate 持续修复玩家 4 件盔甲耐久，并在穿戴 orgasuit（ConfigHuaji.orgaSuit）
 * 时写 "orga" NBT 标记。orgasuit 判定对齐 ItemInfiniteCharm：HEAD 为
 * ItemOrgaHair 且 CHEST/LEGS/FEET 为 ItemOrgaArmor 四件套。
 */
public class ItemInfiniteCharm extends Item {

	public ItemInfiniteCharm() {
		super(new Item.Settings());
	}

	public static boolean isOrga(ItemStack stack) {
		return NBTHelper.getTagCompoundSafe(stack).getBoolean("orga");
	}

	@Override
	public Text getName(ItemStack stack) {
		if (NBTHelper.getTagCompoundSafe(stack).getBoolean("orga")) {
			return Text.translatable("item.huajiage.orgaFlagUnbroken.name");
		}
		return super.getName(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			ItemStack[] slots = new ItemStack[] {
					player.getEquippedStack(EquipmentSlot.HEAD),
					player.getEquippedStack(EquipmentSlot.CHEST),
					player.getEquippedStack(EquipmentSlot.LEGS),
					player.getEquippedStack(EquipmentSlot.FEET)
			};
			for (ItemStack i : slots) {
				if (i.getMaxDamage() - i.getDamage() < i.getMaxDamage()) {
					i.setDamage(0);
				}
			}
			boolean orgaSet = ConfigHuaji.Huaji.orgaSuit
					&& player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof ItemOrgaHair
					&& player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ItemOrgaArmor
					&& player.getEquippedStack(EquipmentSlot.LEGS).getItem() instanceof ItemOrgaArmor
					&& player.getEquippedStack(EquipmentSlot.FEET).getItem() instanceof ItemOrgaArmor;
			if (orgaSet) {
				if (!isOrga(stack)) {
					NBTHelper.getTagCompoundSafe(stack).putBoolean("orga", true);
					NBTHelper.setEntityBoolean(entity, "huajiage.orga.suit", true);
				}
			} else {
				if (isOrga(stack)) {
					NBTHelper.getTagCompoundSafe(stack).putBoolean("orga", false);
					NBTHelper.setEntityBoolean(entity, "huajiage.orga.suit", false);
				}
			}
		}
	}
}
