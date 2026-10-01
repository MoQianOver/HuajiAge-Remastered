package org.huajiager.stand.states.default_set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.HAMathHelper;
import org.huajiager.util.NBTHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Star Platinum 默认态。
 *
 * 行为：
 *  - 非闲置态：与 TheWorld 默认态同构的近身连打（视野角判定 + 龙优先 + LivingEntity 判定 + 掉落物/经验球排除），
 *    每次命中随机播放 hit_1~hit_4 / repeat_1 普通命中音，无重击分支。 *  - 闲置态：周期性给用户施加 替身标记/饥饿/发光 药水并充能。
 *
 * 1.20.1 映射口径与 StateTheWorldDefault 一致； !(i instanceof EntityStandBase) 排除项（EntityStandBase 已）。
 */
public class StateStarPlatinumDefault extends StandStateBase {

    /** 攻击命中音效时长（tick）：白金普通命中音为 hit_1~hit_4 / repeat_1（实测 OGG 时长，20tps 向上取整），
     *  冷却按本次实际播放的音频时长动态设置，播完即可触发下一个。 */
    public static final int STAND_HIT_1_DURATION_TICKS = 104;
    public static final int STAND_HIT_2_DURATION_TICKS = 141;
    public static final int STAND_HIT_3_DURATION_TICKS = 177;
    public static final int STAND_HIT_4_DURATION_TICKS = 78;
    public static final int STAND_REPEAT_1_DURATION_TICKS = 39;
    /** 每位玩家的下一次允许播放攻击音效 tick（keyed by user UUID），值为「上次播放 tick + 该音效时长」。
     * 服务端 state 为全局单例，用 static 以便统一冷却语义。 */
    private static final Map<UUID, Integer> LAST_HIT_SOUND_TICK = new ConcurrentHashMap<>();

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
     * 攻击音带冷却播放（供切换模式等外部入口复用）：切换起手音与状态机命中音共用
     * 同一冷却 Map——切换音未播完时紧接着的命中音会被静默，杜绝"切换+首拳"双响。
     *
     * @return 本次是否真正播放（false = 冷却中，调用方无需补播）
     */
    public static boolean tryPlayAttackSound(LivingEntity user, net.minecraft.sound.SoundEvent sound, float volume,
            int durationTicks) {
        if (!isHitSoundCooldownReady(user)) {
            return false;
        }
        HuajiSoundPlayer.playToNearbyClient(user, sound, volume);
        markHitSoundPlayed(user, durationTicks);
        return true;
    }

    public StateStarPlatinumDefault() {
    }

    public StateStarPlatinumDefault(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    @Override
    public void doTask(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        int stage = StandUtil.getStandStage(user);
        IExposedData data = StandUtil.getStandData(user);
        if (type == null) {
            return;
        }
        boolean isIdle = ExposedData.States.IDLE.getName().equals(data.getState());
        if (!isIdle) {
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
                    if (flagPlayer) {
                        dragon.damage(((PlayerEntity) user).getDamageSources().playerAttack((PlayerEntity) user),
                                type.getDamage() * type.getSpeed());
                    } else {
                        dragon.damage(user.getDamageSources().fallingAnvil(user), type.getDamage() * type.getSpeed());
                    }
                }

                if (i instanceof LivingEntity) {
                    // 对齐 !(i instanceof EntityStandBase)：替身展示实体不算攻击目标。
                    // 攻击态替身站玩家正前方 1 格，此前未排除时替身自己会进命中断，
                    // 无生物时也持续结算命中音（打空气刷屏），现恢复排除。
                    if (i instanceof EntityStandBase) {
                        continue;
                    }
                    LivingEntity target = (LivingEntity) i;
                    if (target != user) {
                        // 无重击分支：普通命中音纳入统一命中冷却（与 THE_WORLD 默认态同口径）——
                        // 冷却按本次实际播放的音频时长设置，播完即可触发下一个。                        // 本 tick 内已播放过任意命中音则不再叠加，避免连打/多目标时同 tick 双响。
                        if (!hitSoundPlayedThisTick && isHitSoundCooldownReady(user)) {
                            int soundIndex = new Random().nextInt(5);
                            int durationTicks;
                            switch (soundIndex) {
                                case 0:
                                    HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.STAND_STAR_PLATINUM_1, 0.6f);
                                    durationTicks = STAND_HIT_1_DURATION_TICKS;
                                    break;
                                case 1:
                                    HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.STAND_STAR_PLATINUM_2, 0.6f);
                                    durationTicks = STAND_HIT_2_DURATION_TICKS;
                                    break;
                                case 2:
                                    HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.STAND_STAR_PLATINUM_3, 0.6f);
                                    durationTicks = STAND_HIT_3_DURATION_TICKS;
                                    break;
                                case 3:
                                    HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.STAND_STAR_PLATINUM_4, 0.6f);
                                    durationTicks = STAND_HIT_4_DURATION_TICKS;
                                    break;
                                default:
                                    HuajiSoundPlayer.playToNearbyClient(target, SoundLoader.STAND_STAR_PLATINUM_REPEAT_1, 0.6f);
                                    durationTicks = STAND_REPEAT_1_DURATION_TICKS;
                                    break;
                            }
                            markHitSoundPlayed(user, durationTicks);
                            hitSoundPlayedThisTick = true;
                        }

                        if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.TIME_STOP) > 0
                                && NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) < 60) {
                            NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT, 60);
                        } else {
                            if (flagPlayer) {
                                PlayerEntity player = (PlayerEntity) user;
                                target.damage(player.getDamageSources().playerAttack(player), type.getDamage());
                            } else {
                                target.damage(user.getDamageSources().fallingAnvil(user), type.getDamage());
                            }
                        }

                        target.setVelocity(back.x, back.y, back.z);
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
        } else {
            List<StatusEffectInstance> effects = new ArrayList<>();
            effects.add(new StatusEffectInstance(PotionLoader.potionStand, 5 * 20));
            effects.add(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20, 5));
            effects.add(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20));
            StandPowerHelper.potionEffect(user, effects);
            StandPowerHelper.MPCharge(user, StandLoader.STAR_PLATINUM.getCharge());
        }
    }
}
