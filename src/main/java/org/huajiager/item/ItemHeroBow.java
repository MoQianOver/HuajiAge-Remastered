package org.huajiager.item;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.entity.EntityHeroArrow;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.sound.SoundLoader;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** 大英雄之弓，独立编写（语义 ，未照搬第三方移植）。 */
public class ItemHeroBow extends BowItem {

	/** Burst（解放）状态 NBT 标签， NBTHelper 语义。 */
	private static final String TAG_OPEN = "open";

	public ItemHeroBow() {
		super(new Item.Settings().maxDamage(384)); // maxDamage() 内部已自动置 maxCount=1，不可再调 maxCount()
	}

	/** 是否处于 Burst（解放）状态。 */
	public static boolean isOpen(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean(TAG_OPEN);
	}

	/**
	 * 切换 Burst 状态：
	 * 置 1 或移除标签。
	 */
	public static void toggleMode(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (isOpen(stack)) {
			nbt.remove(TAG_OPEN);
		} else {
			nbt.putBoolean(TAG_OPEN, true);
		}
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return super.hasGlint(stack) || isOpen(stack);
	}

	@Override
	public Text getName(ItemStack stack) {
		if (isOpen(stack)) {
			return Text.literal(super.getName(stack).getString() + Text.translatable("huajiager.burstout").getString());
		}
		return super.getName(stack);
	}

	/**
	 * 投影物（箭/英雄之箭）spawn 后立即按当前速度初始化朝向角（yaw/pitch）。
	 *
	 * vanilla 的 BowItem 发射路径只调 ProjectileEntity.setVelocity 设置速度，
	 * 朝向本靠实体首 tick 由速度分量反算（prev 为 0 时触发）。但在时停中射箭时，
	 * EventTimeStop.onTheWorld 会在实体首 tick 之前就将其冻结：saveMotionAndPos
	 * 存档的 yaw/pitch 仍是默认 0，随后 setVelocity(ZERO) 清速，首 tick 反算
	 * atan2(0,0)=0，朝向坍缩成默认方向（渲染表现为"一律朝南"），直到时停解冻
	 * 恢复速度后下一 tick 才重算回正确方向。这里在 setVelocity 之后立即按速度
	 * 反算 yaw/pitch：既让冻结存档取到正确朝向角，也让冻结期客户端渲染保持正确。
	 * prev 防覆盖交由 PersistentProjectileEntityMixin 处理（mixin 内字段可直写）。
	 */
	private static void initProjectileFacing(ProjectileEntity p) {
		Vec3d v = p.getVelocity();
		p.setYaw((float) (MathHelper.atan2(v.x, v.z) * 57.2957763671875D));
		p.setPitch((float) (MathHelper.atan2(v.y, v.horizontalLength()) * 57.2957763671875D));
	}

