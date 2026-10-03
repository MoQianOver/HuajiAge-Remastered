package org.huajiager.stand.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.datafixers.util.Pair;

import org.huajiager.attachment.Attachments;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.item.ItemDiscCommand;
import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.events.EventKillerQueen;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.stand.messages.MessageDioHitClient;
import org.huajiager.util.HAMathHelper;
import org.huajiager.util.NBTHelper;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

/**
 * 替身通用辅助。
 * <p>已落入 6 个状态类实际调用的方法：
 * {@link #MPCharge}、{@link #potionEffect}，并补全 various 拳击/飞行态依赖的
 * {@link #rangePunchAttack}（重型方法，1.20.1 口径与本工程 default 态连打一致，
 * damage source 用 playerAttack / fallingAnvil）。其余方法按语义补齐。</p>
 */
public final class StandPowerHelper {

    private StandPowerHelper() {
    }

    /**
     * 充能：走 attachment 挂载的 StandHandler。
     * <p>必须用 getAttachedOrCreate 而非 getAttached：STAND_HANDLER 为 createDefaulted
     * attachment，getAttached 在未显式 set 时返回 initializer 每次新建的临时实例（不落库），
     * 回充落在临时实例上、而召唤校验（MessageStandUp / EventStandKey）经
     * StandUtil.getStandHandler 用 getAttachedOrCreate 读另一份，能量永远 0，
     * 导致觉醒后永远「精神力不足」无法召唤/放技能。统一挂载到实体 storage 后两处读到同实例。</p>
     */
    public static void MPCharge(LivingEntity user, int points) {
        StandHandler chargeHandler = user.getAttachedOrCreate(Attachments.STAND_HANDLER);
        if (chargeHandler != null) {
            chargeHandler.charge(points);
        }
    }

    /**
     * 批量施加药水效果。
     */
    public static void potionEffect(LivingEntity user, List<StatusEffectInstance> potions) {
        for (StatusEffectInstance potion : potions) {
            user.addStatusEffect(potion);
        }
    }

