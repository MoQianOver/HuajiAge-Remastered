package org.huajiager.entity;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.DamageLoader;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 英雄之箭实体。
 *
 * 继承 EntityThrowable，命中产生 50 格威力的大范围爆炸（是否破坏地形由
 * ConfigHuaji.Huaji.heroExplode 控制）。 * 保留默认重力（约等于 throwable 下落），飞行中客户端播洒熔岩 / 烟花粒子。
 */
public class EntityHeroArrow extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}


	/**
	 * trackingTickInterval(1)：满蓄力（speed=6）时每 tick 位移约 0.9 格；本实体客户端
	 * 已移除本地推进（避免双重驱动），渲染位置完全依赖服务端同步。EntityType 默认
	 * trackingTickInterval=3，服务端每 3 tick 才发一次位置包，客户端实体 3 tick 内原地
	 * 不动、收包后一次瞬移约 2.7 格，视觉上"一闪一闪（消失又恢复）"；非满蓄力走普通
	 * ArrowEntity（PersistentProjectileEntity，客户端本地推进 + 位置包特判）无此问题。
	 * 设为 1 后服务端每 tick 同步位置，客户端插值平滑。
	 */
	public static final EntityType<EntityHeroArrow> TYPE = EntityType.Builder
			.<EntityHeroArrow>create((type, world) -> new EntityHeroArrow(type, world), SpawnGroup.MISC)
			.setDimensions(0.5f, 0.5f)
			.trackingTickInterval(1)
			.build("huajiager:hero_arrow");

	public EntityHeroArrow(EntityType<EntityHeroArrow> type, World world) {
		super(type, world);
	}

	public EntityHeroArrow(World worldIn) {
		this(TYPE, worldIn);
	}

	public EntityHeroArrow(World worldIn, LivingEntity throwerIn) {
		this(worldIn);
		this.setOwner(throwerIn);
		// 出生点放在发射者眼睛高度（沿准星的前置弹道起点）。速度不再在此构造里预设：
		// 统一由 ItemHeroBow 走 ProjectileEntity.setVelocity(shooter,pitch,yaw,...) 精确
		// 按玩家视角给出，避免构造里的手写朝向与服务端实际准星出现偏差/时序叠加。
		this.setPosition(throwerIn.getX(), throwerIn.getEyeY() - 0.1, throwerIn.getZ());
	}

	@Override
	protected void initDataTracker() {
		// ProjectileEntity 无必注册数据，本实体无自定义可同步字段，保持空实现
		// （1.20.1 Entity.initDataTracker 为抽象方法，必须显式覆盖）
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient) {
			// 拖尾：每 3 tick 一颗烟花星点。原实现每帧播 5 个 LAVA 火苗 + 1 个烟花，
			// 视觉上像"一直冒火"，这里把火苗去掉、降频为稀疏星点拖尾。
			// 客户端的影分身实体不参与物理：位移/碰撞/引爆全部由服务端权威推进，
			// 客户端只按网络同步的位置渲染。此前客户端也每 tick  本地推进，
			// 与服务端实体位置覆盖叠加成"双重驱动"，表现为箭从准星外偏移射出、
			// 抖动、飞一段才被拉回准星弹道——这里彻底移除客户端推进。
			if (this.age % 3 == 0) {
				this.getWorld().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(), 0, 0, 0.03);
			}
			return;
		}
		//  PersistentProjectileEntity 在首次 tick 按当前速度设置 yaw/pitch（朝向），
		// 之后的 tick 只同步 prev*。若不设置朝向，渲染时模型保持默认世界朝向，
		// 表现为"箭不是对着飞行方向，是斜着的"。防御：速度为零（如时停冻结
		// setVelocity(ZERO) 后）时不重算方向，避免 atan2(0,0) 把朝向坍缩成默认。
		// 注：ItemHeroBow 发射时已立即初始化朝向并写 prev*，正常情况下本分支不触发。
		if (this.prevYaw == 0.0F && this.prevPitch == 0.0F
				&& !this.getVelocity().equals(Vec3d.ZERO)) {
			Vec3d v = this.getVelocity();
			this.setYaw((float) (MathHelper.atan2(v.x, v.z) * 57.2957763671875D));
			this.setPitch((float) (MathHelper.atan2(v.y, v.horizontalLength()) * 57.2957763671875D));
			this.prevYaw = this.getYaw();
			this.prevPitch = this.getPitch();
		}
		// 重力： EntityThrowable.getGravityVelocity()≈0.03，尊重 hasNoGravity 标记
		if (!this.hasNoGravity()) {
			Vec3d v = this.getVelocity();
			this.setVelocity(v.x, v.y - 0.03, v.z);
		}
		// ProjectileEntity.tick 基类只做事件/年龄推进，既不做位移也不会对方块或实体
		// 调用 onCollision。必须手动 raycast 碰撞 + move 位移，箭才会"飞出去"并命中
		// 生物触发爆炸（服务端协同 discard，客户端由服务端同步移除）。
		HitResult hitResult = ProjectileUtil.getCollision(this,
				entity -> entity != this.getOwner() && entity.canHit());
		if (hitResult.getType() != HitResult.Type.MISS) {
			this.onCollision(hitResult);
			return;
		}
		// 近炸引信：箭即使不直接命中生物，飞近后凭 50 格威力的大爆炸也能杀伤
		// 周围生物。此处要求"先沿准星直线飞出去再追踪"：飞行初期（前 5 tick）
		// 不启用贴身引爆，之后也只在真正贴身（0.65 格包围盒）时才引燃，避免
		// 一射出就自动追踪附近敌人提前爆炸。
		if (this.age > 5) {
			List<Entity> nearby = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.65),
					e -> e != this.getOwner() && e.canHit());
			if (!nearby.isEmpty()) {
				this.onCollision(new EntityHitResult(nearby.get(0)));
				return;
			}
		}
		// 本模板 yarn 映射无 MoverType，逐 tick 沿 velocity 手动推进
		Vec3d v = this.getVelocity();
		this.setPosition(getX() + v.x, getY() + v.y, getZ() + v.z);
	}

	@Override
	protected void onCollision(HitResult result) {
		if (!this.getWorld().isClient) {
			Entity owner = this.getOwner();
			this.getWorld().createExplosion(owner, getX(), getY(), getZ(), 50f,
					ConfigHuaji.Huaji.heroExplode, World.ExplosionSourceType.NONE);
			// 真名解放：50 格威力大爆炸会波及发射者本人（createExplosion 的伤害以玩家为
			// 中心的覆盖范围极大），ItemHeroBow 的自伤已保底 1 点生命，但爆炸补刀仍会致死。
			// 此处对发射者做"留一滴血"兜底：爆炸后若持有者被打死，立即恢复至 1 点生命。
			if (owner instanceof PlayerEntity player && !player.isAlive()) {
				player.setHealth(1f);
				player.deathTime = 0;
			}
			this.discard();
		}
	}
}
