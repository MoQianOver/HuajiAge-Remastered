package org.huajiager.item;

import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Random;

/**
 * 辣条剑·滑稽。
 *
 *  setMaxDamage + 属性修饰等价于选择耐久 5400、攻击 14、攻速 2.4 的 ToolMaterial
 * 交由 SwordItem 计算（未覆写属性修饰，攻击数值与目标一致）。
 * 构造函数中的 addPropertyOverride("burst") 在 Fabric 侧由客户端
 * ModelPredicateProvider 注册（见 HuajiAgeRemasteredClient）。
 */
public class ItemHuajiLatiaoSword extends SwordItem {

	private static final ToolMaterial LATIAO_MATERIAL = new ToolMaterial() {
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
			return 14.0F;
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
			return Ingredient.EMPTY;
		}
	};

	public ItemHuajiLatiaoSword() {
		super(LATIAO_MATERIAL, 3, -2.4F, new Item.Settings().maxDamage(5400));
	}

	public static boolean isOpen(ItemStack stack) {
		return NBTHelper.getTagCompoundSafe(stack).getInt("hot") != 0;
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!isOpen(stack)) {
			stack.damage(1, attacker, e -> {
			});
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 2));
		} else {
			target.setOnFireFor(8);
			target.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
			target.playSound(SoundEvents.ENTITY_GENERIC_BURN, 1.0F, 0.1F);
		}
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof PlayerEntity player)) {
			return;
		}
		if (player.getMainHandStack() == stack) {
			if (isOpen(stack)) {
				if (!world.isClient) {
					if (!(player.hasStatusEffect(StatusEffects.SPEED)
							&& player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
							&& player.hasStatusEffect(StatusEffects.STRENGTH))) {
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1));
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 60, 0));
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 1));
					}
				} else {
					if (stack.getDamage() > 0 && player.getHungerManager().getFoodLevel() > 10) {
						if (player.age % 50 == 0) {
							stack.setDamage(Math.max(0, stack.getDamage() - 100));
							player.getHungerManager().setFoodLevel(player.getHungerManager().getFoodLevel() - 1);
							player.playSound(SoundEvents.ENTITY_PARROT_EAT, 0.5F, 1.0F);
						}
					}
				}
			}
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (player.isSneaking()) {
			if (!world.isClient) {
				NBTHelper.getTagCompoundSafe(stack).putInt("hot", isOpen(stack) ? 0 : 1);
			}
			player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
		} else {
			if (isOpen(stack)) {
				player.setVelocity(player.getVelocity().add(0, 0.6, 0));
				player.fallDistance = 0;
				player.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0F, 1.0F);
				player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
				if (!world.isClient) {
					if (player.getHungerManager().getFoodLevel() < 20) {
						player.getHungerManager().setFoodLevel(player.getHungerManager().getFoodLevel() + 2);
					}
					stack.damage(20, player, e -> {
					});
				}
				if (world.isClient) {
					Random random = new Random();
					double spawnX = player.getX();
					double spawnY = player.getY();
					double spawnZ = player.getZ();
					for (int i = 0; i < 10; ++i) {
						for (double d = 0; d < 360; d += 15) {
							double rad = Math.toRadians(d);
							world.addParticle(net.minecraft.particle.ParticleTypes.FLAME,
									spawnX + 0.5 * Math.sin(rad), spawnY + 0.1 + random.nextDouble() * 0.2,
									spawnZ + 0.5 * Math.cos(rad),
									0.5 * Math.sin(rad), 0.1, 0.5 * Math.cos(rad));
						}
					}
				}
			}
		}
		return TypedActionResult.success(stack);
	}
}
