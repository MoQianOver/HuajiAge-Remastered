package org.huajiager.entity;

import java.util.List;
import java.util.UUID;

import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.events.EventKillerQueen;
import org.huajiager.util.HAMathHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;

/**
 * 杀手皇后·枯萎穿透第一形态（Sheer Heart Attack）实体。
 *
 * 自主索敌型召唤物：高血量高护甲，被动跟随主人攻击怪物/史莱姆；锁定攻击目标后
 * 加速扑向目标，接触时按距离衰减造成大范围爆炸伤害；生命耗尽或主人死亡时自爆消失。
 *
 * AI 方案（2026-09 恢复索敌行为）：
 * - 跟随：FollowOwnerGoal 松散跟随（5 格外启动跟随、20 格内停止），
 *   让小车在 20 格范围内自主索敌作战，不会一直紧贴主人。 * - 近战：MeleeAttackGoal 贴身辅助（主动索敌后的主攻是 tick 中近距离直线飞扑，见 tick()）。 * - 飞扑：tick 中锁定目标进入 8 格后 velocity = 视线向量 * 0.8 直线扑向目标。 * - 索敌：AttackWithOwnerGoal + TrackOwnerAttackerGoal + 自动锁定敌对生物
 *   （HostileEntity）与史莱姆（SlimeEntity）+ RevengeGoal。
 */
public class EntitySheerHeartAttack extends TameableEntity {

	private static final String TAG_LIFE = "life";
	private static final String TAG_DAMAGE = "damage";
	private static final String TAG_TRIGGER = "trigger";

	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntitySheerHeartAttack.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> DAMAGE = DataTracker.registerData(EntitySheerHeartAttack.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> TRIGGER = DataTracker.registerData(EntitySheerHeartAttack.class,
			TrackedDataHandlerRegistry.BOOLEAN);

	// 碰撞爆炸冷却（tick 数）：tick() 中 boundingBox 相交判断在爆炸把目标炸飞后仍可能连续成立，
	// 不加冷却会导致每 tick 都触发 3f 爆炸——"把生物炸上天然后一直爆炸"。
	private int explodeCooldown;

	// 目标锁定：防止前后两个相近目标被目标选择 AI 每 tick 交替选中导致小车来回抽搐。
	// 当前目标仍存活且在有效索敌范围内时保持锁定不切换，仅在目标死亡/离开范围时解锁重选。
	private LivingEntity lockedTarget;

	public static final EntityType<EntitySheerHeartAttack> TYPE = EntityType.Builder
			.<EntitySheerHeartAttack>create((type, world) -> new EntitySheerHeartAttack(type, world), SpawnGroup.MISC)
			.setDimensions(0.6f, 0.8f)
			.build("huajiager:sheer_heart_attack");

	// 基于 TameableEntity.createMobAttributes() 构建，确保包含 MobEntity 导航等
	// 初始化所需的全部基础属性（如 generic.follow_range，缺失会在构造时
	// EntityNavigation.<init> 抛 "Can't find attribute minecraft:generic.follow_range"），
	// 再覆盖 KQ 小车专属数值。
	public static final DefaultAttributeContainer SHEER_ATTRIBUTES = TameableEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 999.0)
			.add(EntityAttributes.GENERIC_ARMOR, 999.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6)
			.build();

	public EntitySheerHeartAttack(EntityType<EntitySheerHeartAttack> type, World world) {
		super(type, world);
	}

