package org.huajiager.item;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.sound.SoundLoader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * EX面筋棒。
 *
 * NBT flavor(0~3)：默认 0（无风味），1=香香 / 2=辣辣 / 3=石灰。
 * - 潜行右键循环切风味（服务端改 NBT），客户端播音效+风味提示
 * - 潜行滚轮前后切换风味：复用网络消息 MessageExglutenburMode，客户端本地音效提示
 *   对齐 onMouseDwheelInput，并消费滚动不转发（对齐 evt.setCanceled(true)，
 *   避免潜行切风味时误切物品栏）
 * - 命中按 flavor 施效：1 饥饿+击飞+回饱食；2 点燃+2格AOE溅射；3 虚弱+缓慢+石灰粒子
 *   +30%概率可带劲啦群伤50并拉拽5格实体
 * - inventoryTick buff：flavor2 抗火/力量/再生；flavor3 凋零/速度/石灰粒子
 *  getAttributeModifiers 动态伤害/攻速在 1.20.1 yarn 无等价覆盖点，改为
 * inventoryTick 动态修饰符（同模板滑稽之星剑范式）：覆写 getAttributeModifiers(slot)
 * 清掉 SwordItem 固定默认修饰符，按 flavor 写"总值"：攻速 flavor1 -1.7、其余 -2.4。 * 伤害 flavor1=22、flavor2=8、flavor3=17、flavor0 不写。客户端空挥 flavor2 播音效由
 * EventExglutenburLeftClick 承接。
 */
public class ItemExglutenbur extends SwordItem {

