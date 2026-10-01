package org.huajiager.entity;

import java.util.List;
import java.util.UUID;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 五色之力弹（菲华力）实体， 。
 *
 * 继承 EntityThrowable，无重力；由 master（持有五色之力的玩家 UUID）驱动：
 * 未触发（de=false）时对附近实体造成火焰 + 15/50 点范围伤害并召雷，触发时改为
 * 附加致盲/缓速/虚弱/发光；命中目标造成翡翠溅射伤害。Fabric 对应 ProjectileEntity。
 * 替身实体 EntityStandBase 已，补回 !(entity instanceof EntityStandBase)
 * 排除：弹体射线检测、范围伤害与命中分支均不把替身展示实体当作目标，
 * 避免发射的五五开之力弹误伤/误引爆自己或队友召唤的替身。
 *
 * 运动驱动说明（双重驱动抖动修复）：不能继承 ThrownEntity——其 tick 自带
 *  位移，客户端影分身也会每 tick 本地推进，与服务端位置同步叠加成
 * "双重驱动"，表现为弹体沿弹道前后一抽一抽（与 EntityHeroArrow 同因）。
 * 改继承 ProjectileEntity 后按 EntityHeroArrow 已验证姿势：客户端只播粒子不参与
 * 物理，位移/碰撞/引爆全部由服务端权威推进，客户端只按网络同步的位置渲染。
 */
