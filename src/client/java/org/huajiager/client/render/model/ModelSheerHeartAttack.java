package org.huajiager.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 心锁炸弹实体模型（Killer Queen 第二炸弹）， ModelSheerHeartAttack（Blockbench），纹理 128x128。
 */
public class ModelSheerHeartAttack extends HAModelBase {

	private final HAModelPart head;
	private final HAModelPart ear;
	private final HAModelPart ear1;
	private final HAModelPart ear2;
	private final HAModelPart body;
	private final HAModelPart w1;
	private final HAModelPart w2;

	public ModelSheerHeartAttack() {
		super(128, 128);

		head = new HAModelPart(this);
		head.setRotationPoint(1.0F, 21.0F, -5.0F);
		head.addBox(34, 0, -3.5F, -5.0F, -2.0F, 5, 5, 3, 0.0F, false);
		head.addBox(0, 61, -3.0F, -4.1F, -1.8F, 4, 4, 3, 0.0F, false);

		ear = new HAModelPart(this);
		ear.setRotationPoint(-1.0F, -5.0F, 0.0F);
		setRotationAngle(ear, -0.4363F, 0.0F, 0.0F);
		head.addChild(ear);

		ear1 = new HAModelPart(this);
		ear1.setRotationPoint(-2.5F, 0.0F, 0.0F);
		setRotationAngle(ear1, -0.1745F, 0.4363F, -0.5236F);
		ear.addChild(ear1);
		ear1.addBox(58, 5, -0.5F, -1.0F, 0.0F, 1, 2, 1, 0.0F, false);

		ear2 = new HAModelPart(this);
		ear2.setRotationPoint(2.5F, 0.0F, 0.0F);
		setRotationAngle(ear2, -0.1745F, -0.4363F, 0.5236F);
		ear.addChild(ear2);
		ear2.addBox(58, 5, -0.5F, -1.0F, 0.0F, 1, 2, 1, 0.0F, true);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, 24.0F, 0.0F);
		body.addBox(0, 0, -5.0F, -11.0F, -5.0F, 10, 9, 14, 0.0F, false);
		body.addBox(36, 27, -2.0F, -12.0F, 0.0F, 4, 1, 4, 0.0F, false);
		body.addBox(17, 42, -8.0F, -6.0F, -3.0F, 3, 4, 11, 0.0F, false);
		body.addBox(37, 12, 5.0F, -6.0F, -3.0F, 3, 4, 11, 0.0F, false);
		body.addBox(18, 25, 4.0F, -8.0F, -2.0F, 3, 2, 12, 0.0F, false);
		body.addBox(0, 23, -7.0F, -8.0F, -2.0F, 3, 2, 12, 0.0F, false);

		w1 = new HAModelPart(this);
		w1.setRotationPoint(6.0F, 22.0F, 0.0F);
		w1.addBox(37, 37, -2.0F, -3.0F, -4.0F, 3, 5, 11, 0.0F, false);
		w1.addBox(0, 4, -1.5F, -3.5F, -4.5F, 3, 2, 2, 0.0F, false);

		w2 = new HAModelPart(this);
		w2.setRotationPoint(-6.0F, 22.0F, 0.0F);
		w2.addBox(0, 37, -1.0F, -3.0F, -4.0F, 3, 5, 11, 0.0F, false);
		w2.addBox(0, 0, -1.5F, -3.5F, -4.5F, 3, 2, 2, 0.0F, false);
	}

	public void setRotationAngles(float netHeadYaw, float headPitch) {
		head.rotateAngleX = headPitch * 0.017453292F;
		head.rotateAngleY = netHeadYaw * 0.017453292F;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		w1.render(matrices, vertices, light, overlay, r, g, b, a);
		w2.render(matrices, vertices, light, overlay, r, g, b, a);
	}

	private static void setRotationAngle(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