	public EntitySheerHeartAttack(World worldIn) {
		this(TYPE, worldIn);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(LIFE, 400F);
		tracker.startTracking(DAMAGE, 15F);
		tracker.startTracking(TRIGGER, false);
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new SwimGoal(this));
		// 贴身近战辅助；主动索敌后的主攻是 tick 中近距离直线飞扑（见 tick()）。
		this.goalSelector.add(1, new MeleeAttackGoal(this, 0.5D, false));
		// 松散跟随（5 格外启动跟随、20 格内停止）：让小车在 20 格范围内自主索敌作战，不会一直紧贴主人。
		this.goalSelector.add(4, new FollowOwnerGoal(this, 1.0D, 5.0F, 20.0F, false));
		this.goalSelector.add(5, new WanderAroundGoal(this, 0.4D));
		this.goalSelector.add(7, new LookAtEntityGoal(this, MobEntity.class, 10.0F));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));

		// 狗式目标选择（WolfEntity 同款组合 + 自动锁定敌对生物）：
		//   AttackWithOwnerGoal：主人被攻击 -> 锁定攻击者并攻击
		//   TrackOwnerAttackerGoal：主人攻击的目标 -> 锁定并攻击
		//   ActiveTargetGoal：自动锁定敌对生物（HostileEntity）与史莱姆
		//   RevengeGoal：自己被攻击 -> 复仇
		this.targetSelector.add(1, new AttackWithOwnerGoal(this));
		this.targetSelector.add(2, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, HostileEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, SlimeEntity.class, true));
		this.targetSelector.add(4, new RevengeGoal(this, new Class[0]));
	}

	// 禁止把主人（玩家）与替身类实体作为攻击目标。缺失此限制时，小车被玩家误伤/替身拳击
	// 波及触发 RevengeGoal 复仇，会把主人锁为目标，tick 中 distance<8 扑向主人、贴身爆炸——
	// "一直追着我炸"即此问题。
	// 直接排除所有玩家与替身：不依赖 getOwner()/ownerUuid 解析状态（两者在客户端实体/不同步
	// 场景可能为 null，导致拦截失效），任何玩家都永远不可能成为小车目标，彻底杜绝"卡地里/跟着主人飞"。
	@Override
	public boolean canTarget(LivingEntity target) {
		return target != null && !(target instanceof PlayerEntity) && !(target instanceof TameableEntity);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putFloat(TAG_DAMAGE, getDamage());
		nbt.putBoolean(TAG_TRIGGER, isTriggered());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setLife(nbt.getFloat(TAG_LIFE));
		setDamage(nbt.getFloat(TAG_DAMAGE));
		setTrigger(nbt.getBoolean(TAG_TRIGGER));
	}

	// TameableEntity 1.20.1 中 method_48926 为返回 EntityView 的抽象方法（yarn 未映射），此处实现为当前世界
	@Override
	public EntityView method_48926() {
		return this.getWorld();
	}

	// 替身 AI 近战接触攻击（MeleeAttackGoal）命中：补发"点赞"锁定目标
	@Override
	public boolean tryAttack(Entity target) {
		boolean flag = super.tryAttack(target);
		if (flag && target instanceof LivingEntity living && getOwner() instanceof PlayerEntity owner) {
			EventKillerQueen.onStandHit(owner, living);
		}
		return flag;
	}

	@Override
	public void tick() {
		super.tick();
		if (explodeCooldown > 0) {
			explodeCooldown--;
		}
		// 兜底防御：任何目标 AI（如误伤触发 RevengeGoal 的漏网路径）若把玩家（含主人）
		// 锁为攻击目标，立即清除，避免小车追着玩家贴身爆炸/被推着卡地里/跟飞。
		if (this.getTarget() instanceof PlayerEntity) {
			this.setTarget(null);
		}
		// 目标锁定稳定性：上一 tick 锁定的目标若仍存活且在 16 格有效索敌范围内，
		// 则强制保持该目标（NearestAttackableTargetGoal 会在两个距离相近的目标间
		// 每 tick 交替选择，导致小车前后来回抽搐）；目标死亡/离开范围后解锁，
		// 重新交给目标选择 AI 选新目标。
		LivingEntity locked = this.lockedTarget;
		LivingEntity currentTarget = this.getTarget();
		if (locked != null && locked.isAlive() && locked.squaredDistanceTo(this) < 256.0F && canTarget(locked)) {
			if (currentTarget != locked) {
				this.setTarget(locked);
			}
			this.lockedTarget = locked;
		} else {
			this.lockedTarget = null;
			currentTarget = this.getTarget();
			if (currentTarget != null && currentTarget.isAlive() && currentTarget.squaredDistanceTo(this) < 256.0F
					&& canTarget(currentTarget)) {
				this.lockedTarget = currentTarget;
			}
		}
		LivingEntity entity = this.getTarget();
		// 目标已死亡（爆炸后尸体仍短暂存在/目标死亡但 AI 未清理）时立即解除锁定：
		// 避免小车继续把尸体当目标，每 tick 覆盖 velocity 扑向原地尸体导致抽搐。
		if (entity != null && !entity.isAlive()) {
			this.setTarget(null);
			entity = null;
		}
		List<EntitySheerHeartAttack> attack = this.getWorld().getEntitiesByClass(EntitySheerHeartAttack.class,
				this.getBoundingBox().expand(100), e -> true);
		if (getLife() > 0) {
			setLife(getLife() - 1);
		} else {
			this.getWorld().createExplosion(this, getX(), getY(), getZ(), 1f, false, World.ExplosionSourceType.NONE);
			this.discard();
		}
		if (entity != null && HAMathHelper.getDistance(this.getPos(), entity.getPos()) < 8) {
			if (!isTriggered()) {
				this.getWorld().playSound(null, getX(), getY(), getZ(), SoundLoader.STAND_KILLER_QUEEN_TRIGGER,
						SoundCategory.NEUTRAL, 2f, 1f);
				setTrigger(true);
			}
			// 索敌飞扑：锁定目标进入 8 格后，velocity = 视线向量 * 0.8（每 tick 覆盖），
			// 直线扑向目标；接触爆炸由下方 boundingBox 相交逻辑处理。
			Vec3d vec = HAMathHelper.getVectorEntityEye(this, entity);
			this.setVelocity(vec.x * 0.8, vec.y * 0.8, vec.z * 0.8);
		} else {
			setTrigger(false);
		}
		if (attack != null) {
			for (EntitySheerHeartAttack e : attack) {
				if (e != this && e.getOwnerUuid() != null && e.getOwnerUuid().equals(this.getOwnerUuid())
						&& e.age > this.age) {
					e.discard();
				}
			}
		}
		// 朝向同步：把实体朝向（yaw/bodyYaw/headYaw）每 tick 对齐水平移动方向。
		// 小车速度由 setVelocity 直推（不走导航），默认 BodyControl/LookControl 的
		// 朝向插值与实际移动方向脱节——普通追踪时表现为脸朝后跑，飞扑时 look 插值
		// 与速度方向存在相位差、观感为旋转着飞过去。此处按速度水平方向固定朝向，
		// 飞扑时速度方向恒为指向目标的视线方向，朝向随之锁定，不再旋转。
		// 注意 MC yaw 定义：0=+Z（南）、顺时针为正（90=-X 西），与数学 atan2
		// （逆时针为正）方向相反，故取负号，否则朝向与移动方向错位成"横着走"。
		Vec3d velocity = this.getVelocity();
		if (velocity.horizontalLengthSquared() > 1.0E-4) {
			float yaw = (float) (-MathHelper.atan2(velocity.x, velocity.z) * 180.0D / Math.PI);
			this.setYaw(yaw);
			this.setBodyYaw(yaw);
			this.setHeadYaw(yaw);
			this.setPitch(0.0F);
		}
		if (getOwner() == null || !getOwner().isAlive()) {
			this.getWorld().createExplosion(this, getX(), getY(), getZ(), 2f, false, World.ExplosionSourceType.NONE);
			this.discard();
		}
		// 接触爆炸：与攻击目标接触时对其周围 5 格实体造成按距离衰减的爆炸伤害。
		// explodeCooldown 防止 tick 相交判定在爆炸炸飞目标后仍连续成立导致的"每 tick 一爆"。
		if (entity != null && explodeCooldown <= 0 && this.getBoundingBox().intersects(entity.getBoundingBox())) {
			List<LivingEntity> list = this.getWorld().getEntitiesByClass(LivingEntity.class,
					entity.getBoundingBox().expand(5), e -> true);
			if (list != null) {
				for (LivingEntity living : list) {
					if (living != getOwner() && !(living instanceof TameableEntity)) {
						float distance = (float) HAMathHelper.getDistance(this.getPos(), living.getEyePos());
						float damage = getDamage() * (5 - distance) / 5;
						living.damage(this.getDamageSources().explosion(this, getOwner()), damage > 0 ? damage : 0);
						// 替身 AI 碰撞爆炸命中目标：发放/更新"点赞"锁定被炸的怪（AI 接触攻击不走 rangePunchAttack）
						if (living == entity && getOwner() instanceof PlayerEntity owner) {
							EventKillerQueen.onStandHit(owner, living);
						}
					}
				}
				// 爆炸后进入冷却（60 tick = 3 秒）：爆炸把目标/自身炸开后需重新接触才能再次触发
				this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
						SoundCategory.NEUTRAL, 2f, 1f);
				// 爆炸粒子（改为纯伤害循环后需手动补爆炸闪光与烟雾）
				if (this.getWorld() instanceof ServerWorld serverWorld) {
					serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
							1, 0.0, 0.0, 0.0, 0.0);
					serverWorld.spawnParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
							8, 1.5, 1.5, 1.5, 0.05);
				}
				// 爆炸后进入 60 tick（3 秒）冷却：小车继续存活、可再索敌下一目标，
				// 与目标重新接触后才再次触发接触爆炸；防止 tick 相交判定在目标
				// 未及时被炸离时连续触发"每 tick 一爆"。
				explodeCooldown = 60;
			}
		}
	}

	// 生成时把实体从重叠方块中挤出：玩家贴墙/在方块边缘召唤时小车可能生成进方块里卡成"遁地"
	public void pushOutOfBlocksSafe() {
		this.pushOutOfBlocks(this.getX(), this.getY(), this.getZ());
	}

	// ===== 避免"接触爆炸冲击波弹飞自身"导致卡地/卡墙 =====
	// 1.20.1 的 World.createExplosion 没有"只破坏伤害/击退"的开关：
	// ExplosionSourceType.NONE 仅保证方块不被破坏（DestructionType.KEEP），
	// 对实体的爆炸伤害与冲击波击退仍照常发生。小车处于爆炸中心，每次爆炸都
	// 会被自己炸飞，在狭窄地形（树林/矿洞/墙角）里弹进墙中或地里卡住——
	// 玩家看不见车但 AI 正常运转，表现即为"遁地"。
	// 解决：接触爆炸不再走世界爆炸，改为纯范围伤害 + 音效（伤害循环已有）。	// 同时小车免疫自身爆炸伤害（防其它爆炸源误伤自己）。

	@Override
	public boolean damage(DamageSource source, float amount) {
		// 免疫自身爆炸伤害（接触爆炸的爆炸源就是小车自己，不应对自己造成伤害）
		if (source.getSource() == this || source.getAttacker() == this) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (player == getOwner()) {
			this.discard();
			player.heal(10f);
			player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1f);
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 1));
		}
		return super.interactMob(player, hand);
	}

	public float getLife() {
		return this.getDataTracker().get(LIFE);
	}

	public void setLife(float timeTick) {
		this.getDataTracker().set(LIFE, timeTick);
	}

	public float getDamage() {
		return this.getDataTracker().get(DAMAGE);
	}

	public void setDamage(float damage) {
		this.getDataTracker().set(DAMAGE, damage);
	}

	public Boolean isTriggered() {
		return this.getDataTracker().get(TRIGGER);
	}

	public void setTrigger(boolean trigger) {
		this.getDataTracker().set(TRIGGER, trigger);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundLoader.SHEER_HEART_ATTACK;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WITHER_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.7f;
	}

	@Override
	protected void playStepSound(BlockPos pos, net.minecraft.block.BlockState state) {
		this.playSound(SoundEvents.ENTITY_MINECART_RIDING, 0.5F, 1.0F);
	}

	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity passiveEntity) {
		return null;
	}

	// ===== 繁殖与可攻击目标限制 =====
	// 小车不可繁殖；可攻击目标限制已由 ActiveTargetGoal + canTarget（排除玩家与替身）覆盖，
	// 无需额外实现断言性 API。
}
