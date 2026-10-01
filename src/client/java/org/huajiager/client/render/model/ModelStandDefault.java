package org.huajiager.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 替身实体标准人形模型。
 *
 * 采用 1.20.1 BipedEntityModel 的标准布局（root pivot(0,24,0)，部件坐标全部
 * 使用 HumanoidModel 数值：head box(-4,-8,-4,8,8,8)、body box(-4,0,-2,8,12,4)
 * 等），与 LivingEntityRenderer 渲染玩家的矩阵语义完全一致，因此在外部
 * scale(-1,-1,1)+180-yaw 矩阵下必然正立、站位正确（脚贴实体位置）。
 * 若沿用旧 1.8 布局（pivot 全部挂 24、box 用 0..12 正坐标）会在新版 Cuboid 语义下
 * 整体压入地面并互相重叠，导致"倒立/错位/深埋"。
 */
public class ModelStandDefault extends HAModelBase {

	private final HAModelPart root;

	public ModelStandDefault() {
		// 贴图为 64x128：textureHeight 必须为 128，否则 UV 的 V 归一化错位。
		super(64, 128);

		this.root = new HAModelPart(this, 0, 0);
		//  BipedEntityModel root pivot：模型空间原点放到脚底上方 1.5 格处，
		// 配合外部 scale(-1,-1,1) 使模型正立、脚贴实体位置。
		this.root.setRotationPoint(0f, 24f, 0f);

		HAModelPart head = new HAModelPart(this, 0, 0)
				.addBox(-4f, -8f, -4f, 8f, 8f, 8f);
		head.setRotationPoint(0f, 0f, 0f);

		HAModelPart body = new HAModelPart(this, 16, 16)
				.addBox(-4f, 0f, -2f, 8f, 12f, 4f);
		body.setRotationPoint(0f, 0f, 0f);

		HAModelPart rightArm = new HAModelPart(this, 40, 16)
				.addBox(-3f, -2f, -2f, 4f, 12f, 4f);
		rightArm.setRotationPoint(-5f, 2f, 0f);

		HAModelPart leftArm = new HAModelPart(this, 40, 16)
				.addBox(-1f, -2f, -2f, 4f, 12f, 4f)
				.setMirror(true);
		leftArm.setRotationPoint(5f, 2f, 0f);

		HAModelPart rightLeg = new HAModelPart(this, 0, 16)
				.addBox(-1.9f, 0f, -2f, 4f, 12f, 4f);
		rightLeg.setRotationPoint(-1.9f, 12f, 0f);

		HAModelPart leftLeg = new HAModelPart(this, 0, 16)
				.addBox(-1.9f, 0f, -2f, 4f, 12f, 4f)
				.setMirror(true);
		leftLeg.setRotationPoint(1.9f, 12f, 0f);

		this.root.addChild(head);
		this.root.addChild(body);
		this.root.addChild(rightArm);
		this.root.addChild(leftArm);
		this.root.addChild(rightLeg);
		this.root.addChild(leftLeg);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		renderPart(root, matrices, vertices, light, overlay);
	}
}