public class EntityFivePower extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}


	private static final String TAG_LIFE = "life";
	private static final String TAG_DE = "de";
	private static final String TAG_MASTER = "master";

	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntityFivePower.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> DE = DataTracker.registerData(EntityFivePower.class,
			TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<String> MASTER = DataTracker.registerData(EntityFivePower.class,
			TrackedDataHandlerRegistry.STRING);

	public static final EntityType<EntityFivePower> TYPE = EntityType.Builder
			.<EntityFivePower>create((type, world) -> new EntityFivePower(type, world), SpawnGroup.MISC)
			.setDimensions(0.25f, 0.25f)
			.build("huajiager:five_power");

	public EntityFivePower(EntityType<EntityFivePower> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	public EntityFivePower(World worldIn) {
		this(TYPE, worldIn);
	}

	public EntityFivePower(World worldIn, LivingEntity throwerIn) {
		this(worldIn);
		this.setOwner(throwerIn);
		this.setPosition(throwerIn.getX(), throwerIn.getEyeY() - 0.1, throwerIn.getZ());
		Vec3d look = throwerIn.getRotationVector();
		this.setVelocity(look.multiply(1.5));
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(LIFE, 0F);
		tracker.startTracking(DE, false);
		tracker.startTracking(MASTER, "acfd894c-ad88-4b34-addf-a8d10e2a67f7");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putBoolean(TAG_DE, isDe());
		nbt.putString(TAG_MASTER, getMatser());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setLife(nbt.getFloat(TAG_LIFE));
		setDe(nbt.getBoolean(TAG_DE));
		setMaster(nbt.getString(TAG_MASTER));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved()) {
			return;
		}
		if (this.getWorld().isClient) {
			// 客户端影分身不参与物理：位移/碰撞/引爆全部由服务端权威推进，
			// 客户端只按网络同步的位置渲染。本地再  推进会与服务端
			// 位置覆盖叠加成"双重驱动"，表现为弹体前后一抽一抽（同 EntityHeroArrow 修复）。
			if (tickLife()) {
				spawnParticles();
			}
			return;
		}
		UUID uuid = UUID.fromString(getMatser());
		PlayerEntity player = this.getWorld().getPlayerByUuid(uuid);
		if (player == null) {
			this.discard();
			return;
		}
		// 位移/碰撞：对齐 EntityThrowable——先射线检测，命中则 onCollision 且
		// 不再位移；未命中则沿 velocity 推进并乘 0.99 空气阻力（水中 0.6，同）。
		HitResult hitResult = ProjectileUtil.getCollision(this,
				entity -> entity != this.getOwner() && entity.canHit()
						&& !(entity instanceof EntityStandBase));
		if (hitResult.getType() != HitResult.Type.MISS) {
			this.onCollision(hitResult);
			if (this.isRemoved()) {
				return;
			}
		} else {
			Vec3d v = this.getVelocity();
			this.setPosition(getX() + v.x, getY() + v.y, getZ() + v.z);
			this.setVelocity(v.multiply(this.isTouchingWater() ? 0.6 : 0.99));
		}
		List<LivingEntity> entities = this.getWorld().getEntitiesByClass(LivingEntity.class,
				this.getBoundingBox().expand(1), e -> e != this.getOwner() && !e.getUuid().equals(uuid)
						&& !(e instanceof EntityStandBase));
		for (LivingEntity entity : entities) {
			if (entity != getOwner() && player != null && entity != player) {
				if (!isDe()) {
					entity.setOnFireFor(3);
					entity.damage(DamageLoader.voidBreak(entity), 15f);
					if (NBTHelper.getEntityBoolean(entity, "huajiage.de")) {
						NBTHelper.setEntityBoolean(entity, "huajiage.de", false);
						entity.damage(DamageLoader.voidBreak(entity), 50f);
						//  EntityDragon 龙首特判（dragonPartHead 60 伤）暂裁剪
						player.heal(2f);
						for (int i = 0; i < 3; i++) {
							spawnLightning(entity.getX(), entity.getY(), entity.getZ());
						}
					}
				} else {
					NBTHelper.setEntityBoolean(entity, "huajiage.de", true);
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60));
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 2));
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60));
				}
			}
		}
		tickLife();
	}

	/** 寿命推进：满 180 tick 消亡。返回是否仍然存活。 */
	private boolean tickLife() {
		if (getLife() < 180) {
			setLife(getLife() + 1f);
			return true;
		}
		this.discard();
		return false;
	}

	private void spawnParticles() {
		double r = Math.random() - 0.5;
		if (!isDe()) {
			for (int i = 0; i < 3; i++) {
				this.getWorld().addParticle(ParticleTypes.FLAME, getX() + r, getY() + r, getZ() + r,
						(Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10);
			}
		} else {
			for (int i = 0; i < 3; i++) {
				this.getWorld().addParticle(ParticleTypes.SMOKE, getX() + r, getY() + r, getZ() + r,
						(Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10, (Math.random() - 0.5) / 10);
			}
		}
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			if (entityHit != null && entityHit != getOwner()
					&& !(entityHit instanceof EntityStandBase)) {
				if (!this.getWorld().isClient) {
					if (!(entityHit instanceof EntityFivePower)) {
						if (!isDe()) {
							entityHit.damage(DamageLoader.voidBreak(entityHit), 10f);
							this.getWorld().createExplosion(this, getX(), getY(), getZ(), 1f, false,
									World.ExplosionSourceType.NONE);
							if (NBTHelper.getEntityBoolean(entityHit, "huajiage.de")) {
								NBTHelper.setEntityBoolean(entityHit, "huajiage.de", false);
								entityHit.damage(DamageLoader.voidBreak(entityHit), 10f);
								if (getOwner() instanceof LivingEntity resolved) {
									resolved.heal(2f);
								}
								for (int i = 0; i < 3; i++) {
									spawnLightning(entityHit.getX(), entityHit.getY(), entityHit.getZ());
								}
							}
						} else {
							NBTHelper.setEntityBoolean(entityHit, "huajiage.de", true);
							if (entityHit instanceof LivingEntity living) {
								living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60));
								living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
								living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 2));
								living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60));
							}
						}
						// 命中实体后立即消散，避免停留在命中位置每 tick 重复触发
						this.discard();
					}
				}
				this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1f);
			}
		}
		if (result.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) result).getBlockPos();
			if (!this.getWorld().getBlockState(pos).getCollisionShape(this.getWorld(), pos).isEmpty()) {
				this.discard();
			}
		}
	}

	private void spawnLightning(double x, double y, double z) {
		if (this.getWorld().isClient) {
			return;
		}
		LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(this.getWorld());
		if (bolt == null) {
			return;
		}
		bolt.refreshPositionAndAngles(x, y, z, 0f, 0f);
		bolt.setCosmetic(true);
		this.getWorld().spawnEntity(bolt);
	}

	public float getLife() {
		return this.getDataTracker().get(LIFE);
	}

	public void setLife(float timeTick) {
		this.getDataTracker().set(LIFE, timeTick);
	}

	public boolean isDe() {
		return this.getDataTracker().get(DE);
	}

	public void setDe(boolean de) {
		this.getDataTracker().set(DE, de);
	}

	public String getMatser() {
		return this.getDataTracker().get(MASTER);
	}

	public void setMaster(String uuid) {
		this.getDataTracker().set(MASTER, uuid);
	}
}
