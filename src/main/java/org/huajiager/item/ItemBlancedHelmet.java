package org.huajiager.item;

import org.huajiager.init.loaders.PotionLoader;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 *
 * NBT 三态：active（由挂者之钥 lord_key 激活）、open（MODE_SWITCH 键切换，需 active）、
 * lord（由挂者核心 lord_core 升级）。
 * - active：最大生命 +5（MULTIPLY_BASE）
 * - active+open：攻击伤害 / 攻击速度 / 移动速度 +5（MULTIPLY_BASE）+ 药水效果
 *   （夜视 300 / 抗火 50 I / 力量 50 II / 水肺 50 / 五五开之力 potionFive 50）
 * - active+lord：护甲韧性 +15、护甲 +5（ADDITION）
 *  getAttributeModifiers(EquipmentSlot, ItemStack) 在 1.20.1 yarn 的 Item 上无
 * 等价覆盖点，改为 inventoryTick 中按穿戴态动态添加/移除修饰符（与滑稽之星剑关闭态
 * 修正同范式）。 onArmorTick 药水效果收敛为 inventoryTick + 已穿戴判定。
 */
public class ItemBlancedHelmet extends ArmorItem {

	/**  blanceMaterial（360 耐久，{15,15,15,15} 保护，33 附魔，钻石装备音效，3 韧性） */
	private static final ArmorMaterial BLANCE_MATERIAL = new ArmorMaterial() {
		@Override
		public int getDurability(ArmorItem.Type type) {
			return 360;
		}

		@Override
		public int getProtection(ArmorItem.Type type) {
			return 15;
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
			return "blance_helmet";
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

	//  getAttributeModifiers 中使用的修饰符 UUID。韧性/护甲复用同一 UUID 会
	// 互相覆盖，1.20.1 这里拆分独立 UUID 避免冲突。
	private static final UUID MAX_HEALTH_ID = UUID.fromString("ebb91868-6aed-11e9-a923-1681be663d3e");
	private static final UUID ATTACK_DAMAGE_ID = UUID.fromString("05fd4064-6aee-11e9-a923-1681be663d3e");
	private static final UUID ATTACK_SPEED_ID = UUID.fromString("1cb6e486-6aee-11e9-a923-1681be663d3e");
	private static final UUID MOVEMENT_SPEED_ID = UUID.fromString("1cb6e710-6aee-11e9-a923-1681be663d3e");
	private static final UUID ARMOR_TOUGHNESS_ID = UUID.fromString("2cb6e710-6aee-11e9-a923-1681be663d3e");
	private static final UUID ARMOR_ID = UUID.fromString("2cb6e711-6aee-11e9-a923-1681be663d3e");

	public ItemBlancedHelmet() {
		super(BLANCE_MATERIAL, ArmorItem.Type.HELMET,
				new Item.Settings().maxDamage(BLANCE_MATERIAL.getDurability(ArmorItem.Type.HELMET)));
	}

	/** 是否开启（open）： isOpen = NBT "open" 布尔 */
	public static boolean isOpen(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean("open");
	}

	/** 是否激活（active）：由挂者之钥激活 */
	public static boolean isActive(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean("active");
	}

	/** 是否挂者（lord）：由挂者核心升级 */
	public static boolean isLord(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean("lord");
	}

	/**
	 * Tooltip（主类恒定行）：状态行（active → 封印解除 / 非 active → 封印指定中）
	 * + 恒定追加 Lord.Lu 封印描述。Shift 分支（切换键提示 / 挂者核心提示 / 按住Shift提示）
	 * 因需 Screen.hasShiftDown 判断，按模板范式统一在 client 源集 ItemTooltipHandlers 追加。
	 */
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		boolean active = isActive(stack);
		tooltip.add(Text.translatable("item.blance_helmet:switch")
				.append(Text.translatable(active ? "item.blance_helmet:switch_on" : "item.blance_helmet:switch_off")));
		tooltip.add(Text.translatable("item.blance_helmet:unicode_tooltips.3.desc"));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slotIndex, boolean selected) {
		super.inventoryTick(stack, world, entity, slotIndex, selected);
		if (!(entity instanceof PlayerEntity player)) {
			return;
		}
		boolean equipped = player.getEquippedStack(EquipmentSlot.HEAD) == stack;
		// 动态属性：仅穿戴时按 NBT 三态施加，未穿戴一律移除
		if (equipped) {
			boolean active = isActive(stack);
			boolean open = isOpen(stack);
			boolean lord = isLord(stack);
			applyRemove(stack, MAX_HEALTH_ID, EntityAttributes.GENERIC_MAX_HEALTH, 5.0D,
					EntityAttributeModifier.Operation.MULTIPLY_BASE, active);
			applyRemove(stack, ATTACK_DAMAGE_ID, EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D,
					EntityAttributeModifier.Operation.MULTIPLY_BASE, active && open);
			applyRemove(stack, ATTACK_SPEED_ID, EntityAttributes.GENERIC_ATTACK_SPEED, 5.0D,
					EntityAttributeModifier.Operation.MULTIPLY_BASE, active && open);
			applyRemove(stack, MOVEMENT_SPEED_ID, EntityAttributes.GENERIC_MOVEMENT_SPEED, 5.0D,
					EntityAttributeModifier.Operation.MULTIPLY_BASE, active && open);
			applyRemove(stack, ARMOR_TOUGHNESS_ID, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 15.0D,
					EntityAttributeModifier.Operation.ADDITION, active && lord);
			applyRemove(stack, ARMOR_ID, EntityAttributes.GENERIC_ARMOR, 5.0D,
					EntityAttributeModifier.Operation.ADDITION, active && lord);
		} else {
			removeAttributeModifierByUuid(stack, MAX_HEALTH_ID);
			removeAttributeModifierByUuid(stack, ATTACK_DAMAGE_ID);
			removeAttributeModifierByUuid(stack, ATTACK_SPEED_ID);
			removeAttributeModifierByUuid(stack, MOVEMENT_SPEED_ID);
			removeAttributeModifierByUuid(stack, ARMOR_TOUGHNESS_ID);
			removeAttributeModifierByUuid(stack, ARMOR_ID);
		}
		//  onArmorTick 药水效果：open 且穿戴且服务端
		if (equipped && isOpen(stack) && !world.isClient) {
			if (!(player.hasStatusEffect(StatusEffects.NIGHT_VISION)
					&& player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration() > 250)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, false, false));
			}
			if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 50, 1, false, false));
			}
			if (!player.hasStatusEffect(StatusEffects.STRENGTH)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 50, 2, false, false));
			}
			if (!player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 50, 0, false, false));
			}
			if (!player.hasStatusEffect(PotionLoader.potionFive)) {
				player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionFive, 50, 0, false, true));
			}
		}
	}

	/**
	 *  ModeChange：未 open 且 active → 开启并发 open 消息。	 * 未 open 且非 active → 失败消息；已 open → 关闭。
	 */
	public static void modeChange(ItemStack stack, PlayerEntity player) {
		if (!isOpen(stack)) {
			if (isActive(stack)) {
				stack.getOrCreateNbt().putBoolean("open", true);
				player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.open", player.getDisplayName()), false);
			} else {
				player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.failed"), false);
			}
		} else {
			stack.getOrCreateNbt().putBoolean("open", false);
		}
	}

	private static void applyRemove(ItemStack stack, UUID uuid,
			net.minecraft.entity.attribute.EntityAttribute attribute, double amount,
			EntityAttributeModifier.Operation operation, boolean active) {
		if (active) {
			// 幂等：NBT 已含同 UUID 同数值的修饰符时不再追加，避免每 tick 重复写入
			// AttributeModifiers 造成 NBT 无限膨胀（曾膨胀到 5.6MB 导致联机进不来）。
			if (modifierMatches(stack, uuid, amount)) {
				return;
			}
			removeAttributeModifierByUuid(stack, uuid);
			stack.addAttributeModifier(attribute,
					new EntityAttributeModifier(uuid, "Blanced Helmet modifier", amount, operation),
					EquipmentSlot.HEAD);
		} else {
			removeAttributeModifierByUuid(stack, uuid);
		}
	}

	/** NBT 中是否已存在同 UUID 且数值一致的属性修饰符。1.20.1 的 AttributeModifiers
	 * UUID 由 NbtHelper.fromUuid 存为 int 数组，必须用 NbtHelper.toUuid 读取。	 * 此前用 getString("UUID") 恒读到空串，导致重复追加且永远删不掉（NBT 膨胀根因）。 */
	private static boolean modifierMatches(ItemStack stack, UUID uuid, double amount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
			return false;
		}
		NbtList modifiers = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
		for (NbtElement entry : modifiers) {
			if (!(entry instanceof NbtCompound compound)) {
				continue;
			}
			NbtElement uuidElement = compound.get("UUID");
			if (uuidElement == null) {
				continue;
			}
			UUID storedUuid = NbtHelper.toUuid(uuidElement);
			if (uuid.equals(storedUuid)
					&& Math.abs(compound.getDouble("Amount") - amount) < 1.0E-6) {
				return true;
			}
		}
		return false;
	}

	private static void removeAttributeModifierByUuid(ItemStack stack, UUID uuid) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
			return;
		}
		NbtList modifiers = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
		modifiers.removeIf(entry -> {
			if (!(entry instanceof NbtCompound compound)) {
				return false;
			}
			NbtElement uuidElement = compound.get("UUID");
			if (uuidElement == null) {
				return false;
			}
			return uuid.equals(NbtHelper.toUuid(uuidElement));
		});
		if (modifiers.isEmpty()) {
			nbt.remove("AttributeModifiers");
		}
	}
}
