package org.huajiager.client.model.custom;

import org.huajiager.client.render.model.HAModelBase;
import org.huajiager.client.render.model.StandAnimatedModel;
import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 借用原版实体模型作为替身造型的运行时模型。
 *
 * <p>直接用烘焙好的原版 {@link ModelPart} 树渲染：原版模型与替身模型同为像素单位，
 * ModelPart 内部会自行 /16 转格，因此尺寸与站位和内置替身一致，不需要额外缩放。</p>
 */
public class VanillaStandModel extends HAModelBase implements StandAnimatedModel {

    private final ModelPart root;

    public VanillaStandModel(ModelPart root) {
        super(64, 64);
        this.root = root;
    }

    public ModelPart root() {
        return root;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
            float b, float a) {
        root.render(matrices, vertices, light, overlay, r, g, b, a);
    }

    @Override
    public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
            EntityStandBase entity, float ageTicks, float speed, float power) {
        // 原版实体模型的自定义姿势需要实体对象才能驱动，这里按烘焙姿势渲染
        root.render(matrices, vertices, light, overlay);
    }
}
