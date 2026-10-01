package org.huajiager.item;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.sound.SoundLoader;

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
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 滑稽之星剑。
 *
 *  STAR/STARU 两个 ToolMaterial 中仅 STAR 实际用于构造（super(STAR)），
 * STARU 为历史遗留定义未使用，实际仅使用 STAR。
 *  addPropertyOverride("burst") 对应 huaji_star_sword.json 的 burst 谓词
 * （0 -> star_sword_0 / 1 -> star_sword_1），Fabric 侧在客户端以
 * ModelPredicateProviderRegistry 注册。
 * 左键空挥播 WAVE1 音效由
 * 客户端事件 EventHuajiStarSwordLeftClick 承接（Fabric 1.20.1 无空挥事件）。
 * <p>伤害说明： 关闭/开启两态基础攻击力相同（material 20 + 剑加成 3 =
 * 23），关闭态仅由状态效果施加减速/虚弱、开启态追加虚空伤害与着火。此前实现
 * 曾试图用 inventoryTick 动态增减 -9 攻击修饰符模拟“关闭态 14 伤害”，但
 * 1.20.1 的 ItemStack.addAttributeModifier 每 tick 向 NBT 重复写入同 UUID 修饰符、
 * 且 NBT 中 UUID 以 IntArray 存储导致手工 remove 永远失效，实测关闭态伤害显示
 * 异常（-9）且无法造成伤害，故移除该机制、回归恒定伤害（独立修复）。
 */
public class ItemHuajiStarSword extends SwordItem {

	protected float attackSpeed = -2.4F;
	protected int damage = 13;
	protected int damageCharged = 10;

	private static final ToolMaterial STAR_MATERIAL = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 5400;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return 20.0F;
		}

		@Override
		public float getAttackDamage() {
			return 20.0F;
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
			return Ingredient.ofItems(ItemLoader.neutronStarFragment);
		}
	};

	public ItemHuajiStarSword() {
		super(STAR_MATERIAL, 3, -2.4F, new Item.Settings().maxDamage(5400));
	}

	/** 是否处于开启（burst）状态： isOpen 语义为 NBT 存在 "open" 键。 */
	public static boolean isOpen(ItemStack stack) {
		return stack.getOrCreateNbt().contains("open");
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!isOpen(stack)) {
			stack.damage(1, attacker, e -> {
			});
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 2));
		} else {
			target.damage(target.getDamageSources().outOfWorld(), 10.0F);
			target.setOnFireFor(5);
			target.playSound(SoundLoader.WAVE1, 1.0F, 1.0F);
			target.playSound(SoundLoader.ENERGY_HIT, 1.0F, 0.1F);
		}
		return true;
	}

	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		//  getIsRepairable：中子弹星碎片（netronStarFragment）存在即可修复
		return ingredient.isOf(ItemLoader.neutronStarFragment);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof PlayerEntity player)) {
			return;
		}
		boolean heldMain = player.getMainHandStack() == stack;
		if (heldMain && isOpen(stack) && !world.isClient) {
			if (!(player.hasStatusEffect(StatusEffects.SPEED)
					&& player.hasStatusEffect(StatusEffects.HUNGER)
					&& player.hasStatusEffect(StatusEffects.STRENGTH))) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 3));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 6));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 3));
			}
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (player.isSneaking()) {
			if (!world.isClient) {
				if (isOpen(stack)) {
					stack.getOrCreateNbt().remove("open");
				} else {
					stack.getOrCreateNbt().putBoolean("open", true);
				}
			}
			player.playSound(SoundLoader.CHARGE, 1.0F, 1.0F);
		}
		return TypedActionResult.success(stack);
	}
}
