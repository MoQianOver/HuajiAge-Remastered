package org.huajiager.stand.entity;

import java.util.Collections;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.world.World;


/**
 * 替身展示实体。
 *
 * 继承 Fabric 的 HorseEntity。
 * 裁剪策略：
 * - spawn 数据（IEntityAdditionalSpawnData / writeSpawnData）：站姿类型与用户已通过
 *   DataTracker + NBT 同步，客户端表现（立起音效 / RIDE 尺寸）依赖未就绪的
 *   StandClientUtil / StandStates RIDE 标签，整段裁剪。 * - hasNoGravity 中 StandResourceLoader.CUSTOM_STAND_SERVER（自定义替身资源未就绪）
 *   判断裁剪，统一返回 true（无重力）。 * - 骑乘跟随 / 位置吸附（getEntityBoundingBox / getControllingPassenger / travel）
 *   属客户端表现，此处仅保留服务端核心 tick 跟随逻辑。
 */
public class EntityStandBase extends HorseEntity {


    private final String noUser = "8fdd0799-16c2-49d9-bdea-e75a07b9ec04";

    private static final String TAG_TYPE = "type";
    private static final String TAG_USER = "user";
    private static final String TAG_USER_NAME = "userName";
    private static final String TAG_HAS_ENTITY = "hasEntity";