	/**  Gluten 材料（3 挖掘等级，5400 耐久，25 速度，17 攻击，30 附魔，修复=烤面筋） */
	private static final ToolMaterial GLUTEN_MATERIAL = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 5400;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return 25.0F;
		}

		@Override
		public float getAttackDamage() {
			return 17.0F;
		}

		@Override
		public int getMiningLevel() {
			return 3;
		}

		@Override
		public int getEnchantability() {
			return 30;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ItemLoader.bakingGluten);
		}
	};

	private static final UUID FLAVOR_ATTACK_SPEED_ID = UUID.fromString("4e62e8c0-7d14-4b8e-9a52-3f6b8e1c2d11");
	private static final UUID FLAVOR_ATTACK_DAMAGE_ID = UUID.fromString("4e62e8c0-7d14-4b8e-9a52-3f6b8e1c2d12");

	public ItemExglutenbur() {
		super(GLUTEN_MATERIAL, 0, -2.4F, new Item.Settings().maxDamage(5400));
	}

	/** 对齐 getAttributeModifiers(MAINHAND, stack) 语义：攻击伤害修饰符直接写"总伤害"、
	 * 攻速直接写"总攻速"（flavor1 -1.7，其余 -2.4），flavor0 不写攻击伤害修饰符。
	 * 1.20.1 无带 ItemStack 的等价覆盖点，因此覆写本方法清掉 SwordItem 固定默认修饰符，
	 * 避免 tooltip 叠加显示"基础 17 + 差值"（如 -9 攻击伤害、空白的 +0）。 */
	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
		return HashMultimap.create();
	}

	/** 当前风味：NBT "flavor"（0~3） */
	public static int flavor(ItemStack stack) {
		return stack.getOrCreateNbt().getInt("flavor");
	}

	public static void setFlavor(ItemStack stack, int flavor) {
		stack.getOrCreateNbt().putInt("flavor", flavor & 3);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		World world = target.getWorld();
		switch (flavor(stack)) {
			case 1 -> {
				// 饥饿 + 击飞 + 回饱食
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 3));
				if (attacker instanceof PlayerEntity player) {
					//  getFoodStats().addStats(1, 10f)：+1 饥饿并大幅回饱食
					player.getHungerManager().setFoodLevel(Math.min(20, player.getHungerManager().getFoodLevel() + 1));
					player.getHungerManager().setSaturationLevel(Math.min(
							player.getHungerManager().getFoodLevel(),
							player.getHungerManager().getSaturationLevel() + 20.0F));
				}
				target.setVelocity(target.getVelocity().x, 1.0D, target.getVelocity().z);
				stack.damage(8, attacker, e -> {
				});
			}
			case 2 -> {
				// 点燃 + 2格AOE溅射
				target.setOnFireFor(5);
				stack.damage(1, attacker, e -> {
				});
				target.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0F, 1.0F);
				List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class,
						target.getBoundingBox().expand(2.0D),
						entity -> entity != attacker && entity != target);
				for (LivingEntity entity : entities) {
					entity.damage(entity.getDamageSources().inFire(), 5.0F);
					entity.setOnFireFor(3);
				}
				world.syncWorldEvent(2004, target.getBlockPos(), 0xFF4500);
			}
			case 3 -> {
				// 虚弱 + 缓慢 + 石灰粒子 + 30%概率可带劲啦群伤50并拉拽5格实体
				stack.damage(10, attacker, e -> {
				});
				attacker.heal(1.0F);
				target.playSound(SoundEvents.BLOCK_STONE_BREAK, 1.0F, 1.0F);
				target.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 2));
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 9));
				world.syncWorldEvent(2001, target.getBlockPos(),
						Block.getRawIdFromState(Blocks.OBSIDIAN.getDefaultState()));
				if (Math.random() < 0.3D) {
					attacker.heal(10.0F);
					stack.damage(50, attacker, e -> {
					});
					target.damage(DamageLoader.kdjl(target), 50.0F);
					target.playSound(SoundLoader.EXGLUTENBUR_HIT, 1.0F, 1.0F);
					if (attacker instanceof PlayerEntity player) {
						player.sendMessage(Text.translatable("message.huajiager.exglutenbur.kdjl"), false);
					}
					List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class,
							target.getBoundingBox().expand(5.0D),
							entity -> entity != attacker && entity != target);
					for (LivingEntity entity : entities) {
						Vec3d vec = getVectorEntity(target, entity);
						if (vec.length() != 0) {
							entity.damage(DamageLoader.kdjl(entity), (float) (20.0D / vec.length()));
							entity.setVelocity(entity.getVelocity().add(-vec.x / vec.length(),
									-vec.y / vec.length(), -vec.z / vec.length()));
						}
					}
				}
			}
			default -> {
			}
		}
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		// 动态属性：仅主手持握时按 flavor 施加伤害/攻速修正，否则移除。
		// 与 getAttributeModifiers 一致：修饰符 Amount 写"总值"——
		// 攻速 flavor1=-1.7、其余 -2.4；伤害 flavor1=22、flavor2=8、flavor3=17、flavor0 不写
		if (living instanceof PlayerEntity player && player.getMainHandStack() == stack) {
			int f = flavor(stack);
			double speedBonus = f == 1 ? -1.7D : -2.4D;
			boolean writeDamage = f != 0;
			double damageBonus = switch (f) {
				case 1 -> 22.0D;  // damageAromatic+damage = 15+7
				case 2 -> 8.0D;   // 1f+damage = 1+7
				case 3 -> 17.0D;  // 10f+damage = 10+7
				default -> 0.0D;
			};
			applyRemove(stack, FLAVOR_ATTACK_SPEED_ID, EntityAttributes.GENERIC_ATTACK_SPEED, speedBonus,
					EntityAttributeModifier.Operation.ADDITION, true);
			applyRemove(stack, FLAVOR_ATTACK_DAMAGE_ID, EntityAttributes.GENERIC_ATTACK_DAMAGE, damageBonus,
					EntityAttributeModifier.Operation.ADDITION, writeDamage);
		} else {
			removeAttributeModifierByUuid(stack, FLAVOR_ATTACK_SPEED_ID);
			removeAttributeModifierByUuid(stack, FLAVOR_ATTACK_DAMAGE_ID);
		}
		//   buff：flavor2 抗火/力量/再生；flavor3 凋零/速度/石灰粒子
		if (living.getMainHandStack() == stack || living.getOffHandStack() == stack) {
			switch (flavor(stack)) {
				case 2 -> {
					if (!world.isClient) {
						if (!(living.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
								&& living.hasStatusEffect(StatusEffects.STRENGTH)
								&& living.hasStatusEffect(StatusEffects.REGENERATION))) {
							living.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 60, 0, false, false));
							living.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 2, false, false));
							living.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 2, false, false));
						}
					}
				}
				case 3 -> {
					if (!world.isClient) {
						if (!(living.hasStatusEffect(StatusEffects.WITHER)
								&& living.hasStatusEffect(StatusEffects.SPEED))) {
							living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1, false, false));
							living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 4, false, false));
						}
					}
					// 石灰粒子（0.05/tick 概率）
					if (Math.random() < 0.05D) {
						world.syncWorldEvent(2004, living.getBlockPos(), 0xFF4500);
					}
				}
				default -> {
				}
			}
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (player.isSneaking()) {
			if (!world.isClient) {
				// 服务端：循环切风味（0→1→2→3→0）
				setFlavor(stack, flavor(stack) + 1);
			} else {
				// 客户端：按当前（旧）flavor 播"即将切入"的风味音效与提示（对齐
				//  客户端分支——切换后 NBT 尚未同步，读到的仍是旧值）
				switch (flavor(stack)) {
					case 0 -> {
						player.playSound(SoundLoader.EXGLUTENBUR_1, 1.0F, 1.0F);
						player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.1"), false);
					}
					case 1 -> {
						player.playSound(SoundLoader.EXGLUTENBUR_2, 1.0F, 1.0F);
						player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.2"), false);
					}
					case 2 -> {
						player.playSound(SoundLoader.EXGLUTENBUR_3, 1.0F, 1.0F);
						player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.3"), false);
					}
					default -> {
					}
				}
			}
		}
		return TypedActionResult.success(stack);
	}

	/**  HAMathHelper.getVectorEntity：从 source 指向 target 的单位向量。 */
	private static Vec3d getVectorEntity(Entity source, Entity target) {
		BlockPos sourcePos = source.getBlockPos();
		BlockPos targetPos = target.getBlockPos();
		return new Vec3d(targetPos.getX() - sourcePos.getX(),
				targetPos.getY() - sourcePos.getY(),
				targetPos.getZ() - sourcePos.getZ()).normalize();
	}

	private static void applyRemove(ItemStack stack, UUID uuid, EntityAttribute attribute,
			double amount, EntityAttributeModifier.Operation operation, boolean active) {
		if (active) {
			// 幂等：NBT 已含同 UUID 同数值的修饰符时不再重写，避免每 tick 改写
			// AttributeModifiers 触发物品栏同步/手持物品切换动画（第一人称抽搐）
			if (modifierMatches(stack, uuid, amount)) {
				return;
			}
			removeAttributeModifierByUuid(stack, uuid);
			stack.addAttributeModifier(attribute,
					new EntityAttributeModifier(uuid, "Tool modifier", amount, operation),
					EquipmentSlot.MAINHAND);
		} else {
			removeAttributeModifierByUuid(stack, uuid);
		}
	}

	/** NBT 中是否已存在同 UUID 且数值一致的属性修饰符（Operation 为常量，不参与比较）。
	 * 1.20.1 的 AttributeModifiers UUID 由 NbtHelper.fromUuid 存为 int 数组，必须用
	 * NbtHelper.toUuid 读取；此前用 getString("UUID") 恒读到空串，幂等检查永远失败，
	 * 每 tick 重写/追加修饰符，是"手上抽搐"的根因。 */
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
