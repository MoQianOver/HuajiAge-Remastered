package org.huajiager.client.model.custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;

import org.huajiager.client.render.model.HAModelBase;
import org.huajiager.client.render.model.HAModelPart;
import org.huajiager.client.render.model.StandAnimatedModel;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.util.JsEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

/**
 * 由基岩几何文件在运行时构建的替身模型，并驱动骨骼动画脚本。
 *
 * <p>坐标换算与工程内既有模型同一口径：骨骼 pivot 转为相对父节点（基岩 Y 向上、模型空间
 * Y 向下，故取父 Y 减子 Y；根骨骼把基岩原点平移到模型空间基准 24 像素），方块取相对
 * 该骨骼 pivot 的局部坐标（Y 方向翻转取 pivotY - originY - sizeY），旋转角由度转弧度。</p>
 *
 * <p>渲染时按模型条目声明的 animation 脚本逐帧驱动骨骼，然后按 transfer/rotation 做
 * 模型自身定位与朝向修正，并在未关闭 no_float 时叠加原版的上下浮动；第一人称下只渲染
 * viewFirst / firstOnly 两根骨骼（与原版 renderFirst 同口径）。</p>
 */
public class RuntimeStandModel extends HAModelBase implements StandAnimatedModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeStandModel.class);

    /** 基岩模型空间的 Y 基准：根骨骼 pivot 的 Y 会换算成 24 - pivotY。 */
    private static final float Y_BASIS = 24f;

    private final String modelId;
    private final HAModelPart root;
    private final Map<String, HAModelPart> bones = new LinkedHashMap<>();
    private final Map<String, StandBone> boneWrappers = new LinkedHashMap<>();
    private int boxCount;

    /** 模型条目信息（动画列表、位移、朝向、不浮动开关）。 */
    private StandModelInfo info;

    /** 已 eval 的骨骼动画脚本。 */
    private final List<Object> animations = new ArrayList<>();

    /** 已经报过错的脚本，避免每帧刷屏。 */
    private final Set<Object> failedAnimations = new HashSet<>();

    private RuntimeStandModel(String modelId, int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
        this.modelId = modelId;
        this.root = new HAModelPart(this);
    }

    public String modelId() {
        return modelId;
    }

    /** 方块总数（日志与自检用）。 */
    public int boxCount() {
        return boxCount;
    }

    /** 按骨骼名取节点（供动画脚本按名驱动）。 */
    public HAModelPart bone(String name) {
        return bones.get(name);
    }

    public Map<String, HAModelPart> bones() {
        return bones;
    }

    /** 挂上模型条目信息与动画脚本（加载期调用）。 */
    public void attach(StandModelInfo info, List<Object> animations) {
        this.info = info;
        this.animations.clear();
        if (animations != null) {
            this.animations.addAll(animations);
        }
    }

    public int animationCount() {
        return animations.size();
    }

    /**
     * 由几何文件构建模型树；几何为空或没有骨骼时返回 null（调用方回落既有模型）。
     */
    public static RuntimeStandModel build(String modelId, GeometryModel geometry) {
        GeometryModel.Model model = geometry == null ? null : geometry.activeModel();
        if (model == null || model.bones == null || model.bones.isEmpty()) {
            return null;
        }
        RuntimeStandModel result = new RuntimeStandModel(modelId, model.textureWidth(), model.textureHeight());

        Map<String, float[]> pivots = new LinkedHashMap<>();
        for (GeometryModel.BonesItem bone : model.bones) {
            if (bone.name == null || bone.name.isEmpty()) {
                continue;
            }
            float[] pivot = xyz(bone.pivot);
            pivots.put(bone.name, pivot);
            HAModelPart part = new HAModelPart(result);
            result.bones.put(bone.name, part);
            result.boneWrappers.put(bone.name, new StandBone(part));
        }

        for (GeometryModel.BonesItem bone : model.bones) {
            if (bone.name == null || !result.bones.containsKey(bone.name)) {
                continue;
            }
            HAModelPart part = result.bones.get(bone.name);
            float[] pivot = pivots.get(bone.name);
            float[] parentPivot = bone.parent == null ? null : pivots.get(bone.parent);
            if (parentPivot != null) {
                part.setRotationPoint(pivot[0] - parentPivot[0], parentPivot[1] - pivot[1],
                        pivot[2] - parentPivot[2]);
            } else {
                part.setRotationPoint(pivot[0], Y_BASIS - pivot[1], pivot[2]);
            }
            if (bone.rotation != null) {
                float[] rotation = xyz(bone.rotation);
                part.rotateAngleX = (float) Math.toRadians(rotation[0]);
                part.rotateAngleY = (float) Math.toRadians(rotation[1]);
                part.rotateAngleZ = (float) Math.toRadians(rotation[2]);
            }
            if (Boolean.TRUE.equals(bone.mirror)) {
                part.mirror = true;
            }
            result.boxCount += addCubes(part, pivot, bone);
            if (parentPivot != null) {
                HAModelPart parent = result.bones.get(bone.parent);
                if (parent != null) {
                    parent.addChild(part);
                    continue;
                }
            }
            result.root.addChild(part);
        }
        return result;
    }

    private static int addCubes(HAModelPart part, float[] pivot, GeometryModel.BonesItem bone) {
        if (bone.cubes == null) {
            return 0;
        }
        int added = 0;
        for (GeometryModel.CubesItem cube : bone.cubes) {
            if (cube.origin == null || cube.size == null) {
                continue;
            }
            float[] origin = xyz(cube.origin);
            float[] size = xyz(cube.size);
            float inflate = cube.inflate == null ? 0f : cube.inflate;
            int u = 0;
            int v = 0;
            if (cube.uv != null && cube.uv.size() >= 2) {
                u = Math.round(cube.uv.get(0));
                v = Math.round(cube.uv.get(1));
            }
            part.addBox(u, v, origin[0] - pivot[0], pivot[1] - origin[1] - size[1], origin[2] - pivot[2],
                    size[0], size[1], size[2], inflate, Boolean.TRUE.equals(cube.mirror));
            added++;
        }
        return added;
    }

    private static float[] xyz(List<Float> list) {
        float[] out = new float[3];
        if (list != null) {
            for (int i = 0; i < 3 && i < list.size(); i++) {
                Float value = list.get(i);
                out[i] = value == null ? 0f : value;
            }
        }
        return out;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
            float b, float a) {
        root.render(matrices, vertices, light, overlay, r, g, b, a);
    }

    @Override
    public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            EntityStandBase entity, float ageTicks, float speed, float power) {
        applyAnimations(entity, ageTicks);
        matrices.push();
        applyNudge(matrices, ageTicks);
        if (isLocalFirstPerson(entity)) {
            renderFirstPersonBones(matrices, vertices, light, overlay);
        } else {
            root.render(matrices, vertices, light, overlay);
        }
        matrices.pop();
    }

    /** 逐帧调用动画脚本驱动骨骼角度；单个脚本首次出错时记录一次，不再重复刷屏。 */
    private void applyAnimations(EntityStandBase entity, float ageTicks) {
        if (animations.isEmpty() || !(JsEngineHelper.ENGINE instanceof Invocable invocable)) {
            return;
        }
        LivingEntity user = entity.getUser();
        if (user == null) {
            return;
        }
        StandAnimationEntity wrapper = new StandAnimationEntity(user, info, user.getHandSwingProgress(0f));
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
                LOGGER.error("[HuajiAge] Animation script failed for model {}: {}", modelId, e.toString());
            }
        }
    }

    /** 模型自身的定位：先按 rotation 旋转，再按 transfer 平移，最后叠加原版的上下浮动。 */
    private void applyNudge(MatrixStack matrices, float ageTicks) {
        if (info != null && info.rotation != null && info.rotation.size() >= 4) {
            float degree = info.rotation.get(0);
            float axisX = info.rotation.get(1);
            float axisY = info.rotation.get(2);
            float axisZ = info.rotation.get(3);
            if (axisX != 0f || axisY != 0f || axisZ != 0f) {
                matrices.multiply(new Quaternionf().rotationAxis((float) Math.toRadians(degree), axisX, axisY,
                        axisZ));
            }
        }
        if (info != null && info.transfer != null && info.transfer.size() >= 3) {
            matrices.translate(info.transfer.get(0), info.transfer.get(1), info.transfer.get(2));
        }
        if (info == null || !Boolean.TRUE.equals(info.noFloat)) {
            matrices.translate(0.0, MathHelper.sin(ageTicks / 20f) * 0.1f, 0.0);
        }
    }

    /** 本机玩家第一人称：本机视角只看得见自己替身的 fist 骨骼（viewFirst / firstOnly）。 */
    private static boolean isLocalFirstPerson(EntityStandBase entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null || !mc.options.getPerspective().isFirstPerson()) {
            return false;
        }
        return entity.getUser() == mc.player;
    }

    private void renderFirstPersonBones(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        HAModelPart viewFirst = bones.get("viewFirst");
        HAModelPart firstOnly = bones.get("firstOnly");
        if (viewFirst != null) {
            viewFirst.render(matrices, vertices, light, overlay);
        }
        if (firstOnly != null) {
            firstOnly.render(matrices, vertices, light, overlay);
        }
    }
}
