package org.huajiager.client.model.custom;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.custom.StandStateInfo;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * 骨骼动画脚本加载器：按状态声明 / 模型条目声明的 animation 列表（都为空时用默认列表）
 * 读取并 eval {@code assets/&lt;ns&gt;/animation/*.js}，缓存脚本对象供每帧调用。
 *
 * <p>脚本契约为 {@code Java.asJSONCompatible({ animation: function(player, limbSwing,
 * limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, modelMap) {...} })}，
 * 与工程内 animation 目录下的脚本一致。</p>
 */
public final class CustomAnimationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAnimationLoader.class);

    /** 未声明 animation 时使用的默认脚本（骨骼名为 head / blink / flash_frame_* / rotationYaw / arm* / hand* / wheel*）。 */
    public static final List<String> DEFAULT_SCRIPTS = List.of(
            "huajiager:animation/head.js",
            "huajiager:animation/flash_frames.js",
            "huajiager:animation/flash_frames_fast.js",
            "huajiager:animation/rotation_yaw.js",
            "huajiager:animation/ro_arms.js",
            "huajiager:animation/ro_hands.js",
            "huajiager:animation/ro_hit_hands.js",
            "huajiager:animation/wheel.js");

    /** 脚本对象缓存（key = 脚本路径），资源重载时清空。 */
    private static final Map<Identifier, Object> CACHE = new HashMap<>();

    private static ResourceManager resourceManager;

    private CustomAnimationLoader() {
    }

    /** 记录资源管理器（客户端资源重载时调用），供按需读取脚本。 */
    public static void bind(ResourceManager manager) {
        resourceManager = manager;
    }

    public static void clear() {
        CACHE.clear();
    }

    /** 按模型条目取脚本；条目未声明时用默认列表。 */
    public static List<Object> load(StandModelInfo info, ResourceManager manager) {
        List<String> declared = info == null ? null : info.animation;
        List<String> scripts = declared == null || declared.isEmpty() ? DEFAULT_SCRIPTS : declared;
        return load(scripts);
    }

    /** 按给定的脚本路径列表 eval；列表为空时用默认列表。 */
    public static List<Object> load(List<String> scripts) {
        List<String> list = scripts == null || scripts.isEmpty() ? DEFAULT_SCRIPTS : scripts;
        List<Object> result = new ArrayList<>();
        for (String raw : list) {
            Identifier id = Identifier.tryParse(raw);
            if (id == null) {
                LOGGER.warn("[HuajiAge] Illegal animation script path: {}", raw);
                continue;
            }
            Object script = get(id);
            if (script != null) {
                result.add(script);
            }
        }
        return result;
    }

    /**
     * 预热并校验所有自定义替身状态声明的动画脚本：在资源重载期一次性 eval，
     * 语法错或路径错会立刻在日志里暴露，而不是等到渲染那一刻。
     */
    public static void preloadStateScripts() {
        for (StandStateInfo info : org.huajiager.stand.StandResourceLoader.CUSTOM_STATE_SERVER.values()) {
            List<String> animations = info.getAnimations();
            if (animations != null && !animations.isEmpty()) {
                load(animations);
            }
        }
    }

    /** 当前状态声明的动画脚本列表；未声明或列表为空时返回空表（调用方据此退回默认脚本）。 */
    public static List<String> stateAnimations(EntityStandBase entity) {
        LivingEntity user = entity.getUser();
        if (user == null) {
            return List.of();
        }
        IExposedData data = StandUtil.getStandData(user);
        if (data == null) {
            return List.of();
        }
        if (StandStates.getStandState(data.getStand(), data.getState()) instanceof StandStateBase state) {
            List<String> animations = state.getAnimations();
            return animations == null || animations.isEmpty() ? List.of() : List.copyOf(animations);
        }
        return List.of();
    }

    private static Object get(Identifier id) {
        if (CACHE.containsKey(id)) {
            return CACHE.get(id);
        }
        if (resourceManager == null) {
            return null;
        }
        Optional<Resource> resource = resourceManager.getResource(id);
        if (resource.isEmpty()) {
            LOGGER.warn("[HuajiAge] Animation script not found: {}", id);
            CACHE.put(id, null);
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(resource.get().getInputStream(),
                StandardCharsets.UTF_8)) {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) > 0) {
                text.append(buffer, 0, read);
            }
            Object script = JsEngineHelper.ENGINE.eval(text.toString());
            CACHE.put(id, script);
            LOGGER.info("[HuajiAge] Animation script loaded: {}", id);
            return script;
        } catch (Exception e) {
            LOGGER.error("[HuajiAge] Failed to eval animation script: {}", id, e);
            CACHE.put(id, null);
            return null;
        }
    }
}
