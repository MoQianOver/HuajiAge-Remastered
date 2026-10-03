package org.huajiager.stand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.stand.custom.StandCustomInfo;
import org.huajiager.stand.custom.StandStateInfo;
import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * 自定义替身资源加载器。
 * <p>只从 mod 内置资源加载自定义替身（不扫描用户 config 目录）：
 * - standalone JSON（{@link #loadCustomStand()} → {@link StandCustomInfo}）注册进
 *   {@link #CUSTOM_STAND_SERVER}。 * - JS 状态脚本（custom_stand/states/*.js）经 {@link JsEngineHelper#ENGINE} eval 后
 *   {@code transObjectToEntry} 转成 {@link StandStateInfo} 注册进 {@link #CUSTOM_STATE_SERVER}，
 *   并保存脚本对象供状态调用。
 * {@link StandLoader#reloadStands()} 遍历 CUSTOM_STAND_SERVER 实例化 StandCustom 完成注册。</p>
 */
public class StandResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandResourceLoader.class);
    private static final Gson GSON = new Gson();

    /** 自定义替身注册表（key = 替身注册名） */
    public static final Map<String, StandCustomInfo> CUSTOM_STAND_SERVER = new HashMap<>();
    /** 自定义状态注册表（key = 替身注册名 + "_" + stateId） */
    public static final Map<String, StandStateInfo> CUSTOM_STATE_SERVER = new HashMap<>();

    private static final String RES_FOLDER = "/assets/" + HuajiAgeRemastered.MOD_ID + "/custom_stand";
    private static final String RES_STATE_FOLDER = "/assets/" + HuajiAgeRemastered.MOD_ID + "/custom_stand/states";
    private static final String ACCEPTED_STAND_SUFFIX = ".json";
    private static final String ACCEPTED_STATE_SUFFIX = ".js";

    /** 重载入口：清理并加载全部内置资源。 */
    public static void loadCustomStand() {
        CUSTOM_STAND_SERVER.clear();
        CUSTOM_STATE_SERVER.clear();
        loadInternalStands();
        loadInternalStates();
        // 加载结果打印一次，便于确认内置资源是否完整
        LOGGER.info("[HuajiAge] Built-in custom stands loaded: {} | states: {}",
                CUSTOM_STAND_SERVER.keySet(), CUSTOM_STATE_SERVER.keySet());
    }

    /** 内部资源加载（硬编码三替身）。 */
    public static void loadInternalStands() {
        loadInternalStand("crazy_diamond");
        loadInternalStand("hermit_purple");
        loadInternalStand("white_snake");
    }

    /** 内部状态资源加载（七状态脚本）。 */
    public static void loadInternalStates() {
        loadInternalState("crazy_diamond_default");
        loadInternalState("crazy_diamond_heal");
        loadInternalState("crazy_diamond_idle");
        loadInternalState("hermit_purple_default");
        loadInternalState("hermit_purple_overdrive");
        loadInternalState("white_snake_default");
        loadInternalState("white_snake_punch");
    }

    private static void loadInternalStand(String json) {
        InputStream stream = StandResourceLoader.class.getResourceAsStream(RES_FOLDER + "/" + json + ACCEPTED_STAND_SUFFIX);
        try {
            if (stream == null) {
                LOGGER.error("[HuajiAge] Missing internal stand resource: {}", json);
                return;
            }
            StandCustomInfo info = loadStand(stream);
            CUSTOM_STAND_SERVER.put(info.getStand(), info);
        } catch (NullPointerException | JsonSyntaxException e) {
            LOGGER.error("[HuajiAge] Failed to load internal stand: {}", json, e);
        }
    }

    private static void loadInternalState(String js) {
        InputStream stream = StandResourceLoader.class.getResourceAsStream(RES_STATE_FOLDER + "/" + js + ACCEPTED_STATE_SUFFIX);
        try {
            if (stream == null) {
                LOGGER.error("[HuajiAge] Missing internal state resource: {}", js);
                return;
            }
            StandStateInfo info = loadStates(stream);
            if (info != null) {
                CUSTOM_STATE_SERVER.put(info.getStand() + "_" + info.getStateId(), info);
            }
        } catch (NullPointerException e) {
            LOGGER.error("[HuajiAge] Failed to load internal state: {}", js, e);
        }
    }

    private static StandCustomInfo loadStand(InputStream input) {
        StandCustomInfo info = GSON.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8),
                new TypeToken<StandCustomInfo>() {
                }.getType());
        return info.decorate();
    }

    private static StandStateInfo loadStates(InputStream input) {
        try {
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Object scriptObject = JsEngineHelper.ENGINE.eval(script);
            return transObjectToEntry(scriptObject);
        } catch (IOException | ScriptException e) {
            LOGGER.error("[HuajiAge] Failed to eval custom state script", e);
        }
        return null;
    }

    /**
     * 将 JS 状态对象转成 {@link StandStateInfo}。
     * <p>兼容 Nashorn / GraalJS 两种引擎：eval 返回对象经 Java.asJSONCompatible 后
     * 为 Map 视图（GraalJS 的 ScriptObjectMirror 亦实现 Map）。同时保存原始脚本对象到
     * {@code stateObject}，供 StandStateCustom invokeMethod 调用 update/timeOut/capability。</p>
     */
    private static StandStateInfo transObjectToEntry(Object scriptObject) {
        if (!(scriptObject instanceof Map)) {
            LOGGER.error("[HuajiAge] Custom state script eval result is not a Map: {}",
                    scriptObject == null ? "null" : scriptObject.getClass());
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> scriptMaps = new HashMap<>((Map<String, Object>) scriptObject);

        Object standObj = scriptMaps.get(stateArgs.STAND.getName());
        if (standObj == null || String.valueOf(standObj).isBlank()) {
            // 缺 stand 字段的脚本定位不到替身，直接判为无效；不注册成 "null_default" 这类脏 key
            LOGGER.error("[HuajiAge] Custom state script is missing the 'stand' field, skipped");
            return null;
        }
        String standId = String.valueOf(standObj);

        Object stateIdObj = scriptMaps.get(stateArgs.STATE_ID.getName());
        String stateId = stateIdObj == null ? "default" : String.valueOf(stateIdObj);

        Object nameObj = scriptMaps.get(stateArgs.NAME.getName());
        String name = nameObj == null ? null : String.valueOf(nameObj);

        Object stageObj = scriptMaps.get(stateArgs.STAGE.getName());
        int stage = 0;
        if (stageObj instanceof Number) {
            stage = ((Number) stageObj).intValue();
        } else if (stageObj != null) {
            try {
                stage = Integer.parseInt(String.valueOf(stageObj).trim());
            } catch (NumberFormatException e) {
                stage = 0;
            }
        }

        String modelId = null;
        Object modelObj = scriptMaps.get(stateArgs.MODEL.getName());
        if (modelObj != null) {
            modelId = String.valueOf(modelObj);
            if (!stateId.contains("%custom")) {
                modelId += "_" + stateId;
            }
        }

        List<String> stateTags = new ArrayList<>();
        Object tagsObj = scriptMaps.get(stateArgs.TAGS.getName());
        if (tagsObj instanceof List) {
            for (Object o : (List<?>) tagsObj) {
                stateTags.add(String.valueOf(o));
            }
        }

        Object soundRepeatObj = scriptMaps.get(stateArgs.SOUND_REPEAT.getName());
        boolean soundRepeat = false;
        if (soundRepeatObj instanceof Boolean) {
            soundRepeat = (Boolean) soundRepeatObj;
        } else if (soundRepeatObj != null) {
            soundRepeat = Boolean.parseBoolean(String.valueOf(soundRepeatObj));
        }

        Object handObj = scriptMaps.get(stateArgs.HAND.getName());
        boolean hand = true;
        if (handObj instanceof Boolean) {
            hand = (Boolean) handObj;
        } else if (handObj != null) {
            hand = Boolean.parseBoolean(String.valueOf(handObj));
        }

        // 状态声明的骨骼动画脚本列表（空表表示用模型条目声明，再退回默认脚本）
        List<String> animations = new ArrayList<>();
        Object animationObj = scriptMaps.get(stateArgs.ANIMATION.getName());
        if (animationObj instanceof List) {
            for (Object o : (List<?>) animationObj) {
                animations.add(String.valueOf(o));
            }
        }

        StandStateInfo info = new StandStateInfo();
        info.setStand(standId);
        info.setStateId(stateId);
        info.setName(name);
        info.setStage(stage);
        info.setModelId(modelId);
        info.setStateTags(stateTags);
        info.setSoundRepeat(soundRepeat);
        info.setHand(hand);
        info.setAnimations(animations);
        info.setStateObject(scriptObject);
        return info;
    }

    private enum stateArgs {
        STAND("stand"),
        NAME("stateKey"),
        STATE_ID("stateId"),
        STAGE("stage"),
        MODEL("modelId"),
        TAGS("stateTags"),
        SOUND_REPEAT("soundRepeat"),
        ANIMATION("animation"),
        HAND("hand");

        private final String name;

        stateArgs(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
