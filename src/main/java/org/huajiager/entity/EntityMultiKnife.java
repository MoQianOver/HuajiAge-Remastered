package org.huajiager.entity;

import org.huajiager.init.HuajiConstant;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * 多刃飞刀实体。
 *
 * 继承 EntityThrowable，无重力旋转飞行；命中实体造成范围连锁伤害（附近实体数量
 * 加成）与点燃（light 模式），命中方块后停驻直至寿命耗尽。
 * 用 setNoGravity(true) 实现无重力飞行。
 */
public class EntityMultiKnife extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}


	private static final String TAG_ROTATION = "rotation";
	private static final String TAG_PITCH = "pitch";
	private static final String TAG_LIFE = "life";
	private static final String TAG_EXTRA = "extra";
	private static final String TAG_LIGHT = "light";

	private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityMultiKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> PITCH = DataTracker.registerData(EntityMultiKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntityMultiKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> EXTRA = DataTracker.registerData(EntityMultiKnife.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> LIGHT = DataTracker.registerData(EntityMultiKnife.class,
			TrackedDataHandlerRegistry.BOOLEAN);

	public static final EntityType<EntityMultiKnife> TYPE = EntityType.Builder
			.<EntityMultiKnife>create((type, world) -> new EntityMultiKnife(type, world), SpawnGroup.MISC)
			.setDimensions(0.25f, 0.25f)
			// 跟踪范围按满寿命直线射程取值：初速 1.5 格/tick、每 tick ×0.99、life 360，全程约 146 格；
			// 单位是区块，默认 5 区块=80 格，飞刀越界后服务端停止跟踪并向客户端发销毁包，实体连同
			// 碰撞箱一起消失而服务端仍在飞行结算伤害，故取 10 区块=160 格覆盖全程。
			.maxTrackingRange(10)
			.trackingTickInterval(3)
			.build("huajiager:multi_knife");

	public EntityMultiKnife(EntityType<EntityMultiKnife> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	public EntityMultiKnife(World worldIn) {
		this(TYPE, worldIn);
	}

	public EntityMultiKnife(World worldIn, LivingEntity throwerIn) {
		this(worldIn);
		this.setOwner(throwerIn);
		this.setPosition(throwerIn.getX(), throwerIn.getEyeY() - 0.1, throwerIn.getZ());
		Vec3d look = throwerIn.getRotationVector();
		this.setVelocity(look.multiply(1.5));
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(ROTATION, 0F);
		tracker.startTracking(PITCH, 0F);
		tracker.startTracking(LIFE, 300F);
		tracker.startTracking(EXTRA, 0F);
		tracker.startTracking(LIGHT, false);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_ROTATION, getRotation());
		nbt.putFloat(TAG_PITCH, getKnifePitch());
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putFloat(TAG_EXTRA, getExtra());
		nbt.putBoolean(TAG_LIGHT, isLight());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setRotation(nbt.getFloat(TAG_ROTATION));
		setKnifePitch(nbt.getFloat(TAG_PITCH));
		setLife(nbt.getFloat(TAG_LIFE));
		setExtra(nbt.getFloat(TAG_EXTRA));
		setLight(nbt.getBoolean(TAG_LIGHT));
	}

	@Override
	public void tick() {
		super.tick();
		int extra = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(20)).size();
		if (getLife() > 0) {
			setLife(getLife() - 1);
		} else {
			this.discard();
			return;
		}
		Vec3d v = this.getVelocity();
		if (v.x == 0 && v.y == 0 && v.z == 0) {
			// 停驻状态： Entity#setRotation(yaw=getPitch(), pitch=getRotation())
			// setPitch/getPitch 现为 Entity 实体角度方法（自定义俯仰已改名 getKnifePitch）
			this.setYaw(getKnifePitch());
			this.setPitch(getRotation());
		} else {
			// ProjectileEntity.tick 基类只推事件/年龄，不做位移也不触发碰撞，
			// 必须手动 raycast 碰撞 + move 位移（参照 EmeraldBulletEntity/EntityHeroArrow），
			// 否则多刀停留原地不发射（表现为仅见原地粒子）。
			HitResult hitResult = ProjectileUtil.getCollision(this, e -> e != this.getOwner() && e.canHit());
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onCollision(hitResult);
				if (this.isRemoved()) {
					return;
				}
			} else {
				this.move(MovementType.SELF, v);
				// 低速贴脸兜底：getCollision 的射线长度等于当前速度向量，速度每 tick *0.99
				// 衰减后射线够不到方块表面会持续 MISS，刀贴脸停在表面外侧（悬浮）。move 被
				// 碰撞阻挡后，用固定长度短射线沿速度方向找表面，命中则停驻插地。
				if (this.horizontalCollision || this.verticalCollision) {
					Vec3d dir = v.lengthSquared() > 1.0E-8D ? v.normalize() : new Vec3d(0, -1, 0);
					HitResult nearHit = this.getWorld().raycast(new RaycastContext(this.getPos(),
							this.getPos().add(dir.multiply(1.0)),
							RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
					if (nearHit.getType() != HitResult.Type.MISS) {
						this.onCollision(nearHit);
						if (this.isRemoved()) {
							return;
						}
					}
				}
				//  EntityThrowable 每 tick 速度 *= drag(0.99) 逐渐减速
				this.setVelocity(v.multiply(0.99D));
			}
			if (isLight()) {
				double r1 = (Math.random() - 0.5) * 0.2;
				double r2 = (Math.random() - 0.5) * 0.2;
				double r3 = (Math.random() - 0.5) * 0.2;
				this.getWorld().addParticle(ParticleTypes.LAVA, getX() + r1, getY() + r2, getZ() + r3, r1, r2, r3);
			}
		}
		if (getOwner() != null) {
			// 仅在攻击者处于时停（THE_WORLD tag > 0）时将该实体数量同步为额外伤害系数
			if (NBTHelper.getEntityInteger(getOwner(), HuajiConstant.Tags.THE_WORLD) > 0) {
				setExtra(extra);
			}
		}
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			if (entityHit != null && entityHit != getOwner()) {
				if (!this.getWorld().isClient) {
					if (isLight()) {
						entityHit.damage(this.getDamageSources().thrown(this, getOwner()), 3f);
						entityHit.setOnFireFor(6);
						this.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 0.5f);
					}
					// 附带"时停伤害"（无击退/无闪避），以 thrown 伤害近似
					entityHit.damage(this.getDamageSources().thrown(this, getOwner()), getExtra() * 2 + 5f);
					this.discard();
				}
				this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1f);
			}
		}
		if (result.getType() == HitResult.Type.BLOCK) {
			// 悬浮根因修复：不要用 BlockPos.ofFloored(result.getPos()) 判断碰撞箱！
			// 对顶面命中，result.getPos().y 恰为顶面整数坐标，ofFloored 会取到表面上方的
			// 空气方块，getCollisionShape 判空导致整个停驻逻辑被跳过：实体停留在首次命中时
			// 的位置（地面上方约一个速度向量长度、0.1~1 格随机），每 tick 重复命中却什么都不做，
			// 视觉上就是"悬浮在方块表面上空"。raycast 用 ShapeType.COLLIDER 命中的方块必有
			// 碰撞箱，无需此检查，直接停驻即可。
			if (isLight()) {
				this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1f);
				this.setOnFire(false);
			} else {
				this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1f);
			}
			this.setVelocity(0, 0, 0);
			// 把实体中心移到射线与碰撞箱表面的交点（贴表面），下一帧进入停驻分支不再移动
			Vec3d hitPos = result.getPos();
			this.setPosition(hitPos.x, hitPos.y, hitPos.z);
		}
	}

	public float getRotation() {
		return this.getDataTracker().get(ROTATION);
	}

	public void setRotation(float rot) {
		this.getDataTracker().set(ROTATION, rot);
	}

	/**
	 * 自定义俯仰 DataTracker（非实体角度）。必须与 Entity#getPitch/setPitch 区分命名：
	 * 若沿用 getPitch/setPitch 会覆盖 Entity 实体角度方法，时停冻结/恢复等外部代码
	 * 调用 setPitch() 时会错误写入此 DataTracker，导致时停中刀尖朝向颠倒。
	 */
	public float getKnifePitch() {
		return this.getDataTracker().get(PITCH);
	}

	public void setKnifePitch(float rot) {
		this.getDataTracker().set(PITCH, rot);
	}

	public float getLife() {
		return this.getDataTracker().get(LIFE);
	}

	public void setLife(float timeTick) {
		this.getDataTracker().set(LIFE, timeTick);
	}

	public float getExtra() {
		return this.getDataTracker().get(EXTRA);
	}

	public void setExtra(float extra) {
		this.getDataTracker().set(EXTRA, extra);
	}

	public boolean isLight() {
		return this.getDataTracker().get(LIGHT);
	}

	public void setLight(boolean light) {
		this.getDataTracker().set(LIGHT, light);
	}
}
