package org.huajiager.entity;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 灰色波发符实体， 。
 *
 * 继承 EntityThrowable，无重力、以 NBT 注入动量驱动；命中造成按攻击者当前生命值
 * 计算的巨额伤害（20 - 当前生命 的 2 倍加成），若攻击者为镇魂版灰色波（ORGA_REQUIEM
 * 且持有 potionRequiem），则命中目标被标记为镇魂目标并爆炸。
 */
public class EntityOrgaHairKnife extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}


	private static final String TAG_ROTATION = "rotation";
	private static final String TAG_PITCH = "pitch";
	private static final String TAG_LIFE = "life";
	private static final String TAG_STAY = "stay";
	private static final String TAG_DAMAGE = "damage";
	private static final String TAG_EXTRA = "extra";

	private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> PITCH = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> STAY = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> DAMAGE = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> EXTRA = DataTracker.registerData(EntityOrgaHairKnife.class,
			TrackedDataHandlerRegistry.FLOAT);

	public static final EntityType<EntityOrgaHairKnife> TYPE = EntityType.Builder
			.<EntityOrgaHairKnife>create((type, world) -> new EntityOrgaHairKnife(type, world), SpawnGroup.MISC)
			.setDimensions(0.25f, 0.25f)
			.build("huajiager:orga_hair_knife");

	public EntityOrgaHairKnife(EntityType<EntityOrgaHairKnife> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	public EntityOrgaHairKnife(World worldIn) {
		this(TYPE, worldIn);
	}

	public EntityOrgaHairKnife(World worldIn, LivingEntity throwerIn) {
		this(worldIn);
		this.setOwner(throwerIn);
		Vec3d look = throwerIn.getRotationVector();
		// 生成点沿视线前移 0.6 格，避免实体与玩家碰撞箱重叠被推出，导致飞行路径偏离准星
		this.setPosition(throwerIn.getX() + look.x * 0.6, throwerIn.getEyeY() - 0.1 + look.y * 0.6,
				throwerIn.getZ() + look.z * 0.6);
		this.setVelocity(look.multiply(1.5));
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(ROTATION, 0F);
		tracker.startTracking(PITCH, 0F);
		tracker.startTracking(LIFE, 0F);
		tracker.startTracking(STAY, 0F);
		tracker.startTracking(DAMAGE, 0F);
		tracker.startTracking(EXTRA, 0F);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_ROTATION, getRotation());
		nbt.putFloat(TAG_PITCH, getPitch());
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putFloat(TAG_STAY, getStay());
		nbt.putFloat(TAG_DAMAGE, getDamage());
		nbt.putFloat(TAG_EXTRA, getExtra());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setRotation(nbt.getFloat(TAG_ROTATION));
		setPitch(nbt.getFloat(TAG_PITCH));
		setLife(nbt.getFloat(TAG_LIFE));
		setStay(nbt.getFloat(TAG_STAY));
		setDamage(nbt.getFloat(TAG_DAMAGE));
		setExtra(nbt.getFloat(TAG_EXTRA));
	}

	@Override
	public void tick() {
		super.tick();
		if (getLife() > 0) {
			if (getStay() <= 0) {
				setLife(getLife() - 1);
			} else {
				setStay(getStay() - 1);
			}
		} else {
			this.discard();
			return;
		}

		// 从实体 NBT 读取注入的动量（MOTION_X/Y/Z），Fabric 侧沿用 NBTHelper
		if (getStay() > 0) {
			this.setVelocity(0, 0, 0);
		} else {
			Vec3d v = new Vec3d(NBTHelper.getEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_X.getTag()),
					NBTHelper.getEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_Y.getTag()),
					NBTHelper.getEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_Z.getTag()));
			if (v.length() > 0) {
				this.setVelocity(v.x, v.y, v.z);
				NBTHelper.setEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_X.getTag(), 0);
				NBTHelper.setEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_Y.getTag(), 0);
				NBTHelper.setEntityFloat(this, EmeraldBulletEntity.TAGS_ENTITY.MOTION_Z.getTag(), 0);
			}
		}

		Vec3d vel = this.getVelocity();
		if (vel.x == 0 && vel.y == 0 && vel.z == 0) {
			this.setYaw(getPitch());
			this.setPitch(getRotation());
		} else {
			// 旋转动画：每 tick 自增，配合渲染矩阵让飞刀竖直翻滚着飞出
			setRotation(getRotation() + 8f);
			// 手动 raycast 碰撞 + move（参照 EmeraldBulletEntity：基类不做位移/碰撞）
			// 排除发射者本人及其所属替身：替身（EntityStandBase）是独立实体而非 owner，
			// 仅按 owner 排除时头屑会命中自己召出的替身
			HitResult hitResult = ProjectileUtil.getCollision(this,
					e -> e.canHit() && !isFriendlyEntity(e));
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onCollision(hitResult);
				if (this.isRemoved()) {
					return;
				}
			} else {
				this.move(MovementType.SELF, vel);
				//  EntityThrowable 每 tick 速度 *= drag(0.99) 逐渐减速
				this.setVelocity(vel.multiply(0.99D));
			}
			double r1 = (Math.random() - 0.5) * 0.2;
			double r2 = (Math.random() - 0.5) * 0.2;
			double r3 = (Math.random() - 0.5) * 0.2;
			if (r1 > 0.05) {
				this.getWorld().addParticle(ParticleTypes.LAVA, getX() + r1, getY() + r2, getZ() + r3, r1, r2, r3);
			}
		}
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			// 与 raycast predicate 保持同口径：命中己方替身不结算伤害
			if (entityHit != null && !isFriendlyEntity(entityHit)) {
				if (!this.getWorld().isClient) {
					LivingEntity thrower = (LivingEntity) getOwner();
					if (thrower != null) {
						float extra = 0;
						float health = thrower.getHealth();
						float hp = 20 - health;
						if (hp > 0) {
							extra = hp * 2;
						}
						entityHit.damage(DamageLoader.hopeHit(entityHit), getDamage() + extra);
						StandBase stand = StandUtil.getType(thrower);
						if (stand != null && stand.equals(StandLoader.ORGA_REQUIEM) && thrower instanceof PlayerEntity
								&& thrower.hasStatusEffect(PotionLoader.potionRequiem)) {
							NBTHelper.setEntityInteger(entityHit, HuajiConstant.Tags.REQUIEM, 60);
							NBTHelper.setEntityString(entityHit, HuajiConstant.Tags.PLAYER_NAME,
									thrower.getName().getString());
						}
					}
					this.discard();
					this.getWorld().createExplosion(this, getX(), getY(), getZ(), 0.5f, false,
							World.ExplosionSourceType.NONE);
				}
				this.playSound(SoundLoader.ORGA_REQUIEM_PROTECT, 1.0F, 1f);
			}
		}
		if (result.getType() == HitResult.Type.BLOCK) {
			if (!this.getWorld().isClient) {
				BlockPos pos = ((BlockHitResult) result).getBlockPos();
				if (!this.getWorld().getBlockState(pos).getCollisionShape(this.getWorld(), pos).isEmpty()) {
					this.discard();
					this.getWorld().createExplosion(this, getX(), getY(), getZ(), 0.5f, false,
							World.ExplosionSourceType.NONE);
				}
			}
		}
	}

	/**
	 * 是否为"己方目标"（发射者本人或其所属替身），true 表示应排除、不结算命中。
	 * 替身由 EntityStandBase.getUser() 定位宿主；宿主已下线/丢失时 getUser() 返回 null，
	 * 此时替身视为非己方（可命中）。
	 */
	private boolean isFriendlyEntity(Entity target) {
		if (target == this.getOwner()) {
			return true;
		}
		if (target instanceof EntityStandBase stand) {
			return stand.getUser() == this.getOwner();
		}
		return false;
	}

	public float getRotation() {
		return this.getDataTracker().get(ROTATION);
	}

	public void setRotation(float rot) {
		this.getDataTracker().set(ROTATION, rot);
	}

	public float getPitch() {
		return this.getDataTracker().get(PITCH);
	}

	public void setPitch(float rot) {
		this.getDataTracker().set(PITCH, rot);
	}

	public float getLife() {
		return this.getDataTracker().get(LIFE);
	}

	public void setLife(float timeTick) {
		this.getDataTracker().set(LIFE, timeTick);
	}

	public float getStay() {
		return this.getDataTracker().get(STAY);
	}

	public void setStay(float stay) {
		this.getDataTracker().set(STAY, stay);
	}

	public float getDamage() {
		return this.getDataTracker().get(DAMAGE);
	}

	public void setDamage(float damage) {
		this.getDataTracker().set(DAMAGE, damage);
	}

	public float getExtra() {
		return this.getDataTracker().get(EXTRA);
	}

	public void setExtra(float extra) {
		this.getDataTracker().set(EXTRA, extra);
	}
}
