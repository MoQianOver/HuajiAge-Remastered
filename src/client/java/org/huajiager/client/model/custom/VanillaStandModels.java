package org.huajiager.client.model.custom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.util.Identifier;

/**
 * 可借用的原版实体模型表：替身状态把 modelId 写成 {@code minecraft:<实体>}（如
 * {@code minecraft:warden}）时，直接用原版烘焙好的模型与贴图渲染，不需要资源包提供几何。
 *
 * <p>模型在首次取用时从模型加载器构建并缓存；资源重载后由 {@link #clear()} 清空，
 * 下一次取用时拿到的是重载后的新模型树。</p>
 */
public final class VanillaStandModels {

    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaStandModels.class);

    /** modelId → 模型层与贴图。 */
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private static final Map<String, VanillaStandModel> BUILT = new HashMap<>();

    private static EntityModelLoader modelLoader;

    static {
        ENTRIES.put("minecraft:warden", new Entry(EntityModelLayers.WARDEN,
                Identifier.of("minecraft", "textures/entity/warden/warden.png")));
    }

    private VanillaStandModels() {
    }

    /** 记录客户端模型加载器（渲染器构造时调用），供按需构建原版模型树。 */
    public static void bind(EntityModelLoader loader) {
        modelLoader = loader;
    }

    /** 资源重载后清空缓存：下次取用会重新构建。 */
    public static void clear() {
        BUILT.clear();
    }

    /** 按状态声明的 modelId 取原版模型；未登记或构建失败返回 null。 */
    public static VanillaStandModel find(String stateModelId) {
        Entry entry = entryOf(stateModelId);
        if (entry == null || modelLoader == null) {
            return null;
        }
        String key = keyOf(stateModelId);
        VanillaStandModel cached = BUILT.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            VanillaStandModel built = new VanillaStandModel(modelLoader.getModelPart(entry.layer()));
            BUILT.put(key, built);
            LOGGER.info("[HuajiAge] Vanilla stand model borrowed: {} (bones={})", key, built.boneCount());
            return built;
        } catch (Exception e) {
            LOGGER.error("[HuajiAge] Failed to borrow vanilla stand model: {}", key, e);
            return null;
        }
    }

    /** 按状态声明的 modelId 取原版贴图；未登记返回 null。 */
    public static Identifier texture(String stateModelId) {
        Entry entry = entryOf(stateModelId);
        return entry == null ? null : entry.texture();
    }

    private static Entry entryOf(String stateModelId) {
        String key = keyOf(stateModelId);
        return key == null ? null : ENTRIES.get(key);
    }

    /** 状态声明的 modelId 形如 {@code minecraft:warden_default}，按登记表的前缀匹配取回登记键。 */
    private static String keyOf(String stateModelId) {
        if (stateModelId == null || stateModelId.isEmpty()) {
            return null;
        }
        if (ENTRIES.containsKey(stateModelId)) {
            return stateModelId;
        }
        for (String key : ENTRIES.keySet()) {
            if (stateModelId.startsWith(key + "_")) {
                return key;
            }
        }
        return null;
    }

    /** 一项可借用的原版模型。 */
    public record Entry(EntityModelLayer layer, Identifier texture) {
    }
}
