package org.huajiager.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 万刀实体模型， ModelMuliKnife（Techne），纹理 64x32。
 */
public class ModelMuliKnife extends HAModelBase {

	private final HAModelPart shape1;
	private final HAModelPart shape2;
	private final HAModelPart shape3;
	private final HAModelPart shape4;
	private final HAModelPart shape5;
	private final HAModelPart shape6;
	private final HAModelPart shape7;
	private final HAModelPart shape8;

	public ModelMuliKnife() {
		super(64, 32);

		shape1 = new HAModelPart(this, 0, 27);
		shape1.addBox(-4.6F, 0F, -3F, 2, 2, 3);
		shape1.setRotationPoint(3F, 16F - 17f, 0F);
		shape1.mirror = true;
		setRotation(shape1, 0.8179294F, 0F, 0F);

		shape2 = new HAModelPart(this, 0, 27);
		shape2.addBox(-0.3F, 0F, -3F, 2, 2, 3);
		shape2.setRotationPoint(0F, 16F - 17f, 0F);
		setRotation(shape2, 0.8179294F, 0F, 0F);

		shape3 = new HAModelPart(this, 0, 17);
		shape3.addBox(0F, 0F, 0F, 2, 3, 2);
		shape3.setRotationPoint(-1F, 15.33333F - 17f, -1.6F);
		shape3.setTextureSize(64, 32);
		shape3.mirror = true;

		shape4 = new HAModelPart(this, 12, 23);
		shape4.addBox(-1F, 0F, 0F, 2, 7, 2);
		shape4.setRotationPoint(0F, 17F - 17f, -1F);
		shape4.setTextureSize(64, 32);
		shape4.mirror = true;

		shape5 = new HAModelPart(this, 0, 0);
		shape5.addBox(-0.5F, 0F, 0F, 1, 12, 2);
		shape5.setRotationPoint(0F, 3.866667F - 17f, -1.5F);
		shape5.setTextureSize(64, 32);
		shape5.mirror = true;

		shape6 = new HAModelPart(this, 0, 0);
		shape6.addBox(0F, 0F, -1F, 1, 11, 3);
		shape6.setRotationPoint(-0.5F, 7F - 17f, -2.5F);
		shape6.setTextureSize(64, 32);
		shape6.mirror = true;
		setRotation(shape6, 0.1487144F, 0F, 0F);

		shape7 = new HAModelPart(this, 0, 0);
		shape7.addBox(0F, -0.7666667F, 0.3F, 1, 5, 2);
		shape7.setRotationPoint(-0.5F, 3.4F - 17f, -2F);
		shape7.setTextureSize(64, 32);
		shape7.mirror = true;
		setRotation(shape7, -0.4461433F, 0F, 0F);

		shape8 = new HAModelPart(this, 0, 0);
		shape8.addBox(0F, -0.1333333F, 0F, 1, 1, 2);
		shape8.setRotationPoint(-0.5F, 2.333333F - 17f, -0.8666667F);
		shape8.setTextureSize(64, 32);
		shape8.mirror = true;
		setRotation(shape8, -0.8179294F, 0F, 0F);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		shape1.render(matrices, vertices, light, overlay, r, g, b, a);
		shape2.render(matrices, vertices, light, overlay, r, g, b, a);
		shape3.render(matrices, vertices, light, overlay, r, g, b, a);
		shape4.render(matrices, vertices, light, overlay, r, g, b, a);
		shape5.render(matrices, vertices, light, overlay, r, g, b, a);
		shape6.render(matrices, vertices, light, overlay, r, g, b, a);
		shape7.render(matrices, vertices, light, overlay, r, g, b, a);
		shape8.render(matrices, vertices, light, overlay, r, g, b, a);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