    private static final TrackedData<String> TYPE = DataTracker.registerData(EntityStandBase.class,
            TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> USER = DataTracker.registerData(EntityStandBase.class,
            TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> USERNAME = DataTracker.registerData(EntityStandBase.class,
            TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> HAS_ENTITY = DataTracker.registerData(EntityStandBase.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    /**
     * 替身状态机状态名（default / idle / punch ...），服务端每 tick 从宿主 STAND_DATA
     * 写入，经 DataTracker 自动广播给所有客户端——其他玩家渲染端不依赖宿主 STAND_DATA
     * attachment（该数据在他人客户端从未同步过，读到的总是 null → 永远按攻击态渲染）。
     */
    private static final TrackedData<String> STAND_STATE = DataTracker.registerData(EntityStandBase.class,
            TrackedDataHandlerRegistry.STRING);

    public static final EntityType<EntityStandBase> TYPE_ENTITY = EntityType.Builder
            .<EntityStandBase>create((type, world) -> new EntityStandBase(type, world), SpawnGroup.MISC)
            .setDimensions(0.1f, 0.1f)
            .build("huajiager:stand_base");

    private boolean attributesInitialized = false;

    public EntityStandBase(EntityType<? extends EntityStandBase> type, World world) {
        super(type, world);
    }

    /**
     * 静音替身脚步声：替身继承 HorseEntity，每 tick 位移会播放踩地脚步声。     * 替身贴人连打/飞行时高频触发，用户实测"攻击连打冒重复脚步声"即源于此。
     * 覆写为空彻底静音。
     */
    @Override
    public void playStepSound(BlockPos pos, BlockState state) {
    }

    /**
     * 同时静音马匹周期性随机叫声（HorseEntity.getAmbientSound）。
     */
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void initDataTracker() {
        // 必须调用 super.initDataTracker()：HorseEntity 构造链路（super(type, world) 触发
        // Entity 构造器中的 initDataTracker）会读取马类 VARIANT/TAMED/OWNER 等 tracked data，
        // 缺注册时 DataTracker.get 返回 null Entry 直接 NPE（见召唤时 <init> 报错）。
        // 本项目只用替身自身数据，马类冗余属性不影响行为。
        super.initDataTracker();
        this.dataTracker.startTracking(TYPE, StandLoader.THE_WORLD.getName());
        this.dataTracker.startTracking(USER, noUser);
        this.dataTracker.startTracking(USERNAME, "steve");
        this.dataTracker.startTracking(HAS_ENTITY, false);
        this.dataTracker.startTracking(STAND_STATE, ExposedData.States.DEFAULT.getName());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString(TAG_TYPE, getTypeName());
        nbt.putString(TAG_USER, getUserId());
        nbt.putString(TAG_USER_NAME, getUserName());
        nbt.putBoolean(TAG_HAS_ENTITY, hasEntity());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setType(nbt.getString(TAG_TYPE));
        this.setUser(nbt.getString(TAG_USER));
        this.setUserName(nbt.getString(TAG_USER_NAME));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.SUCCESS;
    }

    /**
     * 骑乘控制配套。
     * Fabric 1.20.1 AbstractHorseEntity.travel 需 isTame + isSaddled 才接受骑手输入，
     * 替身没有驯服/马鞍状态，直接全部放行；受控乘客为第一乘客（骑手）。
     */
    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    public boolean isTame() {
        return true;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity le ? le : null;
    }

    @Override
    public boolean hasNoGravity() {
        // 替身统一无重力（贴附跟随）；按自定义替身 gravity 字段判断的
        // 分支已移除，其余替身均保持默认无重力。
        return true;
    }

    /**
     * 替身贴身渲染（攻击态恒在玩家正前方1格同高、闲置态在背后），默认可见盒极小（由
     * TYPE_ENTITY 尺寸 0.1x0.1 得出）。客户端按本可见盒做视锥剔除：行走/飞行时视角晃动
     * 会让极小盒频繁进出视锥，整颗实体（含第一人称攻击态拳头）一帧帧闪没——静止才正常。
     * 放大为 8 格半径包围盒，替身相对相机恒在视锥内（此盒仅用于剔除判定，不参与碰撞）。
     */
    @Override
    public Box getVisibilityBoundingBox() {
        return new Box(getX() - 8.0, getY() - 8.0, getZ() - 8.0,
                getX() + 8.0, getY() + 8.0, getZ() + 8.0);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // 替身是纯展示实体，必须免疫一切伤害（含窒息/虚空/挤压/玩家攻击）。
        // 此前实测在狭窄空间（地下洞/贴墙）替身被生成到方块内，受窒息伤害 1-2 秒死亡
        // （health 归零 → isAlive=false → 服务器移除、停止渲染），表现即为"召唤即消失"。
        return true;
    }

    @Override
    public boolean isPushable() {
        // 替身是跟随展示实体：既不推挤玩家、也拒绝被撞离，
        // 避免替身与玩家碰撞时"推着玩家走"的体验。
        return false;
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity passiveEntity) {
        return null;
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.45d;
    }

    @Override
    public Arm getMainArm() {
        return Arm.LEFT;
    }

    private int heartbeat = 0;

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            LivingEntity user = getUser();
            if (user == null || !user.isAlive()) {
                this.discard();
                return;
            }
            IExposedData data = StandUtil.getStandData(user);
            if (data == null || !data.isTriggered()) {

                this.discard();
                return;
            }
            // 状态写入实体 DataTracker：值变化时自动广播给所有客户端（切换闲置/攻击后
            // 其他玩家立即看到模型态切换）。set 相同值不会触发发包，每 tick 调用无开销。
            this.setStandState(data.getState());
            // 骑乘状态同步：若玩家骑乘本实体（当前替身不会触发 startRiding，
            // 恒为 false），保持 hasEntity=true 跳过下方位置吸附。            // 下马后恢复贴附展示模式。
            if (user.getVehicle() == this) {
                this.setEntity(true);
            } else if (hasEntity()) {
                this.setEntity(false);
            }
            // 周期心跳（每100tick≈5s）：确认实体存活、触发状态、绑定玩家、读取实例一致
            if ((++heartbeat % 100) == 0) {

            }
            if (!hasEntity()) {
                // 位置按替身状态区分、与玩家同高度（不再抬高/偏侧）：
                // - 闲置态：玩家正背后（沿朝向反方向拉开），替身在身后待机。                // - 攻击态（default）：玩家正前方，替身站到身前挥拳。
                // 依据：替身均贴附在玩家身上（叠加层渲染于玩家模型空间，
                // default translate(0,-0.2,-0.75) 即背后、idle translate(-0.45,-0.2,0.45) 即身前），
                // 本工程为独立实体渲染，按用户要求改为「闲置=背后、攻击=玩家前方」，y 取玩家脚底同高。
                // 水平朝向唯一取自玩家 yaw，俯仰（pitch）不参与：
                // 原来用 getRotationVector 水平投影，极端仰角/俯视角（pitch≈±90°）时
                // 水平分量趋零 → 朝向退化 → 替身被算到玩家正中心，与玩家重叠并互相挤压，
                // 表现为"视角抬到最高/最低时替身推着玩家走"。改用 yaw 后任何视角都稳定。
                double yawRad = Math.toRadians(user.getYaw());
                double nx = -Math.sin(yawRad);
                double nz = Math.cos(yawRad);
                boolean idleState = data != null
                        && ExposedData.States.IDLE.getName().equals(data.getState());
                boolean punchState = data != null
                        && ExposedData.States.PUNCH.getName().equals(data.getState());
                StandBase stand = getStand();
                boolean isStarPlatinum = stand != null
                        && StandLoader.STAR_PLATINUM.getName().equals(stand.getName());
                boolean isHierophantGreen = stand != null
                        && StandLoader.HIEROPHANT_GREEN.getName().equals(stand.getName());
                boolean isKillerQueen = stand != null
                        && StandLoader.KILLER_QUEEN.getName().equals(stand.getName());
                double bx;
                double bz;
                if (idleState) {
                    if (isHierophantGreen) {
                        // 绿法皇闲置态（ModelHierophantGreenIdle.=(0.8,-0.75,-0.7)：
                        // 左侧 0.8、前方 0.9、高 0.75）——闲置飘在玩家左前方（前方按用户实测微调 0.7→0.9）
                        final double OFF_FRONT = 0.9D;
                        final double OFF_LEFT = 0.8D; // 正值 = 向玩家左侧偏移 0.8 格
                        bx = user.getX() + nx * OFF_FRONT + nz * OFF_LEFT;
                        bz = user.getZ() + nz * OFF_FRONT - nx * OFF_LEFT;
                    } else if (isStarPlatinum) {
                        // 白金之星（用户要求）：玩家正背后 1 格、无侧偏
                        bx = user.getX() - nx * 1.0D;
                        bz = user.getZ() - nz * 1.0D;
                    } else if (stand != null && ("hermit_purple".equals(stand.getName()) || "huajiager:hermit_purple".equals(stand.getName()))) {
                        // 隐者之紫缠绕型：贴附玩家位置，服务端逻辑位置同步
                        bx = user.getX();
                        bz = user.getZ();
                    } else {
                        // 其它替身（含 THE_WORLD）：保持原状——背后 1 格 + 向右偏移 0.5 格
                        final double OFF_BACK = 1.0D;
                        final double OFF_LEFT = -0.5D; // 向左取负 = 向玩家右侧偏移 0.5 格
                        bx = user.getX() - nx * OFF_BACK + nz * OFF_LEFT;
                        bz = user.getZ() - nz * OFF_BACK - nx * OFF_LEFT;
                    }
                } else {
                    if (isHierophantGreen) {
                        // 绿法皇攻击态（ModelHierophantGreen.=(0.5,-1.0,0.75)：
                        // 左侧 0.5、背后 0.75、高 1.0）——即攻击时飘在玩家背后左上方
                        final double OFF_BACK = 0.75D;
                        final double OFF_LEFT = 0.5D; // 正值 = 向玩家左侧偏移 0.5 格
                        bx = user.getX() - nx * OFF_BACK + nz * OFF_LEFT;
                        bz = user.getZ() - nz * OFF_BACK - nx * OFF_LEFT;
                    } else if (isKillerQueen && !punchState) {
                        // KQ 待机态（ModelKillerQueen.=(0.9,-0.1,-0.8)：
                        // 左侧 0.9、前方 0.8、高 0.1）——按模型数值映射为实体偏移，不再沉底
                        final double OFF_FRONT = 0.8D;
                        final double OFF_LEFT = 0.9D; // 正值 = 向玩家左侧偏移 0.9 格
                        bx = user.getX() + nx * OFF_FRONT + nz * OFF_LEFT;
                        bz = user.getZ() + nz * OFF_FRONT - nx * OFF_LEFT;
                    } else if (isKillerQueen) {
                        // KQ 攻击态（ModelKillerQueenPunch.=(0,0,-0.9)：
                        // 正前方 0.9、与玩家同高）——十指挥拳正前方贴身，不再沉底
                        final double OFF_FRONT = 0.9D;
                        bx = user.getX() + nx * OFF_FRONT;
                        bz = user.getZ() + nz * OFF_FRONT;
                    } else if (stand != null && ("hermit_purple".equals(stand.getName()) || "huajiager:hermit_purple".equals(stand.getName()))) {
                        // 隐者之紫缠绕型：攻击态同样贴附玩家位置，Overdrive 切换不影响位置
                        bx = user.getX();
                        bz = user.getZ();
                    } else {
                        final double OFF_FRONT = 0.7D; // 玩家前方 0.7 格（同高度，用户要求由 1 格缩近）
                        bx = user.getX() + nx * OFF_FRONT;
                        bz = user.getZ() + nz * OFF_FRONT;
                    }
                }
                // 高度：白金之星闲置比玩家脚底高 0.5 格；其它替身闲置高 0.3 格（沿用默认高度）；攻击态与玩家同高
                double by = user.getY()
                        + (idleState ? (isStarPlatinum ? 0.5D : 0.3D) : 0.0D);
                // 绿法皇特判：攻击态比玩家高 1 格、闲置态比玩家高 0.75
                if (isHierophantGreen) {
                    by = user.getY() + (idleState ? 0.75D : 1.0D);
                }
                // 白金之星特判：攻击态位置调高 0.3 格（用户要求，原与玩家同高）
                if (isStarPlatinum && !idleState) {
                    by = user.getY() + 0.3D;
                }
                // KQ 特判：待机态原比玩家高 0.1 格，用户要求攻击/连打
                // 两态位置整体再调高 0.3 格 → 待机 0.4
                if (isKillerQueen && !punchState && !idleState) {
                    by = user.getY() + 0.4D;
                }
                // KQ 连打态特判：位置调高 0.3 格（用户要求，原与玩家同高）
                if (isKillerQueen && punchState) {
                    by = user.getY() + 0.3D;
                }
                // 疯狂钻石特判：攻击/治疗态比玩家高 0.3 格（用户实测），
                // 避免模型底盘贴近地面整体下沉遁地（与 RenderStandBase 渲染修正一致）
                if (stand != null && ("crazy_diamond".equals(stand.getName())
                        || "huajiager:crazy_diamond".equals(stand.getName())) && !idleState) {
                    by = user.getY() + 0.3D;
                }
                // 隐者之紫特判：缠绕贴附玩家正中心。物理碰撞盒（0.1x0.1x0.1）整体抬升到
                // 玩家碰撞盒之上（站立身高 1.8，抬 2.2 格留余量），彻底避开与玩家碰撞盒重叠——
                // MC 推挤逻辑只看被推者 isPushable（玩家恒为 true），替身哪怕 isPushable=false
                // 只要位置与玩家重叠，move 时仍会把玩家推开（"替身推着玩家走"）。
                // 渲染位置由客户端 RenderStandBase 用玩家实时坐标独立重算贴回（不依赖实体
                // 物理 Y），服务端抬高不影响视觉缠绕效果。
                if (stand != null && ("hermit_purple".equals(stand.getName())
                        || "huajiager:hermit_purple".equals(stand.getName()))) {
                    by = user.getY() + 2.2D;
                }
                this.setPosition(bx, by, bz);
                float faceYaw = user.getYaw();
                this.setYaw(faceYaw);
                this.setHeadYaw(faceYaw);
                this.setBodyYaw(faceYaw);
            }
        }

        initAttributesOnce();
    }

    /**
     * 在 applyEntityAttributes 中设置属性初值；Fabric 1.20.1 属性由
     * EntityType 属性注册表注入，构造器内取不到实例，改为 tick 首帧惰性设置一次。
     */
    private void initAttributesOnce() {
        if (attributesInitialized) {
            return;
        }
        attributesInitialized = true;
        if (this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10);
        }
        if (this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(10);
        }
        if (this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        }
        if (this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH) != null) {
            // 0.7 → 跳跃高度 ≈ 0.7²/(2×0.08) ≈ 3 格，滞空约 0.9 秒（默认 0.4≈1 格
            // 滞空 0.5 秒，表现为"一跳就落地"；0.9=5 格过夸张）
            this.getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH).setBaseValue(0.7);
        }
        // 手动 spawn 的实体不会走 MobEntity.initialize 自动满血，health tracked data 可能
        // 停在构造默认值（1.0）。强制拉满，避免 1 点血被任何微弱伤害打空即死。
        this.setHealth(10f);
    }

    // =============================== Settings =====================================

    public StandBase getStand() {
        return StandLoader.getStand(this.dataTracker.get(TYPE));
    }

    private StandBase getStandBase() {
        return getStand();
    }

    private String getTypeName() {
        StandBase stand = getStandBase();
        return stand != null ? stand.getName() : StandLoader.EMPTY;
    }

    public LivingEntity getUser() {
        if (this.getWorld() == null) {
            return null;
        }
        UUID uid = null;
        String userId = getUserId();
        if (!userId.isEmpty() && !userId.equals(noUser)) {
            try {
                uid = UUID.fromString(userId);
            } catch (IllegalArgumentException ignored) {
                uid = null;
            }
        }
        String userName = getUserName();
        for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (player.getName().getString().equals(userName)) {
                return player;
            }
            if (uid != null && uid.equals(player.getUuid())) {
                return player;
            }
        }
        return null;
    }

    private String getUserId() {
        return this.dataTracker.get(USER);
    }

    private String getUserName() {
        return this.dataTracker.get(USERNAME);
    }

    private boolean hasEntity() {
        return this.dataTracker.get(HAS_ENTITY);
    }

    public void setUserName(String name) {
        this.dataTracker.set(USERNAME, name);
    }

    public void setUser(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            this.dataTracker.set(USER, noUser);
        } else {
            this.dataTracker.set(USER, uuid);
        }
    }

    public void setType(String type) {
        StandBase stand = StandLoader.getStand(type);
        this.dataTracker.set(TYPE, stand != null ? stand.getName() : StandLoader.EMPTY);
    }

    public void setEntity(boolean has_entity) {
        this.dataTracker.set(HAS_ENTITY, has_entity);
    }

    /**
     * 获取替身状态机状态名（DataTracker 已向所有客户端广播，服务端每 tick 写入）。
     */
    public String getStandStateName() {
        return this.dataTracker.get(STAND_STATE);
    }

    /**
     * 写入替身状态机状态名：null/空回退 DEFAULT，避免 DataTracker 出现无效值。
     */
    public void setStandState(String state) {
        this.dataTracker.set(STAND_STATE, state == null || state.isEmpty()
                ? ExposedData.States.DEFAULT.getName() : state);
    }
}
