package org.huajiager.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.attachment.Attachments;
import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.network.StandNetWorkHandler;
import org.huajiager.stand.custom.StandCustom;
import org.huajiager.stand.custom.StandCustomInfo;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.messages.SyncExposedStandDataMessage;
import org.huajiager.stand.states.StandStateBase;

/**
 * 替身通用工具类。
 *
 * 已按依赖就绪情况迁入常用方法（getType / getStandData / getStandHandler /
 * standEffectLoad / getTypeWithIndex / getArrowStands 等）；其余重型方法
 * （getCustomStands / getTagStands / getDiscTex / getStandByEntity 等）
 * 依赖客户端资源加载与 StandCustom，当前未提供。
 */
public final class StandUtil {

    private static final String KEY_BUFF_TIME = "stand_buff_time";

    private StandUtil() {
    }

    /**
     * 获取实体当前替身类型。
     */
    public static StandBase getType(LivingEntity user) {
        IExposedData data = getStandData(user);
        if (data == null) {
            return null;
        }
        String name = data.getStand();
        if (name == null || name.isEmpty() || name.equals(StandLoader.EMPTY)) {
            return null;
        }
        return StandLoader.getStand(name);
    }

    /**
     * 判断实体替身是否与给定替身匹配。
     */
    public static boolean isStandMatch(LivingEntity user, StandBase stand) {
        if (user == null || stand == null) {
            return false;
        }
        IExposedData data = getStandData(user);
        return data != null && stand.getName().equals(data.getStand());
    }

    /**
     * 获取替身展示名（lang key，如 stand.huajiager.hierophant_green）。
     * 与 StandBase#getLocalName / ItemArrowStand 的 Text.translatable 调用约定保持一致，
     * 客户端需对返回值做 translatable 二次翻译成中文（如 "Hierophant Green-绿之法皇"）。
     */
    public static String getLocalName(StandBase stand) {
        return stand == null ? StandLoader.EMPTY
                : (stand.getLocalName() != null ? stand.getLocalName() : stand.getName());
    }

    /**
     * 获取替身能量处理器。
     *
     * 注意：STAND_HANDLER 为 createDefaulted attachment，若始终用 getAttached 读取，
     * 未显式 set 时返回 initializer 每次新建的临时实例（不落库）——回充（MPCharge）
     * 与召唤（canBeCost/cost）会分别落在不同临时对象上，能量永远归零。此处统一改为
     * getAttachedOrCreate，首次读取即把实例真正挂到实体 storage，使回充在实体内累积。
     */
    public static StandHandler getStandHandler(LivingEntity user) {
        return user.getAttachedOrCreate(Attachments.STAND_HANDLER);
    }

    /**
     * 获取替身外露附属数据。
     *
     * 注意：STAND_DATA 为 createDefaulted attachment，未显式 set 时
     * {@code getAttached} 返回 initializer 每次新建的临时实例（不落库）。
     * 读取型判定（getType / getStage 等）走本方法即足够；凡是需要
     * setStand 等真正写入的路径，必须改用 {@link #getOrCreateStandData}，
     * 否则写的只是临时对象，替身不会持久到实体 storage。
     */
    public static IExposedData getStandData(LivingEntity user) {
        return user.getAttached(Attachments.STAND_DATA);
    }

    /**
     * 获取（必要时创建并挂载）替身外露附属数据（独立编写，Fabric 适配）。
     *
     * 通过 {@code getAttachedOrCreate} 把实例真正 attach 到实体 storage，
     * 供替身写入路径（觉醒之箭 / 卡其托里钛等）使用——只有落库的实例
     * 才能保证后续 getAttached 读到同一对象、替身名跨 tick / 跨存档保留。
     */
    public static IExposedData getOrCreateStandData(LivingEntity user) {
        return user.getAttachedOrCreate(Attachments.STAND_DATA);
    }

    /**
     * 将服务端替身数据同步给指定玩家客户端（觉醒成功 / 玩家重进世界时调用）。
     *
     * 客户端 EventStandKey 依赖 local player 的 STAND_DATA 判定是否发送召唤包，
     * 而 persistent attachment 默认实例不会自动 attach，必须收到本同步包后才能就绪。
     */
    public static void syncStandData(ServerPlayerEntity player) {
        IExposedData data = getStandData(player);
        if (data == null) {
            data = getOrCreateStandData(player);
        }
        if (data == null) {
            return;
        }
        StandNetWorkHandler.sendTo(player, new SyncExposedStandDataMessage(
                data.getStand(), data.getStage(), data.isTriggered(), data.isHandDisplay(),
                data.getState(), data.getModel(), player.getGameProfile().getName(), true));
    }

