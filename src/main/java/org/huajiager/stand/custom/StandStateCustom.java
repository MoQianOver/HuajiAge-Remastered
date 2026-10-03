package org.huajiager.stand.custom;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.huajiager.stand.custom.script.EntityLivingBaseWrapper;
import org.huajiager.stand.custom.script.StandDadaWrapper;
import org.huajiager.stand.custom.script.WorldWrapper;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * 自定义替身状态。
 * <p>将 custom_stand/states/*.js 中导出的状态对象桥接到本地 {@link StandStateBase}：
 * - 每 tick 由 {@link #doTask(LivingEntity)} 调 JS {@code update(world, entityWrapper, dataWrapper)}。 * - 超时由 {@link #doTaskOutOfTime(LivingEntity)} 调 JS {@code timeOut(...)}。 * - Hold（持续按键）由 {@link #doTaskCapability(LivingEntity)} 调 JS {@code capability(...)}。
 * 通过 Invocable 直接 invoke JS 对象方法。若脚本对象不可调用（异常环境），自动退化为空实现。</p>
 */
public class StandStateCustom extends StandStateBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandStateCustom.class);

    private StandStateInfo stateInfo;

    public StandStateCustom() {
    }

    public StandStateCustom(StandStateInfo stateInfo) {
        this.stateInfo = stateInfo;
        if (stateInfo == null) {
            return;
        }
        this.stand = stateInfo.getStand() == null ? this.stand : stateInfo.getStand();
        this.stateName = stateInfo.getStateId();
        this.extraDatas = stateInfo.getStateTags();
        this.isHandPlay = stateInfo.isHand();
        this.stage = stateInfo.getStage();
        this.soundLoop = stateInfo.isSoundRepeat();
        this.animations = stateInfo.getAnimations();
    }

    public StandStateInfo getStateInfo() {
        return stateInfo;
    }

    @Override
    public String getModelID() {
        if (stateInfo == null || stateInfo.getModelId() == null) {
            return super.getModelID();
        }
        return stateInfo.getModelId();
    }

    /**
     * 模型 ID 到实体纹理的映射。modelId 形如 "huajiager:crazy_diamond_default"，
     * 映射到 textures/entity/<path>.png；默认态 "_default" 后缀的贴图通常以不带后缀
     * 的文件存在（crazy_diamond.png），故剔除后缀再拼。纹理文件不存在时由资源管理器
     * 在渲染期告警，不在此崩溃。
     */
    @Override
    public Identifier getTex() {
        if (stateInfo == null || stateInfo.getModelId() == null) {
            return super.getTex();
        }
        Identifier res = Identifier.tryParse(stateInfo.getModelId());
        if (res == null) {
            return super.getTex();
        }
        String path = res.getPath();
        if (path.endsWith("_default")) {
            path = path.substring(0, path.length() - "_default".length());
        }
        return new Identifier(res.getNamespace(), "textures/entity/" + path + ".png");
    }

    @Override
    public void doTask(LivingEntity user) {
        invokeScript("update", user);
    }

    @Override
    public void doTaskOutOfTime(LivingEntity user) {
        invokeScript("timeOut", user);
    }

    public boolean doTaskCapability(LivingEntity user) {
        Object result = invokeScript("capability", user);
        if (result instanceof Boolean b) {
            return b;
        }
        return true;
    }

    private Object invokeScript(String methodName, LivingEntity user) {
        Object scriptObject = stateInfo == null ? null : stateInfo.getStateObject();
        if (scriptObject == null) {
            return null;
        }
        // 脚本调用统一走引擎实例的 Invocable：Nashorn 时代 eval 返回的 ScriptObjectMirror
        // 自身实现 Invocable 可直接强转调用；GraalJS 桥接下脚本对象（ScriptObjectMirror）
        // 仅实现 Map、不再实现 Invocable，若仍按 scriptObject instanceof Invocable 判断
        // 会每 tick 打"not invocable"WARN 且脚本完全不执行。改为判断引擎本身并提供
        // invokeMethod 调用路径，两种引擎均兼容。
        if (!(JsEngineHelper.ENGINE instanceof Invocable)) {
            LOGGER.warn("[HuajiAge] Stand state script engine is not invocable, method {} skipped.",
                    stateInfo.getStateId(), methodName);
            return null;
        }
        try {
            Invocable invocable = (Invocable) JsEngineHelper.ENGINE;
            WorldWrapper worldWrapper = new WorldWrapper(user.getWorld());
            EntityLivingBaseWrapper entityWrapper = new EntityLivingBaseWrapper(user);
            StandDadaWrapper dataWrapper = new StandDadaWrapper(user);
            return invocable.invokeMethod(scriptObject, methodName, worldWrapper, entityWrapper, dataWrapper);
        } catch (NoSuchMethodException e) {
            // JS 未定义该方法（如某状态无 capability），静默忽略
        } catch (ScriptException e) {
            LOGGER.error("[HuajiAge] Stand state script error in [{}] method [{}]: {}", stateInfo.getStateId(),
                    methodName, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("[HuajiAge] Unexpected error invoking method [{}] on state [{}]", methodName,
                    stateInfo == null ? "?" : stateInfo.getStateId(), e);
        }
        return null;
    }
}
