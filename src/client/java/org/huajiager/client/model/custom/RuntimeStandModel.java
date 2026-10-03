package org.huajiager.client.model.custom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.huajiager.client.render.model.HAModelBase;
import org.huajiager.client.render.model.HAModelPart;
import org.huajiager.client.render.model.StandAnimatedModel;
import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 由基岩几何文件在运行时构建的替身模型。
 *
 * <p>坐标换算与工程内既有模型同一口径：骨骼 pivot 转为相对父节点（基岩 Y 向上、模型空间
 * Y 向下，故取父 Y 减子 Y；根骨骼把基岩原点平移到模型空间基准 24 像素），方块取相对
 * 该骨骼 pivot 的局部坐标（Y 方向翻转取 pivotY - originY - sizeY），旋转角由度转弧度。</p>
 *
 * <p>骨骼动画尚未接入：{@link #renderStand} 目前按静态姿势渲染，骨骼节点已按名字登记，
 * 供后续动画脚本按名取用。</p>
 */
public class RuntimeStandModel extends HAModelBase implements StandAnimatedModel {

    /** 基岩模型空间的 Y 基准：根骨骼 pivot 的 Y 会换算成 24 - pivotY。 */
    private static final float Y_BASIS = 24f;

    private final String modelId;
    private final HAModelPart root;
    private final Map<String, HAModelPart> bones = new LinkedHashMap<>();
    private int boxCount;

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
            result.bones.put(bone.name, new HAModelPart(result));
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
        // 骨骼动画接入前按静态姿势渲染（动画脚本将在此按 ageTicks 驱动各骨骼角度）
        root.render(matrices, vertices, light, overlay);
    }
}