    /**
     * 玩家重进世界时重建替身展示实体。
     *
     * 替身实体在宿主登出瞬间会被 EntityStandBase.tick 判定 user==null 而 discard
     * （getUser 只在服务器在线玩家列表里找宿主），重进后实体已不存在，客户端只收到
     * 数据同步（isTriggered=true），表现为"召唤过替身，退出重进就看不见替身"。
     * 本方法在玩家 JOIN 后按与 MessageStandUp 相同口径原地重建，先查重避免重复 spawn。
     */
    public static void spawnStandEntityIfMissing(ServerPlayerEntity player) {
        IExposedData data = getStandData(player);
        StandBase stand = getType(player);
        if (data == null || stand == null || !data.isTriggered()) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        if (world == null) {
            return;
        }
        Entity existing = world.getEntitiesByType(EntityStandBase.TYPE_ENTITY,
                        e -> e.getUser() == player)
                .stream().findAny().orElse(null);
        if (existing != null) {
            return;
        }
        EntityStandBase standBase = new EntityStandBase(EntityStandBase.TYPE_ENTITY, world);
        standBase.setUser(player.getUuid().toString());
        standBase.setUserName(player.getGameProfile().getName());
        standBase.setPosition(player.getX(), player.getY(), player.getZ());
        standBase.setType(StandLoader.getStand(data.getStand()) != null ? data.getStand() : StandLoader.EMPTY);
        // 重建实体时同步状态机名：DataTracker 默认值为 DEFAULT，不写会有一帧攻击态闪现
        // （替身实际处于闲置态时旁观者会先看到攻击模型再跳回闲置）。
        standBase.setStandState(data.getState());
        if (StandStates.getStandState(stand.getName(), data.getState()) instanceof StandStateBase base
                && base.hasExtraData(EnumStandTag.StateTags.RIDE.getName())) {
            standBase.setEntity(true);
        }
        world.spawnEntity(standBase);
    }

    /**
     * 当前蓄力值。
     */
    public static int getCharge(LivingEntity user) {
        StandHandler handler = getStandHandler(user);
        return handler == null ? 0 : handler.getChargeValue();
    }

    /**
     * 蓄力上限。
     */
    public static int getChargeMax(LivingEntity user) {
        StandHandler handler = getStandHandler(user);
        return handler == null ? 0 : handler.getMaxValue();
    }

    /**
     * 设置蓄力上限。
     */
    public static void setChargeMax(LivingEntity user, int value) {
        StandHandler handler = getStandHandler(user);
        if (handler != null) {
            handler.setMaxValue(value);
        }
    }

    /**
     * 获取替身阶段。
     */
    public static int getStandStage(LivingEntity user) {
        IExposedData data = getStandData(user);
        return data == null ? 0 : data.getStage();
    }

    /**
     * 设置替身阶段。
     */
    public static void setStandStage(LivingEntity user, int stage) {
        IExposedData data = getStandData(user);
        if (data != null) {
            data.setStage(stage);
        }
    }

    /**
     * 获取替身状态机当前状态名。
     */
    public static String getStandState(LivingEntity user) {
        IExposedData data = getStandData(user);
        return data == null ? ExposedData.States.DEFAULT.getName() : data.getState();
    }

    /**
     * 设置替身状态机当前状态名。
     */
    public static void setStandState(LivingEntity user, String state) {
        IExposedData data = getStandData(user);
        if (data != null) {
            data.setState(state);
        }
    }

    /**
     * 获取替身出场时间（倒计时），未触发时为 -1。
     */
    @SuppressWarnings("unchecked")
    public static int getStandBuffTime(LivingEntity user) {
        Map<String, Object> data = user.getAttached(Attachments.ENTITY_DATA);
        if (data == null || !data.containsKey(KEY_BUFF_TIME)) {
            return -1;
        }
        Object v = data.get(KEY_BUFF_TIME);
        return v instanceof Number ? ((Number) v).intValue() : -1;
    }

