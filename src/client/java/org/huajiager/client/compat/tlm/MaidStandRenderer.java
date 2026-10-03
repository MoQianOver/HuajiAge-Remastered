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
        return true;
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
