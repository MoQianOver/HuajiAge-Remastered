package org.huajiager.entity;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 第二卷轴·追杀弹， 。
 *
 * 继承 EntityShulkerBullet 并保留其自动追踪逻辑；Fabric 1.20.1 侧简化为
 * ProjectileEntity 直线追踪方案：每 tick 朝目标方向匀速飞行，命中任何生物即触发
 * SECON既秒杀（genericKill 大额伤害），掉落虚空镜片后自行销毁。
 */
public class EntitySecondFoil extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}

	private static final String TAG_TARGET = "target";
	private static final String TAG_OWNER = "owner";

	private static final TrackedData<String> TARGET = DataTracker.registerData(EntitySecondFoil.class,
			TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> OWNER = DataTracker.registerData(EntitySecondFoil.class,
			TrackedDataHandlerRegistry.STRING);

	public static final EntityType<EntitySecondFoil> TYPE = EntityType.Builder
			.<EntitySecondFoil>create((type, world) -> new EntitySecondFoil(type, world), SpawnGroup.MISC)
			.setDimensions(1.0f, 1.0f)
			.build("huajiager:second_foil");

	public EntitySecondFoil(EntityType<EntitySecondFoil> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	public EntitySecondFoil(World world) {
		this(TYPE, world);
	}

	public EntitySecondFoil(World world, LivingEntity owner, LivingEntity target) {
		this(world);
		this.setOwner(owner);
		if (target != null) {
			setTarget(target.getUuid().toString());
		}
		if (owner != null) {
			// 生成在丢出者/使用者面前并给予初速，避免出生在世界原点不动
			Vec3d look = owner.getRotationVec(1.0f);
			this.setPosition(
					owner.getX() + look.x * 0.6,
					owner.getEyeY() - 0.3 + look.y * 0.6,
					owner.getZ() + look.z * 0.6);
			this.setVelocity(look.multiply(0.4d));
		}
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(TARGET, "");
		tracker.startTracking(OWNER, "");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString(TAG_TARGET, getTarget());
		nbt.putString(TAG_OWNER, getOwnerUuid());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setTarget(nbt.getString(TAG_TARGET));
		setOwnerUuid(nbt.getString(TAG_OWNER));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved()) {
			return;
		}
		if (this.getWorld().isClient) {
			// 客户端本地推进：对齐 EntityShulkerBullet.tick 结构——追踪计算与碰撞
			// 只在服务端执行，但位移推进（）客户端也必须做，用服务端同步来的
			// velocity 逐 tick 连续位移。此前客户端完全 return 只靠 20Hz 位置包跳变渲染，
			// 慢速追踪（速度仅 0.6）下每个同步间隔的"阶梯感"被放大，表现为一卡一卡。
			Vec3d v = this.getVelocity();
			this.setPosition(getX() + v.x, getY() + v.y, getZ() + v.z);
			return;
		}
		// 目标缓存：只有目标死亡/移除/变为 owner 或替身时才重新扫描，平时零扫描
		if (cachedTarget == null || cachedTarget.isRemoved() || !cachedTarget.isAlive()
				|| cachedTarget == this.getOwner() || cachedTarget instanceof EntityStandBase) {
			cachedTarget = findTarget();
		}
		if (cachedTarget != null) {
			// 渐进转向：不瞬间把速度掰向目标（那会形成折线弹道，慢速下视觉一顿一顿），
			// 而是每 tick 把当前速度向目标方向融合 35%，轨迹呈平滑曲线追踪
			Vec3d desired = cachedTarget.getPos().subtract(this.getPos()).normalize().multiply(0.6d);
			this.setVelocity(this.getVelocity().lerp(desired, 0.35d));
		} else {
			// 无目标时按原有速度惯性与重力自然下坠
			this.setVelocity(this.getVelocity().x, this.getVelocity().y - 0.04d, this.getVelocity().z);
		}
		// 位移/碰撞：对齐 EntityFivePower 已验证姿势——ProjectileEntity.tick 不自带位移，
		// 先按当前 velocity 做射线检测，命中则 onCollision（且不再位移），未命中手动沿速度推进，
		// 否则弹射物只会停留在出生点不动。
		HitResult hitResult = ProjectileUtil.getCollision(this,
				entity -> entity != this.getOwner() && entity.canHit()
						&& !(entity instanceof EntityStandBase));
		if (hitResult.getType() != HitResult.Type.MISS) {
			this.onCollision(hitResult);
		} else {
			Vec3d v = this.getVelocity();
			this.setPosition(getX() + v.x, getY() + v.y, getZ() + v.z);
		}
	}

	/** 统一碰撞谓词：super.tick()（ProjectileEntity 内部碰撞）与本类手动碰撞共用，排除释放者与替身。 */
	@Override
	protected boolean canHit(Entity entity) {
		return entity != this.getOwner() && entity.canHit()
				&& !(entity instanceof EntityStandBase);
	}

	/** 目标实体缓存：避免每 tick 全图 64 格扫描 + UUID 字符串比较拖慢服务端 tick（追踪一卡一卡的主因）。 */
	private LivingEntity cachedTarget;

	private LivingEntity findTarget() {
		if (getTarget().isEmpty()) {
			return null;
		}
		List<LivingEntity> list = this.getWorld().getEntitiesByClass(LivingEntity.class,
				this.getBoundingBox().expand(64), e -> true);
		for (LivingEntity e : list) {
			if (e.getUuid().toString().equals(getTarget())) {
				// 防御：即使目标被误设为释放者或替身也放弃锁定，避免弹射物跟踪释放者/替身
				if (e == this.getOwner() || e instanceof EntityStandBase) {
					return null;
				}
				return e;
			}
		}
		return null;
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			if (entityHit instanceof LivingEntity && entityHit != this.getOwner()
					&& !(entityHit instanceof EntityStandBase)) {
				World world = this.getWorld();
				if (!world.isClient) {
					entityHit.damage(DamageLoader.second(entityHit), 1000f);
					entityHit.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
					ItemEntity drop = new ItemEntity(world, entityHit.getX(), entityHit.getY(), entityHit.getZ(),
							new ItemStack(ItemLoader.expendedView));
					world.spawnEntity(drop);
				}
				this.discard();
			} else if (entityHit instanceof EntityStandBase) {
				// 替身展示实体：不误杀，直接销毁弹射物避免卡在原地反复碰撞
				this.discard();
			}
		} else {
			this.discard();
		}
	}

	public String getTarget() {
		return this.getDataTracker().get(TARGET);
	}

	public void setTarget(String uuid) {
		this.getDataTracker().set(TARGET, uuid);
	}

	public String getOwnerUuid() {
		return this.getDataTracker().get(OWNER);
	}

	public void setOwnerUuid(String uuid) {
		this.getDataTracker().set(OWNER, uuid);
	}
}
