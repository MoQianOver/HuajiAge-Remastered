package org.huajiager.entity;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.util.HAMathHelper;
import org.huajiager.util.NBTHelper;

import net.minecraft.block.BlockState;
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
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 绿宝石弹幕实体。
 *
 * 继承 ProjectileEntity（无重力用 setNoGravity 代替
 * getGravityVelocity 返回 0）。运动由 NBT 注入的 MOTION_X/Y/Z 驱动 + 锁定追踪目标修正。 * 碰撞时产生间接爆炸伤害并播放玻璃碎裂音效。
 */
public class EmeraldBulletEntity extends ProjectileEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}

    private static final String TAG_ROTATION = "rotation";
    private static final String TAG_ROTATION_RANDOM = "rotation_forward";
    private static final String TAG_PITCH = "pitch";
    private static final String TAG_LIFE = "life";
    private static final String TAG_DAMAGE = "damage";
    private static final String TAG_STAY = "stay";
    private static final String TAG_HUGE = "huge";
    private static final String TAG_TYPE = "type";
    private static final String TAG_TARGET = "target";

    private static final TrackedData<Float> ROTATION = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROTATION_RANDOM = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> PITCH = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> LIFE = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> STAY = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HUGE = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> BULLET_TYPE = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> TARGET = DataTracker.registerData(EmeraldBulletEntity.class,
            TrackedDataHandlerRegistry.STRING);

    public static final EntityType<EmeraldBulletEntity> TYPE = EntityType.Builder
            .<EmeraldBulletEntity>create((type, world) -> new EmeraldBulletEntity(type, world), SpawnGroup.MISC)
            .setDimensions(0.25f, 0.25f)
            .build("huajiager:emerald_bullet");

    public EmeraldBulletEntity(EntityType<EmeraldBulletEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public EmeraldBulletEntity(World worldIn) {
        this(TYPE, worldIn);
    }

    public EmeraldBulletEntity(World worldIn, LivingEntity throwerIn) {
        this(worldIn);
        this.setOwner(throwerIn);
        // useHuajiSplash 开启时：法皇攻击态/技能发射的翡翠弹幕贴图替换为滑稽物品贴图
        // （渲染端 resolveStack 按 bulletType 取物品渲染），关闭时保持默认绿宝石贴图。
        if (ConfigHuaji.Stands.useHuajiSplash) {
            setBulletType("huajiager:huaji");
        }
    }

    @Override
    protected void initDataTracker() {
        DataTracker tracker = this.getDataTracker();
        tracker.startTracking(ROTATION, 0F);
        tracker.startTracking(ROTATION_RANDOM, 0F);
        tracker.startTracking(PITCH, 0F);
        tracker.startTracking(LIFE, 0F);
        tracker.startTracking(DAMAGE, 5F);
        tracker.startTracking(STAY, 0F);
        tracker.startTracking(HUGE, false);
        tracker.startTracking(BULLET_TYPE, "emerald");
        tracker.startTracking(TARGET, "a723e52d-a1b5-4121-838b-65fa63bfc161");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat(TAG_ROTATION, getRotation());
        nbt.putFloat(TAG_ROTATION_RANDOM, getRotationRandom());
        nbt.putFloat(TAG_PITCH, getPitch());
        nbt.putFloat(TAG_LIFE, getLife());
        nbt.putFloat(TAG_DAMAGE, getDamage());
        nbt.putFloat(TAG_STAY, getStayTime());
        nbt.putBoolean(TAG_HUGE, isSplashHuge());
        nbt.putString(TAG_TYPE, getBulletType());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setRotation(nbt.getFloat(TAG_ROTATION));
        setRotationRandom(nbt.getFloat(TAG_ROTATION_RANDOM));
        setPitch(nbt.getFloat(TAG_PITCH));
        setLife(nbt.getFloat(TAG_LIFE));
        setDamage(nbt.getFloat(TAG_DAMAGE));
        setStayTime(nbt.getFloat(TAG_STAY));
        setSplashHuge(nbt.getBoolean(TAG_HUGE));
        setBulletType(nbt.getString(TAG_TYPE));
    }

    @Override
    public void tick() {
        super.tick();
        if (getLife() > 0) {
            if (getStayTime() <= 0) {
                setLife(getLife() - 1f);
            } else {
                setStayTime(getStayTime() - 1f);
            }
        } else {
            this.discard();
            return;
        }
        List<LivingEntity> entities = this.getWorld().getEntitiesByClass(LivingEntity.class,
                this.getBoundingBox().expand(20), e -> true);
        for (LivingEntity e : entities) {
            if (e.getUuid().toString().equals(getTarget())) {
                Vec3d v = HAMathHelper.getVector(this.getPos(), e.getPos());
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_X.getTag(), (float) v.x * 2.5f);
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_Y.getTag(), (float) v.y * 2.5f);
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_Z.getTag(), (float) v.z * 2.5f);
                break;
            }
        }

        if (getStayTime() > 0) {
            this.setVelocity(0, 0, 0);
        } else {
            Vec3d v = new Vec3d(NBTHelper.getEntityFloat(this, TAGS_ENTITY.MOTION_X.getTag()),
                    NBTHelper.getEntityFloat(this, TAGS_ENTITY.MOTION_Y.getTag()),
                    NBTHelper.getEntityFloat(this, TAGS_ENTITY.MOTION_Z.getTag()));
            if (v.length() > 0) {
                this.setVelocity(v.x, v.y, v.z);
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_X.getTag(), 0);
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_Y.getTag(), 0);
                NBTHelper.setEntityFloat(this, TAGS_ENTITY.MOTION_Z.getTag(), 0);
            }
        }

        Vec3d vel = this.getVelocity();
        if (vel.x == 0 && vel.y == 0 && vel.z == 0) {
            //  Entity#setRotation(yaw=getPitch(), pitch=getRotation())
            this.setYaw(getPitch());
            this.setPitch(getRotation());
        } else {
            // ProjectileEntity.tick 基类只推事件/年龄，不做位移也不触发碰撞，
            // 必须手动 raycast 碰撞 + move 位移（参照 EntityHeroArrow），否则翡翠停留原地不发射。
            HitResult hitResult = ProjectileUtil.getCollision(this,
                    e -> e != this.getOwner() && e.canHit());
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
                spawnParticle(ParticleTypes.FIREWORK, getX() + r1, getY() + r2, getZ() + r3, r1, r2, r3);
            }
        }
        if (isSplashHuge()) {
            double r1 = (Math.random() - 0.5) * 0.2;
            double r2 = (Math.random() - 0.5) * 0.2;
            double r3 = (Math.random() - 0.5) * 0.2;
            if (r1 > 0.05) {
                // 固定绿宝石水花（HAPPY_VILLAGER）；useHuajiSplash 语义已改为贴图替换
                // （见构造器 setBulletType），不再用 huaji_splash 粒子代替绿宝石水花。
                spawnParticle(ParticleTypes.HAPPY_VILLAGER, getX() + r1, getY() + r2, getZ() + r3, r1, r2, r3);
            }
        }
    }

    private void spawnParticle(ParticleEffect particle, double x, double y, double z, double dx, double dy, double dz) {
        if (this.getWorld().isClient) {
            this.getWorld().addParticle(particle, x, y, z, dx, dy, dz);
        } else {
            ((ServerWorld) this.getWorld()).spawnParticles(particle, x, y, z, 1, dx, dy, dz, 0d);
        }
    }

    @Override
    protected void onCollision(HitResult result) {
        List<Entity> group = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(2));
        int extra = group.size();
        if (result.getType() == HitResult.Type.ENTITY) {
            Entity entityHit = ((EntityHitResult) result).getEntity();
            if (entityHit != null && entityHit != getOwner()) {
                if (!this.getWorld().isClient) {
                    if (!(entityHit instanceof EmeraldBulletEntity)) {
                        entityHit.damage(DamageLoader.voidBreak(entityHit),
                                getDamage() * (1 + extra));
                        this.discard();
                        this.getWorld().createExplosion(this, getX(), getY(), getZ(), 0.5f, false,
                                World.ExplosionSourceType.NONE);
                    }
                }
                this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1f);
            }
        }
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) result).getBlockPos();
            BlockState state = this.getWorld().getBlockState(pos);
            if (!state.getCollisionShape(this.getWorld(), pos).isEmpty()) {
                this.discard();
                this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1f);
                this.getWorld().createExplosion(this, getX(), getY(), getZ(), 0.5f, false,
                        World.ExplosionSourceType.NONE);
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

    public float getRotationRandom() {
        return this.getDataTracker().get(ROTATION_RANDOM);
    }

    public void setRotationRandom(float rot) {
        this.getDataTracker().set(ROTATION_RANDOM, rot);
    }

    public float getDamage() {
        return this.getDataTracker().get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.getDataTracker().set(DAMAGE, damage);
    }

    public float getStayTime() {
        return this.getDataTracker().get(STAY);
    }

    public void setStayTime(float damage) {
        this.getDataTracker().set(STAY, damage);
    }

    public boolean isSplashHuge() {
        return this.getDataTracker().get(HUGE);
    }

    public void setSplashHuge(boolean isHuge) {
        this.getDataTracker().set(HUGE, isHuge);
    }

    public String getBulletType() {
        return this.getDataTracker().get(BULLET_TYPE);
    }

    public void setBulletType(String type) {
        this.getDataTracker().set(BULLET_TYPE, type);
    }

    public String getTarget() {
        return this.getDataTracker().get(TARGET);
    }

    public void setTarget(String type) {
        this.getDataTracker().set(TARGET, type);
    }

    public enum TAGS_ENTITY {
        MOTION_X("huajiage.motion.x"),
        MOTION_Y("huajiage.motion.y"),
        MOTION_Z("huajiage.motion.z");

        TAGS_ENTITY(String tag) {
            this.tag = tag;
        }

        private final String tag;

        public String getTag() {
            return tag;
        }
    }
}