    /**
     * 替身范围内直线拳击连打（重型方法）。
     * <p>1.20.1 映射口径与 StateTheWorldDefault / StateStarPlatinumDefault 一致：
     *  - 视野角判定 + 龙优先（EnderDragonEntity.damage）。     *  - LivingEntity 命中：时停标记结算（TIME_STOP/DIO_HIT）或玩家/跌落伤害（playerAttack / fallingAnvil）。     *  - 额外排除 !(i instanceof EntityStandBase)，EntityStandBase 未接入该链，按 default 态口径裁剪。     *  - 掉落物/经验球排除；大体型方块实体给玩家抗性；其余实体按伤害比例弹飞。</p>
     *
     * @param user     使用者
     * @param degree   视野角阈值（度）
     * @param damage   单次伤害
     * @param distance 探测半径
     */
    public static boolean rangePunchAttack(LivingEntity user, float degree, float damage, float distance) {
        List<Entity> entityCollection = user.getWorld().getOtherEntities(user,
                user.getBoundingBox().expand(distance));
        if (entityCollection.isEmpty()) {
            return false;
        }
        boolean hit = false;

        for (Entity i : entityCollection) {
            Vec3d back = HAMathHelper.getVectorEntityEye(user, i);
            boolean flagPlayer = false;
            boolean flagDegree = HAMathHelper.getDegreeXZ(user.getRotationVector(),
                    HAMathHelper.getVectorEntityEye(user, i)) > degree;
            if (flagDegree) {
                continue;
            }

            if (user instanceof PlayerEntity) {
                flagPlayer = true;
            }

            if (i instanceof EnderDragonEntity) {
                EnderDragonEntity dragon = (EnderDragonEntity) i;
                // 隐者之紫波纹疾走：OVERDRIVE 形态命中用 overdrive_hit 死亡提示
                if (isHermitOverdrive(user)) {
                    dragon.damage(DamageLoader.overdriveHit(dragon), damage);
                } else if (flagPlayer) {
                    dragon.damage(((PlayerEntity) user).getDamageSources().playerAttack((PlayerEntity) user), damage);
                } else {
                    dragon.damage(user.getDamageSources().fallingAnvil(user), damage);
                }
            }

            if (i instanceof LivingEntity && !(i instanceof EntitySheerHeartAttack)
                    && !(i instanceof EntityStandBase)) {
                // 额外排除 EntityStandBase；本工程 KQ 替身 EntitySheerHeartAttack 贴身存在，
                // 若不排除会把替身当命中目标：既打伤自己替身，又经 onStandHit 发放"点赞"锁定替身
                // （右键引爆时爆炸中心=替身=玩家脸上 → 一直炸自己）。此处一并排除。
                LivingEntity target = (LivingEntity) i;
                if (target != user) {
                    hit = true;
                    // KQ 替身攻击命中：发放/更新"点赞"并锁定目标（替身自动攻击不走玩家攻击事件）
                    EventKillerQueen.onStandHit(user, target);
                    boolean timeStopped = NBTHelper.getEntityInteger(target, HuajiConstant.Tags.TIME_STOP) > 0;
                    // 时停中命中：打 DIO_HIT 标记 + 登记延迟伤害（时停结束统一结算，不实时扣血），
                    // 同时广播 DIO 命中表现（熔岩粒子 + DIO_HIT 音效），让时停拳有即时反馈
                    if (timeStopped) {
                        if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) < 60) {
                            NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT, 60);
                        }
                        EventTimeStop.registerPendingPunch(user, target, damage);
                        sendDioHitToNearby(user, target, true);
                    } else {
                        // 隐者之紫波纹疾走：OVERDRIVE 形态命中用 overdrive_hit 死亡提示
                        if (isHermitOverdrive(user)) {
                            target.damage(DamageLoader.overdriveHit(target), damage);
                        } else if (flagPlayer) {
                            PlayerEntity player = (PlayerEntity) user;
                            target.damage(player.getDamageSources().playerAttack(player), damage);
                        } else {
                            target.damage(user.getDamageSources().fallingAnvil(user), damage);
                        }
                    }

                    // 时停中目标保持冻结定格，不施加击退（否则破坏"纹丝不动"的冻结表现）。                    // 仅正常时间下按伤害比例击退
                    if (!timeStopped) {
                        target.setVelocity(back.x, back.y, back.z);
                    }
                }
            } else if (i instanceof ItemEntity || i instanceof ExperienceOrbEntity) {
                continue;
            } else if (HAMathHelper.getAABBSize(i.getBoundingBox()) > 2) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 50, 1));
                continue;
            } else {
                i.setVelocity((damage / 10) * back.x, (damage / 10) * back.y, (damage / 10) * back.z);
            }
        }
        return hit;
    }

    // ================= 替身音效冷却播放（连打音/修复音去重） =================

    /**
     * 每位玩家每个通道当前冷却中的替身音效截止 tick
     * （key = "uuid:channel" -> 可再次播放的 age）。
     * <p>分双通道：连打/起手音（hit）与技能修复音（repair）各自独立冷却。
     * 修复前为全局单通道，治疗态 update 每命中就播连打音（时长 100~184 tick）
     * 把冷却占满，capability 的 repair 音被静默 → "技能没音效"。
     * 分通道后 repair 音不再被连打音占用；hit 通道内连打/起手音仍共用冷却，
     * 保持"切模式+首拳不双响、同一音效未播完不重头播"的原语义。</p>
     */
    private static final Map<String, Integer> STAND_SOUND_CD_UNTIL = new ConcurrentHashMap<>();

    /** 替身音效完整时长（tick）：冷却窗口 = 音效时长，同一音效未播完前不重头再播 */
    private static final Map<String, Integer> STAND_SOUND_DURATION_TICKS = Map.ofEntries(
            Map.entry("huajiager:stand_crazy_diamond_1", 184),
            Map.entry("huajiager:stand_crazy_diamond_2", 100),
            Map.entry("huajiager:stand_crazy_diamond_3", 161),
            Map.entry("huajiager:stand_crazy_diamond_4", 133),
            Map.entry("huajiager:stand_crazy_diamond_repair_1", 62),
            Map.entry("huajiager:stand_crazy_diamond_repair_2", 122),
            // 白蛇连打命中音：时长按 ogg 实测（48000Hz，granule/rate 换算后向上取整）
            Map.entry("huajiager:stand_white_snake_hit_1", 35),
            Map.entry("huajiager:stand_white_snake_hit_2", 26),
            Map.entry("huajiager:stand_white_snake_hit_3", 30));

    /** 音效所属冷却通道：repair 音走 repair 通道，其余（连打/起手）走 hit 通道 */
    private static String soundChannel(String soundId) {
        return soundId != null && soundId.startsWith("huajiager:stand_crazy_diamond_repair_")
                ? "repair"
                : "hit";
    }

    /**
     * 带冷却的替身音效播放：同一玩家同一通道在同一音效时长窗口内只响第一声，
     * 后续调用被静默。疯狂钻石连打/修复音由 JS 状态机在命中或切态时调用，
     * 冷却自动错开重音叠加（hit 与 repair 双通道互不干扰）。
     *
     * @return 本次是否真正播放（false = 音效不存在或仍在冷却）
     */
    public static boolean playStandSoundWithCooldown(LivingEntity user, String soundId, float volume) {
        if (user == null || soundId == null) {
            return false;
        }
        Identifier sid = Identifier.tryParse(soundId);
        SoundEvent se = sid == null ? null : Registries.SOUND_EVENT.get(sid);
        if (se == null || se == SoundEvents.INTENTIONALLY_EMPTY) {
            return false;
        }
        String channel = soundChannel(soundId);
        if (!isStandSoundReady(user, channel)) {
            return false;
        }
        int duration = STAND_SOUND_DURATION_TICKS.getOrDefault(soundId, 60);
        STAND_SOUND_CD_UNTIL.put(user.getUuid() + ":" + channel, user.age + duration);
        HuajiSoundPlayer.playToNearbyClient(user, se, volume);
        return true;
    }

    /** 玩家当前是否可再次播放替身音效（指定通道冷却结束或从未播放） */
    public static boolean isStandSoundReady(LivingEntity user, String channel) {
        Integer until = STAND_SOUND_CD_UNTIL.get(user.getUuid() + ":" + channel);
        return until == null || user.age >= until;
    }

    /** 玩家当前是否可再次播放替身音效（任一通道就绪，兼容旧语义） */
    public static boolean isStandSoundReady(LivingEntity user) {
        return isStandSoundReady(user, "hit") || isStandSoundReady(user, "repair");
    }

    /**
     * 向目标周边玩家广播 DIO 命中表现（熔岩粒子 + DIO_HIT/DIO_FLAG 音效），
     * 让时停拳命中在客户端有即时的听觉/视觉反馈。复用 MessageDioHitClient（S2C），
     * 客户端处理器见 ClientPacketHandlers.processDioHit。
     */
    private static void sendDioHitToNearby(Entity source, Entity target, boolean flag) {
        if (!(source.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        MessageDioHitClient packet = new MessageDioHitClient(target.getPos(), flag);
        for (ServerPlayerEntity sp : sw.getPlayers()) {
            if (sp.squaredDistanceTo(target) < 64.0 * 64.0) {
                ServerPlayNetworking.send(sp, packet);
            }
        }
    }

    /**
     * 生成粒子特效。
     * <p>Fabric 1.20.1 无 playEvent 广播层，按 ID 映射到 world 级事件：
     * type=1 → 经验球拾取音效 + 世界事件 2005。</p>
     */
    public static void createParticleEffect(Entity entity, int type) {
        switch (type) {
            case 1:
                entity.getWorld().syncWorldEvent(2005, entity.getBlockPos(), 1);
                break;
            case 2:
            default:
                break;
        }
    }

    /**
     * 播放世界事件。两个重载均落到 world.syncWorldEvent。
     */
    public static void playEvent(Entity entity, int event_type, int data) {
        entity.getWorld().syncWorldEvent(event_type, entity.getBlockPos(), data);
    }

    public static void playEvent(World world, BlockPos blockPos, int event_type, int data) {
        world.syncWorldEvent(event_type, blockPos, data);
    }

    /**
     * 批量追加药水效果（变长参数版）。
     */
    public static void potionEffectAdd(LivingEntity user, StatusEffectInstance... potions) {
        potionEffect(user, Arrays.asList(potions));
    }

    /**
     * 按注册名施加药水效果（String、duration、level 版本）。
     */
    public static void potionEffectAdd(LivingEntity user, String type, int duration, int level) {
        StatusEffectInstance potion = newPotion(type, duration, level);
        if (potion != null) {
            potionEffect(user, Collections.singletonList(potion));
        }
    }

    /**
     * 按注册名查询状态效果。
     */
    public static StatusEffect getPotion(String type) {
        return Registries.STATUS_EFFECT.get(Identifier.tryParse(type));
    }

    /**
     * 按注册名构造状态效果实例（level 按 1 基减一）。
     */
    public static StatusEffectInstance newPotion(String type, int duration, int level) {
        StatusEffect potion = getPotion(type);
        if (potion != null) {
            return new StatusEffectInstance(potion, duration, Math.max(level - 1, 0));
        }
        return null;
    }

    /**
     * 获取使用者已召唤的替身实体。
     * <p>在用户周围 10×6×10 范围内找 allay 替身实体（EntityStandBase）且 getUser() == user。
     * 供 JS 的 entityWrapper.getStandEntity() / getSpeed() 使用。</p>
     */
    public static EntityStandBase getUserStand(LivingEntity user) {
        if (user == null) {
            return null;
        }
        List<EntityStandBase> stands = user.getWorld().getEntitiesByClass(EntityStandBase.class,
                user.getBoundingBox().expand(10, 6, 10),
                e -> e.getUser() != null && e.getUser() == user);
        return stands.isEmpty() ? null : stands.get(0);
    }

    /**
     * 替身放出超时的默认惩罚：续挂"替身在场"药水。
     * <p>还会在配置开启惩罚时施加惩罚药水，本地精简为只续精神药水（不至于超时即崩）。</p>
     */
    public static void potionDefaultOutOfTime(LivingEntity user) {
        if (user == null || PotionLoader.potionStand == null) {
            return;
        }
        StatusEffectInstance origin = user.getStatusEffect(PotionLoader.potionStand);
        if (origin != null) {
            if (origin.getDuration() < 400) {
                user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 400, origin.getAmplifier()));
            }
        } else {
            user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 600, 0));
        }
    }

    /**
     * 清除全部非正向（负面）药水效果。
     */
    public static void removeBadPotion(LivingEntity user) {
        if (user == null || user.getStatusEffects().isEmpty()) {
            return;
        }
        List<StatusEffectInstance> toRemove = new ArrayList<>();
        for (StatusEffectInstance effect : user.getStatusEffects()) {
            if (!effect.getEffectType().isBeneficial()) {
                toRemove.add(effect);
            }
        }
        for (StatusEffectInstance effect : toRemove) {
            user.removeStatusEffect(effect.getEffectType());
        }
    }

    /**
     * 延长"替身在场"药水时间。
     */
    public static void increaseStandTime(LivingEntity user, int ticks) {
        if (user == null || PotionLoader.potionStand == null) {
            return;
        }
        user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, ticks, 0));
    }

    /**
     * 延长指定药水效果时间。
     * <p>已有该效果则在剩余时长上追加 duration×buffer；没有则施加 duration×buffer 时长的
     * level-1 级效果。</p>
     */
    public static void increasePotionTime(LivingEntity user, String type, int duration, int level, int buffer) {
        if (user == null || type == null) {
            return;
        }
        StatusEffect potion = getPotion(type);
        if (potion == null) {
            return;
        }
        StatusEffectInstance origin = user.getStatusEffect(potion);
        if (origin != null) {
            user.addStatusEffect(new StatusEffectInstance(potion, origin.getDuration() + duration * buffer,
                    origin.getAmplifier()));
        } else {
            user.addStatusEffect(new StatusEffectInstance(potion, Math.max(duration * buffer, 0), Math.max(level - 1, 0)));
        }
    }

    /**
     * 获取玩家手持物品。主手传 true，副手传 false。
     */
    public static ItemStack getPlayerHoldItem(LivingEntity user, boolean mainHand) {
        if (user == null) {
            return ItemStack.EMPTY;
        }
        if (mainHand) {
            return user.getMainHandStack();
        }
        return user.getOffHandStack();
    }

    /**
     * 修复物品耐久（全量修复）。
     */
    public static void repairItem(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.isDamaged()) {
            stack.setDamage(0);
        }
    }

    /**
     * 按注册名播放音效。未注册的音效 id 安全忽略。
     */
    public static void playSound(Entity entity, String soundId, float volume, float pitch) {
        if (entity == null || soundId == null) {
            return;
        }
        Identifier sid = Identifier.tryParse(soundId);
        SoundEvent soundEvent = sid == null ? null : Registries.SOUND_EVENT.get(sid);
        if (soundEvent != null && soundEvent != SoundEvents.INTENTIONALLY_EMPTY) {
            if (entity.getWorld() != null && !entity.getWorld().isClient && entity instanceof LivingEntity) {
                // 服务端广播链路：Entity.playSound 内部调用的是 client-only 的 World.playSound 重载，
                // 服务端调用静默（疯狂钻石 idle 态 stand_up 语音无声根因）；改为与召唤音/
                // playStandSoundWithCooldown 一致的 HuajiSoundPlayer 服务端广播（pitch 固定 1.0，
                // 与全部脚本调用处 pitch=1 等价）
                HuajiSoundPlayer.playToNearbyClient((LivingEntity) entity, soundEvent, volume);
            } else {
                // 客户端侧保留原逻辑（客户端 World.playSound 重载存在，可正常播放）
                entity.playSound(soundEvent, volume, pitch);
            }
        }
    }

    /**
     * 获取物品注册名。
     */
    public static String getItemRegistryName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    /**
     * 发送系统消息。替身技能台词走聊天栏（普通消息），
     * 与状态 JS 的念写/波纹疾走文案保持一致，不再用 actionBar 顶部即时显示。
     */
    public static void sendMessage(LivingEntity user, String key) {
        if (user == null || key == null) {
            return;
        }
        Text message = Text.translatable(key);
        if (user instanceof PlayerEntity player) {
            player.sendMessage(message, false);
        } else {
            user.sendMessage(message);
        }
    }

    /**
     * 获取指定距离 + 视野角范围内的活体实体。
     * <p>返回原生数组，JS 侧以 {@code entities.length} / {@code entities[i]} 直接遍历。</p>
     */
    public static LivingEntity[] getRangeLiving(LivingEntity user, float distance, float degree) {
        if (user == null) {
            return new LivingEntity[0];
        }
        List<LivingEntity> list = new ArrayList<>();
        for (LivingEntity entity : user.getWorld().getEntitiesByClass(LivingEntity.class,
                user.getBoundingBox().expand(distance), e -> e != user)) {
            if (HAMathHelper.getDegreeXZ(user.getRotationVector(), HAMathHelper.getVectorEntityEye(user, entity)) > degree) {
                continue;
            }
            list.add(entity);
        }
        return list.toArray(new LivingEntity[0]);
    }

    /**
     * 治疗实体（加血不超过上限）。
     */
    public static void healEntity(LivingEntity target, int toHeal) {
        if (target != null && toHeal > 0) {
            target.heal(toHeal);
        }
    }

    /**
     * 判断实体是否需要治疗（当前血量不满上限）。
     * 用于治疗态技能避免对满血实体误触发治疗粒子/音效。
     */
    public static boolean isNeedHeal(LivingEntity target) {
        return target != null && target.getHealth() < target.getMaxHealth();
    }

    /**
     * 给玩家背包追加指定注册名的物品。
     */
    public static void addItemToplayer(LivingEntity user, String itemId, int amount) {
        if (user == null || itemId == null || amount <= 0 || !(user.getWorld() instanceof ServerWorld)) {
            return;
        }
        Item item = Registries.ITEM.get(Identifier.tryParse(itemId));
        if (item == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(item, amount);
        if (user instanceof PlayerEntity player) {
            player.getInventory().offerOrDrop(stack);
        } else {
            user.getWorld().spawnEntity(
                    new ItemEntity(user.getWorld(), user.getX(), user.getY() + 0.6, user.getZ(), stack));
        }
    }

    /**
     * 给予玩家命令唱片，带当前使用者身份 + 命令类型。
     */
    public static void giveDisc(LivingEntity user, String type) {
        if (user == null || type == null || ItemLoader.discCommand == null
                || !(user.getWorld() instanceof ServerWorld)) {
            return;
        }
        ItemStack disc = new ItemStack(ItemLoader.discCommand);
        ItemDiscCommand.setOwner(disc, user.getName().getString(), user.getUuidAsString());
        ItemDiscCommand.setCommandType(disc, type);
        if (user instanceof PlayerEntity player) {
            player.getInventory().offerOrDrop(disc);
        } else {
            user.getWorld().spawnEntity(
                    new ItemEntity(user.getWorld(), user.getX(), user.getY() + 0.6, user.getZ(), disc));
        }
    }

    /**
     * 消耗物品数量。
     */
    public static void consumeItem(ItemStack stack, int count) {
        if (stack != null && !stack.isEmpty() && count > 0) {
            stack.decrement(count);
        }
    }

    /**
     * 念写（对齐 TelepathyHelper.telepathizeItem 语义 + 扩展 1.20.1 结构）。
     * <p> JS 在主手持 expensive_camera 时调用本方法：取副手物品的注册名映射
     * 到结构类型，经 WorldServer.findNearestStructure 探测该结构最近坐标并输出
     * "***x***y***z***"；副手为空时本工程扩展为探测当前维度内最近的支持结构。
     * 1.20.1 改用 ChunkGenerator.locateStructure 批量定位（RegistryEntryList 可
     * 一次搜索多个结构，副手空场景只需一次调用），输出三段式聊天消息：
     * "念写：" + 结构名（lang telepathy.result.position.<type>）+ 坐标。</p>
     */
    public static void telepathizeItem(LivingEntity user, ItemStack stack) {
        if (user == null || !(user.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        String itemId = (stack == null || stack.isEmpty())
                ? "" : Registries.ITEM.getId(stack.getItem()).toString();
        Pair<BlockPos, RegistryKey<Structure>> found;
        String suffix;
        if (itemId.isEmpty()) {
            // 副手无物品：探测当前维度内最近的支持结构（全候选一次批量定位）
            found = findNearestStructure(sw, user.getBlockPos(), allDimensionStructures(sw));
            suffix = found == null ? null : telepathyPositionSuffix(found.getSecond());
        } else {
            TelepathyTarget target = TELEPATHY_ITEMS.get(itemId);
            if (target == null) {
                sendMessage(user, "stand.huajiager.skill.huajiager.hermit_purple.telepathy.item_null");
                return;
            }
            if (!target.dimension.matches(sw.getRegistryKey())) {
                sendMessage(user, "telepathy.result.position.dimension.miss");
                return;
            }
            found = findNearestStructure(sw, user.getBlockPos(), target.keys);
            suffix = telepathyPositionSuffix(target.keys.get(0));
        }
        if (found == null) {
            sendMessage(user, "telepathy.result.position.dimension.miss");
            return;
        }
        BlockPos pos = found.getFirst();
        Text message = Text.translatable("stand.huajiager.skill.huajiager.hermit_purple.telepathy")
                .copy()
                .append(Text.translatable("telepathy.result.position." + suffix))
                .append(Text.literal("***" + pos.getX() + "***" + pos.getY() + "***" + pos.getZ() + "***"));
        if (user instanceof PlayerEntity player) {
            player.sendMessage(message, false);
        } else {
            user.sendMessage(message);
        }
    }

    /** 念写结构搜索半径（区块单位，与 findNearestStructure 默认范围一致） */
    private static final int TELEPATHY_RADIUS = 100;

    /** 念写目标结构：结构归属维度 + 候选结构 key 列表 */
    private record TelepathyTarget(TelepathyDimension dimension, List<RegistryKey<Structure>> keys) {
    }

    /** 念写结构维度 */
    private enum TelepathyDimension {
        OVERWORLD(World.OVERWORLD), NETHER(World.NETHER), END(World.END);

        private final RegistryKey<World> key;

        TelepathyDimension(RegistryKey<World> key) {
            this.key = key;
        }

        boolean matches(RegistryKey<World> worldKey) {
            return worldKey == this.key || worldKey.equals(this.key);
        }
    }

    /**
     * 念写物品注册名 → 目标结构。
     * <p>前 8 项为 映射（金块/绿宝石/绿宝石块/末影之眼/信标/末影水晶/
     * 地狱砖块/矿车），后 11 项为 1.20.1 新增结构选配的探测物品：
     * 弩-掠夺者前哨站（掠夺者标志武器）、橡木船-沉船、海洋之心-埋藏的宝藏、
     * 海晶碎片-海底废墟（废墟建材）、雪球-雪屋、发酵蛛眼-沼泽小屋（女巫掉落）、
     * 打火石-废弃传送门（点火激活）、金锭-猪灵堡垒（猪灵钟爱）、骨头-下界化石、
     * 回响碎片-远古城市（古城专属掉落）、刷子-古迹废墟（1.20 考古工具）。</p>
     */
    private static final Map<String, TelepathyTarget> TELEPATHY_ITEMS = Map.ofEntries(
            // ——  念写映射 ——
            Map.entry("minecraft:gold_block", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.DESERT_PYRAMID))),
            Map.entry("minecraft:emerald", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.VILLAGE_PLAINS, StructureKeys.VILLAGE_DESERT,
                            StructureKeys.VILLAGE_SAVANNA, StructureKeys.VILLAGE_SNOWY,
                            StructureKeys.VILLAGE_TAIGA))),
            Map.entry("minecraft:emerald_block", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.MANSION))),
            Map.entry("minecraft:ender_eye", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.STRONGHOLD))),
            Map.entry("minecraft:beacon", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.MONUMENT))),
            Map.entry("minecraft:end_crystal", new TelepathyTarget(TelepathyDimension.END,
                    List.of(StructureKeys.END_CITY))),
            Map.entry("minecraft:nether_brick", new TelepathyTarget(TelepathyDimension.NETHER,
                    List.of(StructureKeys.FORTRESS))),
            Map.entry("minecraft:minecart", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.MINESHAFT, StructureKeys.MINESHAFT_MESA))),
            // —— 1.20.1 新增结构 ——
            Map.entry("minecraft:crossbow", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.PILLAGER_OUTPOST))),
            Map.entry("minecraft:oak_boat", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.SHIPWRECK, StructureKeys.SHIPWRECK_BEACHED))),
            Map.entry("minecraft:heart_of_the_sea", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.BURIED_TREASURE))),
            Map.entry("minecraft:prismarine_shard", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.OCEAN_RUIN_COLD, StructureKeys.OCEAN_RUIN_WARM))),
            Map.entry("minecraft:snowball", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.IGLOO))),
            Map.entry("minecraft:fermented_spider_eye", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.SWAMP_HUT))),
            Map.entry("minecraft:flint_and_steel", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.RUINED_PORTAL, StructureKeys.RUINED_PORTAL_DESERT,
                            StructureKeys.RUINED_PORTAL_JUNGLE, StructureKeys.RUINED_PORTAL_SWAMP,
                            StructureKeys.RUINED_PORTAL_MOUNTAIN, StructureKeys.RUINED_PORTAL_OCEAN))),
            Map.entry("minecraft:gold_ingot", new TelepathyTarget(TelepathyDimension.NETHER,
                    List.of(StructureKeys.BASTION_REMNANT))),
            Map.entry("minecraft:bone", new TelepathyTarget(TelepathyDimension.NETHER,
                    List.of(StructureKeys.NETHER_FOSSIL))),
            Map.entry("minecraft:echo_shard", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.ANCIENT_CITY))),
            Map.entry("minecraft:brush", new TelepathyTarget(TelepathyDimension.OVERWORLD,
                    List.of(StructureKeys.TRAIL_RUINS))));

    /** 念写定位：在 TELEPATHY_RADIUS 半径内批量搜索给定结构列表，返回最近结构与命中 key */
    private static Pair<BlockPos, RegistryKey<Structure>> findNearestStructure(
            ServerWorld sw, BlockPos pos, List<RegistryKey<Structure>> keys) {
        Registry<Structure> registry = sw.getRegistryManager().get(RegistryKeys.STRUCTURE);
        List<RegistryEntry<Structure>> entries = new ArrayList<>();
        for (RegistryKey<Structure> key : keys) {
            registry.getEntry(key).ifPresent(entries::add);
        }
        if (entries.isEmpty()) {
            return null;
        }
        RegistryEntryList<Structure> list = RegistryEntryList.of(entries);
        Pair<BlockPos, RegistryEntry<Structure>> found =
                sw.getChunkManager().getChunkGenerator()
                        .locateStructure(sw, list, pos, TELEPATHY_RADIUS, false);
        if (found == null) {
            return null;
        }
        return new Pair<>(found.getFirst(), found.getSecond().getKey().orElse(null));
    }

    /** 当前维度可念写的全部结构候选（副手为空时探测"最近结构"） */
    private static List<RegistryKey<Structure>> allDimensionStructures(ServerWorld sw) {
        RegistryKey<World> worldKey = sw.getRegistryKey();
        List<RegistryKey<Structure>> list = new ArrayList<>();
        for (TelepathyTarget target : TELEPATHY_ITEMS.values()) {
            if (target.dimension.matches(worldKey)) {
                for (RegistryKey<Structure> key : target.keys) {
                    if (!list.contains(key)) {
                        list.add(key);
                    }
                }
            }
        }
        return list;
    }

    /** 结构 key → lang 后缀 */
    private static String telepathyPositionSuffix(RegistryKey<Structure> key) {
        if (key == StructureKeys.VILLAGE_PLAINS || key == StructureKeys.VILLAGE_DESERT
                || key == StructureKeys.VILLAGE_SAVANNA || key == StructureKeys.VILLAGE_SNOWY
                || key == StructureKeys.VILLAGE_TAIGA) {
            return "village";
        }
        if (key == StructureKeys.DESERT_PYRAMID || key == StructureKeys.JUNGLE_PYRAMID) {
            return "temple";
        }
        if (key == StructureKeys.MANSION) {
            return "mansion";
        }
        if (key == StructureKeys.MONUMENT) {
            return "monument";
        }
        if (key == StructureKeys.MINESHAFT || key == StructureKeys.MINESHAFT_MESA) {
            return "mineshaft";
        }
        if (key == StructureKeys.STRONGHOLD) {
            return "enderPortal";
        }
        if (key == StructureKeys.FORTRESS) {
            return "fortress";
        }
        if (key == StructureKeys.END_CITY) {
            return "endCity";
        }
        if (key == StructureKeys.PILLAGER_OUTPOST) {
            return "pillager_outpost";
        }
        if (key == StructureKeys.SHIPWRECK || key == StructureKeys.SHIPWRECK_BEACHED) {
            return "shipwreck";
        }
        if (key == StructureKeys.BURIED_TREASURE) {
            return "buried_treasure";
        }
        if (key == StructureKeys.OCEAN_RUIN_COLD || key == StructureKeys.OCEAN_RUIN_WARM) {
            return "ocean_ruin";
        }
        if (key == StructureKeys.IGLOO) {
            return "igloo";
        }
        if (key == StructureKeys.SWAMP_HUT) {
            return "swamp_hut";
        }
        if (key == StructureKeys.RUINED_PORTAL || key == StructureKeys.RUINED_PORTAL_DESERT
                || key == StructureKeys.RUINED_PORTAL_JUNGLE || key == StructureKeys.RUINED_PORTAL_SWAMP
                || key == StructureKeys.RUINED_PORTAL_MOUNTAIN || key == StructureKeys.RUINED_PORTAL_OCEAN
                || key == StructureKeys.RUINED_PORTAL_NETHER) {
            return "ruined_portal";
        }
        if (key == StructureKeys.BASTION_REMNANT) {
            return "bastion_remnant";
        }
        if (key == StructureKeys.NETHER_FOSSIL) {
            return "nether_fossil";
        }
        if (key == StructureKeys.ANCIENT_CITY) {
            return "ancient_city";
        }
        if (key == StructureKeys.TRAIL_RUINS) {
            return "trail_ruins";
        }
        return null;
    }

    /**
     * 是否处于隐者之紫波纹疾走（OVERDRIVE 蓄力）形态：用户替身为 hermit_purple
     * 且蓄力标签为 OVER_DRIVE。该形态下命中伤害改用 overdrive_hit 死亡提示。
     */
    private static boolean isHermitOverdrive(LivingEntity user) {
        StandHandler handler = StandUtil.getStandHandler(user);
        return handler != null && HuajiConstant.BuffTags.OVER_DRIVE.equals(handler.getBuffTag());
    }

}
