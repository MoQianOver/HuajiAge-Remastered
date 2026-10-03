package org.huajiager.client.model.custom;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * 自定义替身模型加载器：随客户端资源重载，扫描**所有命名空间**的
 * {@code assets/&lt;ns&gt;/stand_model.json}，按其中的 model_id 约定去同命名空间取
 * {@code models/entity/&lt;path&gt;.json} 几何文件并构建运行时模型。
 *
 * <p>查表 key 与替身状态自己声明的 modelId 一致（{@code model_id + "_" + state}），
 * 因此渲染端只需把状态的 modelId 传进来即可，不需要再做后缀猜测。几何在重载期一次性
 * 构建完成，渲染期只查表；构建失败会留下日志并标记，不会每帧重试。</p>
 */
public final class CustomModelLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomModelLoader.class);
    private static final Gson GSON = new Gson();

    /** stand_model.json 声明的模型信息：key = model_id + "_" + state。 */
    private static final Map<String, StandModelInfo> INFOS = new HashMap<>();

    /** 已构建的运行时模型：key 与 INFOS 相同（model_id + "_" + state）。 */
    private static final Map<String, RuntimeStandModel> MODELS = new HashMap<>();

    /** 构建失败的条目 key（几何文件缺失/解析失败）。 */
    private static final Set<String> FAILED = new HashSet<>();

    private CustomModelLoader() {
    }

    /** 资源重载入口：清空缓存、重新登记全部声明，并立即构建几何。 */
    public static void reload(ResourceManager manager) {
        INFOS.clear();
        MODELS.clear();
        FAILED.clear();
        CustomAnimationLoader.clear();

        int declared = 0;
        int packs = 0;
        for (String namespace : manager.getAllNamespaces()) {
            Identifier packId = Identifier.of(namespace, "stand_model.json");
            Optional<Resource> resource = manager.getResource(packId);
            if (resource.isEmpty()) {
                continue;
            }
            packs++;
            try (InputStreamReader reader = new InputStreamReader(resource.get().getInputStream(),
                    StandardCharsets.UTF_8)) {
                CustomModelPack pack = GSON.fromJson(reader, CustomModelPack.class);
                if (pack == null || pack.modelList == null) {
                    continue;
                }
                for (StandModelInfo info : pack.modelList) {
                    if (info == null || info.modelId == null || info.modelId.isEmpty()) {
                        continue;
                    }
                    INFOS.putIfAbsent(info.key(), info);
                    declared++;
                }
            } catch (Exception e) {
                LOGGER.error("[HuajiAge] Failed to read stand model pack: {}", packId, e);
            }
        }

        for (StandModelInfo info : INFOS.values()) {
            if (MODELS.containsKey(info.key()) || FAILED.contains(info.key())) {
                continue;
            }
            RuntimeStandModel built = build(info, manager);
            if (built == null) {
                FAILED.add(info.key());
            } else {
                built.attach(info, CustomAnimationLoader.load(info, manager));
                MODELS.put(info.key(), built);
                LOGGER.info("[HuajiAge] Custom stand model built: {} (bones={}, boxes={}, animations={})",
                        info.key(), built.bones().size(), built.boxCount(), built.animationCount());
            }
        }
        LOGGER.info("[HuajiAge] Custom stand models declared: {} entries in {} packs, built {} models",
                declared, packs, MODELS.size());
    }

    /**
     * 按状态声明的 modelId 取运行时模型：该 modelId 必须在 stand_model.json 里声明过，
     * 且几何构建成功；否则返回 null（调用方回落既有模型）。
     */
    public static RuntimeStandModel find(String stateModelId) {
        StandModelInfo info = info(stateModelId);
        return info == null ? null : MODELS.get(info.key());
    }

    /** 该状态声明的模型信息（位移/朝向/不浮动等参数用），未声明返回 null。 */
    public static StandModelInfo info(String stateModelId) {
        return stateModelId == null ? null : INFOS.get(stateModelId);
    }

    private static RuntimeStandModel build(StandModelInfo info, ResourceManager manager) {
        Identifier geometryId = geometryId(info);
        if (geometryId == null) {
            return null;
        }
        Optional<Resource> resource = manager.getResource(geometryId);
        if (resource.isEmpty()) {
            LOGGER.warn("[HuajiAge] Custom stand model geometry not found: {} (model_id={}, state={})",
                    geometryId, info.modelId, info.state);
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(resource.get().getInputStream(),
                StandardCharsets.UTF_8)) {
            GeometryModel geometry = GSON.fromJson(reader, GeometryModel.class);
            RuntimeStandModel model = RuntimeStandModel.build(info.modelId, geometry);
            if (model == null) {
                LOGGER.warn("[HuajiAge] Custom stand model has no bones: {}", geometryId);
            }
            return model;
        } catch (Exception e) {
            LOGGER.error("[HuajiAge] Failed to parse custom stand geometry: {}", geometryId, e);
            return null;
        }
    }

    /**
     * 几何文件约定路径：与 model_id 同命名空间的 models/entity/&lt;path&gt;.json；
     * 非 default 状态取 &lt;path&gt;_&lt;state&gt;.json（例如 white_snake_punch.json）。
     */
    private static Identifier geometryId(StandModelInfo info) {
        Identifier id = Identifier.tryParse(info.modelId);
        if (id == null) {
            LOGGER.warn("[HuajiAge] Illegal model_id in stand_model.json: {}", info.modelId);
            return null;
        }
        String state = info.state == null || info.state.isBlank() ? "default" : info.state;
        String file = "default".equals(state) ? id.getPath() + ".json" : id.getPath() + "_" + state + ".json";
        return Identifier.of(id.getNamespace(), "models/entity/" + file);
    }
}
