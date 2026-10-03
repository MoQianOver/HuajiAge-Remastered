package org.huajiager.client.model.custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;

import org.huajiager.client.render.model.HAModelBase;
import org.huajiager.client.render.model.StandAnimatedModel;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.capability.IExposedData;
import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * 借用原版实体模型作为替身造型的运行时模型，并驱动骨骼动画脚本。
 *
 * <p>直接把烘焙好的原版 {@link ModelPart} 树拿去渲染：原版模型与替身模型同为像素单位，
 * ModelPart 内部会自行 /16 转格，因此尺寸与站位和内置替身一致，不需要额外缩放。
 * 骨骼按名字（原版模型的部件名，如 head / left_arm / right_tendril）暴露给动画脚本。</p>
 */
public class VanillaStandModel extends HAModelBase implements StandAnimatedModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaStandModel.class);

    private final ModelPart root;
    private final Map<String, StandBone> boneWrappers = new LinkedHashMap<>();

    /** 已 eval 的骨骼动画脚本。 */
    private final List<Object> animations = new ArrayList<>();

    /** 已经报过错的脚本，避免每帧刷屏。 */
    private final Set<Object> failedAnimations = new HashSet<>();

    /** 当前生效的脚本路径列表（状态切换时重新取脚本）。 */
    private List<String> currentScripts = List.of();

    /** 第一人称骨骼名（原版模型没有这两根骨骼时第一人称不渲染本体）。 */
    private static final String FIRST_PERSON_BONE = "firstOnly";

    public VanillaStandModel(ModelPart root) {
        super(64, 64);
        this.root = root;
        collectBones(root);
    }

    public ModelPart root() {
        return root;
    }

    /** 递归登记部件名，供动画脚本按名取用。 */
    private void collectBones(ModelPart part) {
        for (Map.Entry<String, ModelPart> entry : childrenOf(part).entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            boneWrappers.putIfAbsent(entry.getKey(), new StandBone(entry.getValue()));
            collectBones(entry.getValue());
        }
    }

    /** ModelPart.children 是私有的，经访问器 mixin 读取。 */
    private static Map<String, ModelPart> childrenOf(ModelPart part) {
        return ((org.huajiager.mixin.client.ModelPartAccessor) (Object) part).huajiager$getChildren();
    }

    public void attach(List<Object> scripts) {
        this.animations.clear();
        if (scripts != null) {
            this.animations.addAll(scripts);
        }
    }

    public int animationCount() {
        return animations.size();
    }

    public int boneCount() {
        return boneWrappers.size();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
            float b, float a) {
        root.render(matrices, vertices, light, overlay, r, g, b, a);
    }

    @Override
    public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            EntityStandBase entity, float ageTicks, float speed, float power) {
        refreshAnimations(entity);
        applyAnimations(entity, ageTicks);
        matrices.push();
        // 与原版口径一致：未显式关闭浮动时叠加上下浮动
        matrices.translate(0.0, MathHelper.sin(ageTicks / 20f) * 0.1f, 0.0);
        if (isLocalFirstPerson(entity)) {
            ModelPart firstOnly = childrenOf(root).get(FIRST_PERSON_BONE);
            if (firstOnly != null) {
                firstOnly.render(matrices, vertices, light, overlay);
            }
        } else {
            root.render(matrices, vertices, light, overlay);
        }
        matrices.pop();
    }

    /** 状态切换时按状态声明的动画列表（没有声明则用默认脚本）重新取脚本。 */
    private void refreshAnimations(EntityStandBase entity) {
        List<String> declared = CustomAnimationLoader.stateAnimations(entity);
        List<String> scripts = declared.isEmpty() ? CustomAnimationLoader.DEFAULT_SCRIPTS : declared;
        if (!scripts.equals(currentScripts)) {
            currentScripts = scripts;
            attach(CustomAnimationLoader.load(scripts));
        }
    }

    /** 逐帧调用动画脚本；单个脚本首次出错只记录一次。 */
    private void applyAnimations(EntityStandBase entity, float ageTicks) {
        if (animations.isEmpty() || !(JsEngineHelper.ENGINE instanceof Invocable invocable)) {
            return;
        }
        LivingEntity user = entity.getUser();
        if (user == null) {
            return;
        }
        StandAnimationEntity wrapper = new StandAnimationEntity(user, null, user.getHandSwingProgress(0f));
        for (Object script : animations) {
            if (script == null || failedAnimations.contains(script)) {
                continue;
            }
            try {
                invocable.invokeMethod(script, "animation", wrapper, entity.limbAnimator.getPos(0f),
                        entity.limbAnimator.getSpeed(0f), ageTicks, user.getHeadYaw(), user.getPitch(), 1f,
                        boneWrappers);
            } catch (Exception e) {
                failedAnimations.add(script);
                LOGGER.error("[HuajiAge] Animation script failed for vanilla model: {}", e.toString());
            }
        }
    }

    private static boolean isLocalFirstPerson(EntityStandBase entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.player instanceof AbstractClientPlayerEntity) || mc.options == null
                || !mc.options.getPerspective().isFirstPerson()) {
            return false;
        }
        return entity.getUser() == mc.player;
    }
}