	/** Burst 态详细能力说明（客户端 tooltip：按住 Shift 时展示 1.1~1.4）。 */
	public static List<Text> createDetailedTooltip() {
		List<Text> list = new ArrayList<>();
		list.add(Text.translatable("item.hero_bow:unicode_tooltips.1.1.desc"));
		list.add(Text.translatable("item.hero_bow:unicode_tooltips.1.2.desc"));
		list.add(Text.translatable("item.hero_bow:unicode_tooltips.1.3.desc"));
		list.add(Text.translatable("item.hero_bow:unicode_tooltips.1.4.desc"));
		return list;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		return TypedActionResult.consume(stack);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity player)) {
			return;
		}
		int useTicks = this.getMaxUseTime(stack) - remainingUseTicks;
		if (useTicks < 0) {
			return;
		}
		float f = BowItem.getPullProgress(useTicks);
		if (f < 0.1f) {
			return;
		}
		boolean burst = isOpen(stack) && f >= 1.0f;
		boolean hasHope = player.hasStatusEffect(PotionLoader.potionFlowerHope);

		if (burst) {
			if (!hasHope) {
				world.playSound(null, player.getBlockPos(), SoundLoader.STELLA, SoundCategory.PLAYERS, 5f, 1f);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
						SoundCategory.PLAYERS, 3f, 1f);
			}
		} else {
			world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
		}

		if (!world.isClient) {
			if (burst) {
				if (!hasHope) {
					// 英雄之箭：命中 50 格威力爆炸 + 自伤（attackEntityFrom(stella, maxHealth*5)
					// 为普通伤害源，可被护甲/抗性减免；Fabric 原用 genericKill 无视护甲必死，
					// 与用户实测"不死"不符，改为掉血但不死——至少保留 1 点生命）
					EntityHeroArrow heroArrow = new EntityHeroArrow(world, player);
					float speed = f * 6.0f;
					// 与普通箭发射路径对齐：走 ProjectileEntity.setVelocity(shooter,pitch,yaw,...)
					// 标准方法按玩家视角弹簧方向精确发射，避免手写 getRotationVec 在服务端
					// 取到的朝向与实际准星存在偏差（表现为"滑稽之星斜着飞、不对准屏幕中心"）。
					// divergence=0 无随机散布，真名解放应精准命中准星。
					heroArrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, speed, 0.0f);
					initProjectileFacing(heroArrow);
					world.spawnEntity(heroArrow);
					if (player.getHealth() > 1f) {
						float stellaDmg = Math.min(player.getMaxHealth() * 5f, player.getHealth() - 1f);
						player.damage(DamageLoader.stella(player), stellaDmg);
					}
					// 攻击消耗本体：Burst 一击后物品消失
					if (!player.getAbilities().creativeMode) {
						stack.decrement(1);
					}
				} else {
					// 持有希望之花：不发射，仅提示
					player.sendMessage(Text.translatable("message.huaji.orga.hero.shot"), true);
				}
			} else {
				// 普通箭发射（附魔/状态/不消耗箭袋，语义同 findAmmo 落空时兜底 Items.ARROW）
				ArrowEntity arrow = new ArrowEntity(world, player);
				float velocity = f * 6.0f;
				arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, velocity, 1.0f);
				initProjectileFacing(arrow);
				if (f >= 1.0f) {
					arrow.setCritical(true);
				}
				int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
				if (powerLevel > 0) {
					arrow.setDamage(arrow.getDamage() + powerLevel * 2.0 + 0.5);
				}
				int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
				if (punchLevel > 0) {
					arrow.setPunch(punchLevel);
				}
				if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
					arrow.setOnFireFor(5);
				}
				StatusEffectInstance strength = player.getStatusEffect(StatusEffects.STRENGTH);
				if (strength != null) {
					arrow.setDamage(arrow.getDamage() + strength.getAmplifier());
				}
				//  PickupStatus.CREATIVE_ONLY：英雄之弓发射的箭不可拾取
				arrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
				world.spawnEntity(arrow);
			}
			// 每次射击掉 1 点耐久
			if (!player.getAbilities().creativeMode) {
				stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof LivingEntity living) || world.isClient) {
			return;
		}
		// 仅主手持握时触发
		if (!living.getMainHandStack().isOf(this)) {
			return;
		}
		// 四增益：SPEED / RESISTANCE / STRENGTH / REGENERATION（260t，level 2）
		boolean full = living.hasStatusEffect(StatusEffects.SPEED)
				&& living.hasStatusEffect(StatusEffects.RESISTANCE)
				&& living.hasStatusEffect(StatusEffects.STRENGTH)
				&& living.hasStatusEffect(StatusEffects.REGENERATION);
		if (!full) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 260, 2));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 260, 2));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 260, 2));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 260, 2));
		}
		// 夜视：持续时间不足 240t 时刷新
		StatusEffectInstance nv = living.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (nv == null || nv.getDuration() <= 240) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0));
		}
		// 清除中毒与凋零
		living.removeStatusEffect(StatusEffects.POISON);
		living.removeStatusEffect(StatusEffects.WITHER);
	}
}
