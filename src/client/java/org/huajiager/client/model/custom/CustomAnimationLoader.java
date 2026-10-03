package org.huajiager.client.model.custom;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * 骨骼动画脚本加载器：按模型条目声明的 animation 列表（未声明时用默认列表）
 * 读取并 eval {@code assets/&lt;ns&gt;/animation/*.js}，缓存脚本对象供每帧调用。
 *
 * <p>脚本契约为 {@code Java.asJSONCompatible({ animation: function(player, limbSwing,
 * limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, modelMap) {...} })}，
 * 与工程内 animation 目录下的脚本一致。</p>
 */
public final class CustomAnimationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAnimationLoader.class);

    /** 未声明 animation 时使用的默认脚本（骨骼名为 head / blink / flash_frame_* / rotationYaw / arm* / hand* / wheel*）。 */
    private static final List<String> DEFAULT_SCRIPTS = List.of(
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

    private CustomAnimationLoader() {
    }

    public static void clear() {
        CACHE.clear();
    }

    /** 取该模型条目要跑的动画脚本；声明为空时用默认列表。 */
    public static List<Object> load(StandModelInfo info, ResourceManager manager) {
        List<String> declared = info == null ? null : info.animation;
        List<String> scripts = declared == null || declared.isEmpty() ? DEFAULT_SCRIPTS : declared;
        List<Object> result = new ArrayList<>();
        for (String raw : scripts) {
            Identifier id = Identifier.tryParse(raw);
            if (id == null) {
                LOGGER.warn("[HuajiAge] Illegal animation script path: {}", raw);
                continue;
            }
            Object script = get(id, manager);
            if (script != null) {
                result.add(script);
            }
        }
        return result;
    }

    private static Object get(Identifier id, ResourceManager manager) {
        if (CACHE.containsKey(id)) {
            return CACHE.get(id);
        }
        Optional<Resource> resource = manager.getResource(id);
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
            return script;
        } catch (Exception e) {
            LOGGER.error("[HuajiAge] Failed to eval animation script: {}", id, e);
            CACHE.put(id, null);
            return null;
        }
    }
}
