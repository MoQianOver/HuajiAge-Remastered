package org.huajiager.client.compat.tlm;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import org.huajiager.capability.IExposedData;
import org.huajiager.compat.tlm.MaidBallHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

/**
 * 女仆替身的渲染：直接借用车万自己的女仆渲染器。
 *
 * <p>车万的女仆模型是 geckolib（基岩动画模型），我们的替身管线吃的是普通基岩几何，两者不通用。
 * 因此这里临时造一只只带 model id 的"哑女仆"，摆到替身位置后交给车万的渲染器绘制：
 * 模型与皮肤都是女仆本人，且不需要新增任何美术。</p>
 */
public final class MaidStandRenderer {

    /** 复用的哑女仆：同一客户端世界内复用，换世界或换模型时重建。 */
    private static EntityMaid dummy;
    private static ClientWorld dummyWorld;
    private static String dummyModelId;

    private MaidStandRenderer() {
    }

    /** 返回 true 表示已由车万渲染器画完，调用方不要再走自带模型流程。 */
    public static boolean renderStand(EntityStandBase entity, float yaw, float tickDelta, MatrixStack matrices,
                                      VertexConsumerProvider vcp, int light) {
        if (!FabricLoader.getInstance().isModLoaded("touhou_little_maid")) {
            return false;
        }
        StandBase stand = entity.getStand();
        if (stand == null || !MaidBallHelper.MAID_STAND.equals(stand.getName())) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) {
            return false;
        }
        String modelId = maidModelId(entity);
        if (modelId == null) {
            return false;
        }
        if (dummy == null || dummyWorld != world || !modelId.equals(dummyModelId)) {
            dummy = new EntityMaid(world);
            dummy.setModelId(modelId);
            dummyWorld = world;
            dummyModelId = modelId;
        }
        // 摆到替身当前位置与朝向，再以本地坐标 (0,0,0) 交给车万渲染器
        dummy.setPosition(entity.getX(), entity.getY(), entity.getZ());
        // 同步插值用的上一帧/上一渲染坐标，否则渲染位置会在旧坐标与现坐标之间插值（表现为"跟不上"）
        dummy.prevX = entity.getX();
        dummy.prevY = entity.getY();
        dummy.prevZ = entity.getZ();
        dummy.lastRenderX = entity.getX();
        dummy.lastRenderY = entity.getY();
        dummy.lastRenderZ = entity.getZ();
        dummy.prevYaw = entity.prevYaw;
        dummy.setYaw(entity.getYaw());
        dummy.prevBodyYaw = entity.prevBodyYaw;
        dummy.bodyYaw = entity.bodyYaw;
        dummy.prevHeadYaw = entity.prevHeadYaw;
        dummy.headYaw = entity.headYaw;
        client.getEntityRenderDispatcher().render(dummy, 0.0, 0.0, 0.0, yaw, tickDelta, matrices, vcp, light);
        playSummonVoiceIfNeeded(entity);
        return true;
    }

    /** 已播过召唤语音的替身，避免每帧重复播放。 */
    private static final java.util.Set<Integer> SUMMON_VOICED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 某只女仆替身首次出现时，让她说一句车万驯服语音。 */
    private static void playSummonVoiceIfNeeded(EntityStandBase entity) {
        if (!SUMMON_VOICED.add(entity.getId())) {
            return;
        }
        // 召唤音随机取一条女仆环境/战斗语音，与技能用的驯服语音区分开
        // （数组在方法内构造，避免类初始化阶段就触碰车万的类）
        net.minecraft.sound.SoundEvent[] voices = {
                com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_IDLE,
                com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_ATTACK,
                com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_DANMAKU_ATTACK,
                com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_FIND_TARGET,
                com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_SNOW
        };
        playVoice(voices[java.util.concurrent.ThreadLocalRandom.current().nextInt(voices.length)], entity);
    }

    /**
     * 技能语音：技能确认包到达宿主本机后调用，让当前女仆替身说一句车万驯服语音。
     *
     * <p>发声本身复用渲染时创建的哑女仆实例，这里只用 owner 找到该玩家的替身实体；
     * 还没渲染过、或找不到替身实体时静默返回 false。</p>
     */
    public static boolean playSkillVoice(net.minecraft.entity.LivingEntity owner) {
        EntityStandBase standEntity = org.huajiager.stand.helper.StandPowerHelper.getUserStand(owner);
        if (standEntity == null) {
            return false;
        }
        return playVoice(com.github.tartaricacid.touhoulittlemaid.init.InitSounds.MAID_TAMED, standEntity);
    }

    /**
     * 让哑女仆发声：必须走车万的 {@code MaidSoundInstance}。
     *
     * <p>车万女仆语音的 43 条 sounds.json 条目全部指向空占位 {@code maid/empty}，
     * 原版 SoundEvent 广播必然静音；女仆本人说话走的是车万自己的音频系统
     * （CustomSoundLoader 缓存 + MaidSoundInstance），车万的音效包 GUI 也是这么播的。</p>
     */
    public static boolean playVoice(net.minecraft.sound.SoundEvent event, EntityStandBase entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (event == null || client.getSoundManager() == null || dummy == null) {
            return false;
        }
        try {
            client.getSoundManager().play(
                    new com.github.tartaricacid.touhoulittlemaid.client.sound.data.MaidSoundInstance(
                            event, dummy.getSoundPackId(), dummy, true));
            return true;
        } catch (Throwable t) {
            // 车万缺席时会抛 NoClassDefFoundError（属 Error 而非 RuntimeException），必须一并兜住
            return false;
        }
    }

    /** 替身数据里存的模型 id（球转替身时写入），去掉状态后缀即车万的 model id。 */
    private static String maidModelId(EntityStandBase entity) {
        IExposedData data = StandUtil.getStandData(entity.getUser());
        if (data == null) {
            return null;
        }
        String model = data.getModel();
        if (model == null || model.isEmpty()) {
            return null;
        }
        for (String suffix : new String[] { "_default", "_idle", "_heal", "_punch", "_overdrive", "_fly" }) {
            if (model.endsWith(suffix)) {
                model = model.substring(0, model.length() - suffix.length());
                break;
            }
        }
        return model.isEmpty() ? null : model;
    }
}
