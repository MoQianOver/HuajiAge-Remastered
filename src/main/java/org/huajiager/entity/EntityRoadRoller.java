package org.huajiager.entity;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.capability.IExposedData;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.messages.MessageDioHitClient;
import org.huajiager.stand.messages.MessageParticleGenerator;
import org.huajiager.util.NBTHelper;
import org.huajiager.util.ServerUtil;

import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * 压路机实体。
 *
 * 继承 EntityThrowable，bounding box 外扩 (2,1.5,2)，投掷物特性为“静止时无重力、
 * 运动时施加 0.06 重力”。命中/包围范围内实体造成 thrown 伤害并引发大爆炸。 * The World 时停联动与 Star Platinum 粒子/音效逻辑经 ServerUtil 广播。
 * 依赖 EntityStandBase 的排除判断（EntityStandBase 已）。
 */
public class EntityRoadRoller extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}


	private static final String TAG_ROTATION = "rotation_t";
	private static final String TAG_PITCH = "pitch_t";
	private static final String TAG_LIFE = "life_t";
	private static final String TAG_DAMAGE = "damage_t";
	private static final String TAG_EXTRA = "extra_t";
	private static final String TAG_TYPE = "type_t";

	private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> PITCH = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> DAMAGE = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> EXTRA = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<String> TYPE = DataTracker.registerData(EntityRoadRoller.class,
			TrackedDataHandlerRegistry.STRING);

	public static final EntityType<EntityRoadRoller> TYPE_ENTITY = EntityType.Builder
			.<EntityRoadRoller>create((type, world) -> new EntityRoadRoller(type, world), SpawnGroup.MISC)
			.setDimensions(1.0f, 1.0f)
			.build("huajiager:road_roller");

	public EntityRoadRoller(EntityType<EntityRoadRoller> type, World world) {
		super(type, world);
	}

	public EntityRoadRoller(World worldIn) {
		this(TYPE_ENTITY, worldIn);
	}

	public EntityRoadRoller(World worldIn, LivingEntity throwerIn) {
		this(worldIn);
		this.setOwner(throwerIn);
		this.setPosition(throwerIn.getX(), throwerIn.getEyeY() - 0.1, throwerIn.getZ());
		Vec3d look = throwerIn.getRotationVector();
		this.setVelocity(look.multiply(1.2));
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(ROTATION, 0F);
		tracker.startTracking(PITCH, 0F);
		tracker.startTracking(LIFE, 0F);
		tracker.startTracking(DAMAGE, 0F);
		tracker.startTracking(EXTRA, 0F);
		tracker.startTracking(TYPE, enumTYPE.ROAD_ROLLER.getName());
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_ROTATION, getRotation());
		nbt.putFloat(TAG_PITCH, getPitch());
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putFloat(TAG_DAMAGE, getDamage());
		nbt.putFloat(TAG_EXTRA, getExtra());
		nbt.putString(TAG_TYPE, getRollType());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setRotation(nbt.getFloat(TAG_ROTATION));
		setPitch(nbt.getFloat(TAG_PITCH));
		setLife(nbt.getFloat(TAG_LIFE));
		// 此处误将 TAG_DAMAGE 赋给 setLife，此处按正确的 setDamage 
		setDamage(nbt.getFloat(TAG_DAMAGE));
		setExtra(nbt.getFloat(TAG_EXTRA));
		setRollType(nbt.getString(TAG_TYPE));
	}

	@Override
	public void tick() {
		super.tick();
		//  getGravityVelocity：速度为 0 时无重力，移动时 0.06 重力。
		Vec3d v = this.getVelocity();
		if (v.x == 0 && v.y == 0 && v.z == 0) {
			this.setNoGravity(true);
		} else {
			this.setNoGravity(false);
			v = v.add(0, -0.06, 0);
			this.setVelocity(v);
		}

		// 手动推进：ProjectileEntity.tick 基类不自动移动实体（EntityThrowable.
		// 内部自带 move(motion) + 重力），若不 push，压路机只会停在出生点附近原地打转，
		// 对地形毫无反应，只有迎面恰好扫到贴近生物才触发——即"对地面右键不发射、只对生物有效"。
		// 这里逐 tick 沿速度前进，还原投掷行为。
		double nx = getX() + v.x;
		double ny = getY() + v.y;
		double nz = getZ() + v.z;
		this.setPosition(nx, ny, nz);

		// 方块碰撞检测：ProjectileEntity.tick 基类不会自动对 Block 调用 onCollision，
		// 原实现仅按包围盒探测实体。这里 raycast 上一位置→当前位置，捕捉飞行路径上
		// 撞到的方块并触发爆炸（onCollision 的 BLOCK 分支）。
		if (!this.getWorld().isClient) {
			Vec3d start = new Vec3d(nx - v.x, ny - v.y, nz - v.z);
			Vec3d end = this.getPos();
			BlockHitResult blockHit = this.getWorld().raycast(new RaycastContext(start, end,
					RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, this));
			if (blockHit != null && blockHit.getType() == BlockHitResult.Type.BLOCK) {
				this.onCollision(blockHit);
				return;
			}
		}

		int extra = NBTHelper.getEntityInteger(this, "huajiage.dio_push");
		List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(2, 1.5, 2));
		if (list != null) {
			for (Entity entity : list) {
				// 排除投掷物、替身实体（原行为）与己方/友军（召唤者、五五开头盔翅膀、己方替身），
				// 避免"丢出压路机/黑色轿车后爆炸误伤自己的翅膀并炸到自己"。
				if (entity != null && !(entity instanceof ProjectileEntity)
						&& !(entity instanceof EntityStandBase) && !isFriendlyEntity(entity)) {
					if (!this.getWorld().isClient) {
						if (entity instanceof LivingEntity) {
							//  EntityDragon 龙首特判（dragonPartHead + 爆炸伤害）暂裁剪为普通 thrown 伤害
							entity.damage(this.getDamageSources().thrown(this, getOwner()), getDamage() + getExtra() * 2);
						}
						this.explodeExcludingFriendly();
						this.discard();
					}
				}
			}
		}
		if (getLife() > 0) {
			setLife(getLife() - 1);
		} else {
			this.discard();
		}
		if (getOwner() instanceof LivingEntity owner) {
			boolean isStar = false;
			IExposedData data = StandUtil.getStandData(owner);
			if (data != null && data.getStand().equals(StandLoader.STAR_PLATINUM.getName())) {
				isStar = true;
			}
			// 时停标记统一以 HuajiConstant.Tags.THE_WORLD（"huajiager.the_world"，
			// 由 TimeStopHelper.setTimeStop 写入、EventTimeStop 递减）为准。			// 此前残留旧 MOD ID 硬编码 "huajiage.the_world"（少一个 r），
			// 与服务端真正写入的 key 不一致，导致时停中 dio_push 结算分支永不触发。
			if (NBTHelper.getEntityInteger(owner, HuajiConstant.Tags.THE_WORLD) > 0) {
				if (extra > getExtra()) {
					Vec3d targetPosition = owner.getPos();
					MessageDioHitClient msg1 = new MessageDioHitClient(targetPosition, false);
					MessageDioHitClient msg2 = new MessageDioHitClient(targetPosition, true);
					if (owner instanceof PlayerEntity player && NBTHelper.getEntityInteger(player, "huajiage.dio_flag") == 0 && !isStar) {
						ServerUtil.sendPacketToNearbyPlayersStand(player, msg1);
						NBTHelper.setEntityInteger(player, "huajiage.dio_flag", 180);
					}
					if (owner instanceof PlayerEntity player
							&& NBTHelper.getEntityInteger(player, "huajiage.dio_flag") < 140
							&& !isStar
							&& NBTHelper.getEntityInteger(player, "huajiage.dio_flag") > 0) {
						ServerUtil.sendPacketToNearbyPlayersStand(player, msg2);
					}
					if (isStar) {
						MessageParticleGenerator particle = new MessageParticleGenerator(targetPosition,
								"minecraft:firework", 60, 5, 1);
						ServerUtil.sendPacketToNearbyPlayers(owner, particle);
						HuajiSoundPlayer.playToNearbyClient(owner, SoundLoader.STAND_STAR_PLATINUM_REPEAT_1, 1f);
						owner.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 60));
					}
					setExtra(extra);
				}
			}
		}
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			// 命中判定同样排除替身实体（原行为）与己方/友军（召唤者、五五开头盔翅膀、己方替身）。
			if (entityHit != null && !(entityHit instanceof EntityStandBase) && !isFriendlyEntity(entityHit)) {
				if (!this.getWorld().isClient) {
					if (entityHit instanceof LivingEntity) {
						entityHit.damage(this.getDamageSources().thrown(this, getOwner()), getDamage() + getExtra() * 2);
					}
					this.explodeExcludingFriendly();
					this.discard();
				}
			}
		} else {
			this.explodeExcludingFriendly();
			this.discard();
		}
	}

	/**
	 * 己方/友军判定：召唤者自身、其五五开头盔翅膀实体（EntityLordLuWing.getOwner() 匹配宿主）、
	 * 其替身实体（EntityStandBase.getUser() 匹配宿主，与 EntityOrgaHairKnife 同口径）。
	 * 爆炸结算与碰撞检测排除这些实体，避免"丢出压路机/黑色轿车后爆炸误伤自己的翅膀并炸到自己"。
	 */
	private boolean isFriendlyEntity(Entity target) {
		if (target == null) {
			return false;
		}
		Entity owner = this.getOwner();
		if (target == owner) {
			return true;
		}
		if (target instanceof EntityLordLuWing wing) {
			return wing.getOwner() == owner;
		}
		if (target instanceof EntityStandBase stand) {
			return stand.getUser() == owner;
		}
		return false;
	}

	/**
	 * 爆炸结算：复刻原版 createExplosion 的实体伤害/击退公式（KEEP 不破坏方块），
	 * 但在实体筛选中排除己方/友军（召唤者、翅膀、己方替身）。伤害源仍为爆炸默认伤害源
	 * （causer=压路机自身），对敌人的伤害与击退与原版完全一致；随后发送爆炸 S2C 包
	 * 驱动客户端音效/爆炸粒子/预测击退（等效 ServerWorld.createExplosion 的客户端表现）。
	 */
	private void explodeExcludingFriendly() {
		World world = this.getWorld();
		if (world.isClient) {
			return;
		}
		float power = getExtra() > 5 ? 4f : 2f;
		double x = getX();
		double y = getY();
		double z = getZ();
		Explosion explosion = new Explosion(world, this, x, y, z, power, false, Explosion.DestructionType.KEEP);
		float q = power * 2.0F;
		int r = MathHelper.floor(x - (double) q - 1.0D);
		int s = MathHelper.floor(x + (double) q + 1.0D);
		int t = MathHelper.floor(y - (double) q - 1.0D);
		int u = MathHelper.floor(y + (double) q + 1.0D);
		int v = MathHelper.floor(z - (double) q - 1.0D);
		int w = MathHelper.floor(z + (double) q + 1.0D);
		List<Entity> list = world.getOtherEntities(this, new Box(r, t, v, s, u, w));
		Vec3d center = new Vec3d(x, y, z);
		for (Entity entity : list) {
			if (entity.isImmuneToExplosion() || isFriendlyEntity(entity)) {
				continue;
			}
			double dist = Math.sqrt(entity.squaredDistanceTo(center)) / (double) q;
			if (dist > 1.0D) {
				continue;
			}
			double dx = entity.getX() - x;
			double dy = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - y;
			double dz = entity.getZ() - z;
			double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (len == 0.0D) {
				continue;
			}
			dx /= len;
			dy /= len;
			dz /= len;
			double exposure = Explosion.getExposure(center, entity);
			double impact = (1.0D - dist) * exposure;
			entity.damage(explosion.getDamageSource(),
					(float) ((int) ((impact * impact + impact) / 2.0D * 7.0D * (double) q + 1.0D)));
			double knockback = impact;
			if (entity instanceof LivingEntity living) {
				knockback = ProtectionEnchantment.transformExplosionKnockback(living, impact);
			}
			Vec3d vel = new Vec3d(dx * knockback, dy * knockback, dz * knockback);
			entity.setVelocity(entity.getVelocity().add(vel));
			if (entity instanceof PlayerEntity player
					&& !player.isSpectator() && !(player.isCreative() && player.getAbilities().flying)) {
				explosion.getAffectedPlayers().put(player, vel);
			}
		}
		// 复刻 ServerWorld.createExplosion：向所有玩家发送爆炸 S2C 包（携带每个玩家各自的击退向量），
		// 客户端据此播放爆炸音效/粒子并施加本地预测击退；己方玩家不在 affectedPlayers 中则收到零向量。
		if (world instanceof ServerWorld serverWorld) {
			for (ServerPlayerEntity player : serverWorld.getPlayers()) {
				player.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, power,
						new ArrayList<>(explosion.getAffectedBlocks()),
						explosion.getAffectedPlayers().getOrDefault(player, Vec3d.ZERO)));
			}
		}
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

	public float getDamage() {
		return this.getDataTracker().get(DAMAGE);
	}

	public void setDamage(float damage) {
		this.getDataTracker().set(DAMAGE, damage);
	}

	public float getExtra() {
		return this.getDataTracker().get(EXTRA);
	}

	public void setExtra(float damage) {
		this.getDataTracker().set(EXTRA, damage);
	}

	public String getRollType() {
		return this.getDataTracker().get(TYPE);
	}

	public void setRollType(String type) {
		this.getDataTracker().set(TYPE, type);
	}

	public enum enumTYPE {
		ROAD_ROLLER("road_roller"),
		CAR("car");

		enumTYPE(String name) {
			this.name = name;
		}

		String name;

		public String getName() {
			return name;
		}
	}
}
