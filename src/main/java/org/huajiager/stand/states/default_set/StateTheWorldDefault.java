package org.huajiager.stand.states.default_set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.HAMathHelper;
import org.huajiager.util.NBTHelper;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The World 默认态， 。
 *
 * 1.20.1 映射说明：
 *  - EntityDragon → EnderDragon， dragonPartHead 分区伤害简化为对龙头实体整体伤害（part 系统未）。 *  - 活跃 volume 内对视线外实体弹飞（世界系近身连打），本实现保留：
 *    龙优先判定、LivingEntity 判定（含 DIO_HIT 计数与物理弹飞）、掉落物/经验球排除、大体型方块实体给玩家抗性。
 *  -  !(i instanceof EntityStandBase) 排除替身实体（EntityStandBase 已）。
 */
public class StateTheWorldDefault extends StandStateBase {

    /** 攻击命中音效时长（tick）：ffprobe 实测（已删除尾部留白），按 20 tick/s 换算并向上取整，
     *  保证音频完整播完。冷却按本次实际触发的音效时长动态设置：
     *  播完一个音后若继续命中，即刻允许播放下一个，不再有固定冷却造成的空白期。
     *  若后续再改音频，需重新 ffprobe 实测换算。 */
    public static final int STAND_THE_WORLD_HIT_1_DURATION_TICKS = 66; // ≈ 3.274s
    public static final int STAND_THE_WORLD_HIT_2_DURATION_TICKS = 51; // ≈ 2.508s
    public static final int DIO_HIT_DURATION_TICKS = 60;               // ≈ 2.995s
    /** 每位玩家的下一次允许播放攻击音效 tick（keyed by user UUID），值为「上次播放 tick + 该音效时长」。
     * 服务端 state 为全局单例，用 static 以便 MessageStandModeSwitch（切换攻击态起手音）共享同一套冷却，
     * 避免「切换起手音 + 紧接着的命中音」叠加成两响。 */
    private static final Map<UUID, Integer> LAST_HIT_SOUND_TICK = new ConcurrentHashMap<>();

    public StateTheWorldDefault() {
    }

