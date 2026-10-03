package org.huajiager.client.compat.tlm;

import java.util.Optional;

import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;

import net.minecraft.util.Identifier;

/**
 * 女仆替身的贴图解析：直接用车万女仆本人那张皮肤。
 *
 * <p>随模组分发的 maid 贴图只是 Blockbench 的 UV 模板（原版那个可选资源包里就是占位图），
 * 拿来当皮肤会满身参考线与色块；这里改读车万自己已经加载好的皮肤资源，无需新增任何美术。</p>
 */
public final class MaidSkinTextures {

    /** 取不到女仆自身皮肤时回落用的车万内置默认模型。 */
    private static final String DEFAULT_MAID_MODEL = "touhou_little_maid:hakurei_reimu";

    private MaidSkinTextures() {
    }

    /**
     * 按替身数据里的模型 id 解析女仆皮肤。
     *
     * @param modelId 替身数据里的模型 id（车万 id，或我们自带的几何 id）
     * @return 可直接用于渲染的贴图；取不到时返回 null，调用方继续走原有贴图链
     */
    public static Identifier resolve(String modelId) {
        String maidModel = toMaidModelId(modelId);
        Identifier texture = maidModel == null ? null : lookup(maidModel);
        if (texture == null) {
            texture = lookup(DEFAULT_MAID_MODEL);
        }
        return texture;
    }

    /** 剔除替身状态后缀，得到车万的 model id；不是车万的 id 则返回 null。 */
    private static String toMaidModelId(String modelId) {
        if (modelId == null || !modelId.startsWith("touhou_little_maid:")) {
            return null;
        }
        String id = modelId;
        for (String suffix : new String[] { "_default", "_idle", "_heal", "_punch", "_overdrive", "_fly" }) {
            if (id.endsWith(suffix)) {
                id = id.substring(0, id.length() - suffix.length());
                break;
            }
        }
        return id;
    }

    private static Identifier lookup(String maidModel) {
        try {
            Optional<MaidModelInfo> info = CustomPackLoader.MAID_MODELS.getInfo(maidModel);
            return info.map(MaidModelInfo::getTexture).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