    /**
     * 替身状态激活时的特效/药水加载：为实体附加替身专属药水。
     * 还会通过 HuajiSoundPlayer 播放再激活音效、ServerUtil 广播粒子（MessageParticleGenerator）。
     */
    public static void standEffectLoad(LivingEntity entity, boolean isLoadEffect) {
        if (entity.getStatusEffect(PotionLoader.potionStand) == null) {
            StandBase stand = StandUtil.getType(entity);
            if (stand != null) {
                entity.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 60, 0));
                if (isLoadEffect) {
                    // HuajiSoundPlayer.playSound(...); ServerUtil.packetSendToAll(
                    // new MessageParticleGenerator(entity.posX, entity.posY + 0.5, entity.posZ, false));
                }
            }
        }
        StatusEffectInstance effect = entity.getStatusEffect(PotionLoader.potionStand);
        if (effect != null && effect.getDuration() < 20) {
            entity.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 60, 0));
        }
    }

    /** 自定义替身用来声明「可被觉醒之箭抽到」的 standTags 标签名。 */
    private static final String ARROW_STAND_TAG = "arrow";

    /** 觉醒之箭默认可抽到的原生替身：镇魂曲只能由奥尔加进化路线获得，不入抽取池。 */
    private static final List<StandBase> ARROW_STANDS_NATIVE = List.of(
            StandLoader.THE_WORLD, StandLoader.STAR_PLATINUM,
            StandLoader.HIEROPHANT_GREEN, StandLoader.KILLER_QUEEN);

    /** 觉醒之箭抽取池：原生白名单 + standTags 声明 {@link #ARROW_STAND_TAG} 的自定义替身。 */
    public static List<StandBase> getArrowStands() {
        List<StandBase> pool = new ArrayList<>(ARROW_STANDS_NATIVE);
        for (StandBase stand : StandLoader.STAND_LIST) {
            if (stand instanceof StandCustom custom && custom.getInfo() != null) {
                List<String> tags = custom.getInfo().getStandTags();
                if (tags != null && tags.contains(ARROW_STAND_TAG)) {
                    pool.add(stand);
                }
            }
        }
        return pool;
    }

    /** 按索引在觉醒之箭抽取池里取替身，index 按池大小取模。 */
    public static StandBase getTypeWithIndex(int index) {
        List<StandBase> pool = getArrowStands();
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(Math.floorMod(index, pool.size()));
    }

    /** 自定义替身召唤音池：JSON sounds 里能解析成已注册音效的条目（无效 id 跳过）。 */
    public static List<SoundEvent> getCustomStandSounds(StandBase stand) {
        List<SoundEvent> result = new ArrayList<>();
        StandCustomInfo info = customInfoOf(stand);
        if (info == null || info.getSounds() == null) {
            return result;
        }
        for (String id : info.getSounds()) {
            SoundEvent sound = resolveSound(id);
            if (sound != null) {
                result.add(sound);
            }
        }
        return result;
    }

    /** 自定义替身循环音池：JSON sounds_repeat 的「音效id-音量」条目。 */
    public static List<RepeatSound> getCustomStandRepeatSounds(StandBase stand) {
        List<RepeatSound> result = new ArrayList<>();
        StandCustomInfo info = customInfoOf(stand);
        if (info == null || info.getSoundsRepeat() == null) {
            return result;
        }
        for (String entry : info.getSoundsRepeat()) {
            if (entry == null) {
                continue;
            }
            int split = entry.lastIndexOf('-');
            if (split <= 0 || split == entry.length() - 1) {
                continue;
            }
            SoundEvent sound = resolveSound(entry.substring(0, split));
            if (sound == null) {
                continue;
            }
            try {
                result.add(new RepeatSound(sound, Float.parseFloat(entry.substring(split + 1))));
            } catch (NumberFormatException ignored) {
                // 音量解析失败的条目跳过，不影响其余音效
            }
        }
        return result;
    }

    /** 替身碟片贴图：自定义替身走 JSON 的 disc 字段，原生替身走 textures/item/disc/disc_<name>.png。 */
    public static Identifier getDiscTex(StandBase stand) {
        if (stand == null) {
            return Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/item/disc/disc_null.png");
        }
        StandCustomInfo info = customInfoOf(stand);
        if (info != null && info.getDisc() != null && !info.getDisc().isEmpty()) {
            Identifier disc = Identifier.tryParse(info.getDisc());
            String path = disc != null ? disc.getPath() : info.getDisc();
            return Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/item/" + path + ".png");
        }
        return Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/item/disc/disc_" + stand.getName() + ".png");
    }

    private static StandCustomInfo customInfoOf(StandBase stand) {
        return stand instanceof StandCustom custom ? custom.getInfo() : null;
    }

    private static SoundEvent resolveSound(String id) {
        Identifier identifier = Identifier.tryParse(id == null ? "" : id);
        return identifier == null ? null : Registries.SOUND_EVENT.get(identifier);
    }

    /** 自定义替身循环音条目：音效 + JSON 里声明的音量。 */
    public record RepeatSound(SoundEvent sound, float volume) {
    }
}