    public StateTheWorldDefault(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    /** 命中音是否已可播放：距上次播放的音频已完整播完（user.age >= 上次允许播放时刻）。 */
    private static boolean isHitSoundCooldownReady(LivingEntity user) {
        Integer readyAtTick = LAST_HIT_SOUND_TICK.get(user.getUuid());
        return readyAtTick == null || user.age >= readyAtTick;
    }

    /** 记录命中音播放：按本次实际播放音效的时长设置下一次可播放时刻。 */
    private static void markHitSoundPlayed(LivingEntity user, int durationTicks) {
        LAST_HIT_SOUND_TICK.put(user.getUuid(), user.age + durationTicks);
    }

    /**
     * 攻击系音效统一入口（THE_WORLD 命中/时停拳/DIO 特效/切换攻击态起手音共用）：
     * 冷却内静默跳过并返回 false，就绪则播放并占冷却（按该音效时长设下一次可播时刻）。
     * 供 MessageStandModeSwitch 切换攻击态起手音调用，保证「切换起手音 + 紧接着的命中音」
     * 不会叠加成两响。
     */
    public static boolean tryPlayAttackSound(LivingEntity user, net.minecraft.sound.SoundEvent sound, float volume, int durationTicks) {
        if (!isHitSoundCooldownReady(user)) {
            return false;
        }
        HuajiSoundPlayer.playToNearbyClient(user, sound, volume);
        markHitSoundPlayed(user, durationTicks);
        return true;
    }

    @Override
    public void doTask(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        int stage = StandUtil.getStandStage(user);
        if (type == null) {
            return;
        }

        Box box = user.getBoundingBox().expand(
                stage > 0 ? type.getDistance() + 1f : type.getDistance(),
                stage > 0 ? type.getDistance() + 1f : type.getDistance(),
                stage > 0 ? type.getDistance() + 1f : type.getDistance());
        List<Entity> entityCollection = user.getWorld().getOtherEntities(user, box);
        if (entityCollection.size() <= 0) {
            return;
        }

        // 本 tick 内命中音效是否已播放过：连打命中多目标时只响一次，避免多段音叠加
        boolean hitSoundPlayedThisTick = false;

        for (Entity i : entityCollection) {
            Vec3d back = HAMathHelper.getVectorEntityEye(user, i);
            boolean flagPlayer = false;
            boolean flagDegree = HAMathHelper.getDegreeXZ(user.getRotationVector(),
                    HAMathHelper.getVectorEntityEye(user, i)) > (type.getName().equals(StandLoader.STAR_PLATINUM.getName()) ? 120 : 90);
            if (flagDegree) {
                continue;
            }

            if (user instanceof PlayerEntity) {
                flagPlayer = true;
            }

            if (i instanceof EnderDragonEntity) {
                EnderDragonEntity dragon = (EnderDragonEntity) i;
                //  dragon.attackEntityFromPart(dragonPartHead, new EntityDamageSource(...).setExplosion(), ...)
                // 1.20.1 分区伤害系统未，简化为对龙本体造成伤害
                dragon.damage(DamageLoader.dioHit(dragon), type.getDamage() * type.getSpeed());
            }

            if (i instanceof LivingEntity) {
                // 对齐 !(i instanceof EntityStandBase)：替身展示实体不算攻击目标。
                // 攻击态替身站玩家正前方 1 格、方向角在 90° 内，此前被裁剪未排除时，
                // 替身自己会进 getOtherEntities 命中断，表现为"无生物时也一直结算命中音"
                // （打空气刷屏）。现在实体类已就位，恢复排除。
                if (i instanceof EntityStandBase) {
                    continue;
                }
                LivingEntity target = (LivingEntity) i;
                if (target != user) {
                    float random = new Random().nextFloat() * 100;
                    // 高阶段 DIO 特效音：
                    // DIO_HIT 计数保留原语义，但爆炸+喊声音效纳入统一命中冷却，冷却内不再随机轰炸
                    if (random < 20 && target.hurtTime <= 0 && stage > 0) {
                        if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) < 120) {
                            NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT, 120);
                        }
                        if (!hitSoundPlayedThisTick && isHitSoundCooldownReady(user)) {
                            HuajiSoundPlayer.playToNearbyClient(target, SoundEvents.ENTITY_GENERIC_EXPLODE, 0.25f);
                            HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.DIO_HIT, 0.75f);
                            markHitSoundPlayed(user, DIO_HIT_DURATION_TICKS);
                            hitSoundPlayedThisTick = true;
                        }
                    }

                    // 时停中命中：打 DIO_HIT 标记 + 登记延迟伤害（时停结束统一结算，不实时扣血）
                    if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.TIME_STOP) > 0) {
                        if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) < 60) {
                            NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT, 60);
                        }
                        EventTimeStop.registerPendingPunch(user, target, type.getDamage());
                    } else {
                        target.damage(DamageLoader.dioHit(target), type.getDamage());
                    }

                    // 攻击命中音效（STAND_THE_WORLD_HIT）：与 DIO 特效音共享同一套
                    // 动态命中冷却——冷却按本次实际播放的音频时长设置，播完即可触发下一个。                    // 本 tick 内已播放过任意命中音（含 DIO 特效音）则不再叠加，避免连打多目标多音种刷屏。
                    if (!hitSoundPlayedThisTick && isHitSoundCooldownReady(user)) {
                        boolean useSecond = new Random().nextBoolean();
                        HuajiSoundPlayer.playToNearbyClient(user,
                                useSecond ? SoundLoader.STAND_THE_WORLD_HIT_2
                                        : SoundLoader.STAND_THE_WORLD_HIT_1,
                                0.6f);
                        markHitSoundPlayed(user,
                                useSecond ? STAND_THE_WORLD_HIT_2_DURATION_TICKS
                                        : STAND_THE_WORLD_HIT_1_DURATION_TICKS);
                        hitSoundPlayedThisTick = true;
                    }

                    if (HAMathHelper.getVectorEntityEye(user, target).length() < type.getDistance()) {
                        target.setVelocity(back.x, back.y, back.z);
                    }
                }
            } else if (i instanceof ItemEntity || i instanceof ExperienceOrbEntity) {
                continue;
            } else if (HAMathHelper.getAABBSize(i.getBoundingBox()) > 2) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 50, 5));
                continue;
            } else {
                i.setVelocity((type.getDamage() / 10) * back.x, (type.getDamage() / 10) * back.y, (type.getDamage() / 10) * back.z);
            }
        }
    }
}
