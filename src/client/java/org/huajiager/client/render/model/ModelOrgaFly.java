package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * ORGA REQUIEM 飞行态模型。
 *
 * 数值照搬自 Blockbench 导出模型（64x64 贴图），配合 RenderStandBase 的矩阵（180-yaw + scale(-1,-1,1)）渲染即为
 * 保持一致的飞行姿态：身体绕 X 躺平 90 度（body.rotateAngleX=1.5708）、头部后仰
 * 85 度（head.rotateAngleX=1.4835）+ 左倾 20 度、四肢收拢/张开，背后七根飘带
 * Extra 位于 (0,22,-14) 整体绕 X 躺平。
 * 动画与 render() 一致：head/body/Extra 整体上下漂浮（offsetY）、
 * Extra.rotateAngleZ = ageTicks/2 持续旋转（飞行态以绕 Z 代替绕 Y，视觉上飘带
 * 绕玩家身体轴线旋转）。
 * 位置与 ModelOrgaFly.=(0,-0.9,0) 对齐（正中、高 0.9），由
 * RenderStandBase 的 fly 偏移特判落实，模型本身不再做位移。
 * 第一人称 renderFirst 渲染飞行本体（绕 Z 180 + 前倾 35 度），工程口径与
 * 其它 ORGA 一致：renderHandsStand 空实现（第一人称隐藏本体）。
 */
public class ModelOrgaFly extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart head;
	private final HAModelPart hair1;
	private final HAModelPart hair2;
	private final HAModelPart hair3;
	private final HAModelPart hair4;
	private final HAModelPart hair5;
	private final HAModelPart hair6;
	private final HAModelPart hair7;
	private final HAModelPart hair8;
	private final HAModelPart hair9;
	private final HAModelPart hair10;
	private final HAModelPart body;
	private final HAModelPart leftArm;
	private final HAModelPart lb_1;
	private final HAModelPart lb_2;
	private final HAModelPart rightArm;
	private final HAModelPart rb_1;
	private final HAModelPart rb_2;
	private final HAModelPart rb_3;
	private final HAModelPart leftLeg;
	private final HAModelPart ll_1;
	private final HAModelPart ll_2;
	private final HAModelPart rightLeg;
	private final HAModelPart rl_1;
	private final HAModelPart rl_2;
	private final HAModelPart extra;
	private final HAModelPart hair_p_1;
	private final HAModelPart hair_p_2;
	private final HAModelPart hair_p_3;
	private final HAModelPart hair_p_4;
	private final HAModelPart hair_p_5;
	private final HAModelPart hair_p_6;
	private final HAModelPart hair_p_7;

	public ModelOrgaFly() {
		super(64, 64);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, 22.0F, 0.0F);
		setRotation(head, 1.4835F, 0.0F, -0.3491F);
		head.addBox(0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, true);

		hair1 = new HAModelPart(this);
		hair1.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(hair1);
		hair1.addBox(11, 0, -1.0F, -6.0F, -4.5F, 1, 1, 1, 0.0F, true);

		hair2 = new HAModelPart(this);
		hair2.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(hair2);
		hair2.addBox(33, 8, -4.0F, -8.0F, -4.2F, 7, 6, 0, 0.0F, true);

		hair3 = new HAModelPart(this);
		hair3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair3, -0.0744F, 0.0F, 0.0F);
		head.addChild(hair3);
		hair3.addBox(9, 1, -1.0F, -8.6333F, -4.8333F, 2, 4, 1, 0.0F, true);

		hair4 = new HAModelPart(this);
		hair4.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair4, -0.0744F, -0.0372F, -0.5205F);
		head.addChild(hair4);
		hair4.addBox(10, 1, 0.2F, -8.6F, -4.8F, 2, 4, 1, 0.0F, true);

		hair5 = new HAModelPart(this);
		hair5.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair5, -0.1487F, 0.0372F, 0.632F);
		head.addChild(hair5);
		hair5.addBox(11, 4, -3.2667F, -8.0F, -5.2667F, 2, 4, 1, 0.0F, true);

		hair6 = new HAModelPart(this);
		hair6.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair6, 0.1859F, 0.0F, 0.0F);
		head.addChild(hair6);
		hair6.addBox(0, 51, -3.0F, -8.6F, -2.0667F, 6, 2, 7, 0.0F, true);

		hair7 = new HAModelPart(this);
		hair7.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair7, 0.1487F, 0.1487F, 0.5205F);
		head.addChild(hair7);
		hair7.addBox(31, 51, -2.0667F, -8.5333F, -1.4F, 2, 2, 6, 0.0F, true);

		hair8 = new HAModelPart(this);
		hair8.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair8, 0.1859F, -0.2603F, -0.409F);
		head.addChild(hair8);
		hair8.addBox(31, 51, -0.8667F, -8.5333F, -1.0F, 2, 2, 6, 0.0F, true);

		hair9 = new HAModelPart(this);
		hair9.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair9, 0.3346F, 0.0F, 0.0F);
		head.addChild(hair9);
		hair9.addBox(0, 40, -2.0333F, -8.6667F, -0.4667F, 4, 3, 7, 0.0F, true);

		hair10 = new HAModelPart(this);
		hair10.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair10, 0.5577F, 0.0F, 0.0F);
		head.addChild(hair10);
		hair10.addBox(31, 40, -1.0F, -8.9F, 1.5333F, 2, 3, 5, 0.0F, true);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, 22.0F, 0.0F);
		setRotation(body, 1.5708F, 0.0F, 0.0F);
		body.addBox(16, 16, -3.3572F, -0.0668F, -1.2369F, 8, 12, 4, 0.0F, true);

		leftArm = new HAModelPart(this);
		leftArm.setRotationPoint(-4.0F, 2.0F, 0.0F);
		setRotation(leftArm, 0.0F, 0.0F, 0.8727F);
		body.addChild(leftArm);

		lb_1 = new HAModelPart(this);
		lb_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(lb_1, 0.0F, 0.0F, -0.3491F);
		leftArm.addChild(lb_1);
		lb_1.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F, false);

		lb_2 = new HAModelPart(this);
		lb_2.setRotationPoint(-1.0F, 5.0F, 0.0F);
		setRotation(lb_2, 0.0F, 0.0F, -0.5236F);
		lb_1.addChild(lb_2);
		lb_2.addBox(48, 34, -2.0F, -1.0F, -2.0F, 4, 7, 4, 0.0F, false);

		rightArm = new HAModelPart(this);
		rightArm.setRotationPoint(5.0F, 3.0F, 3.0F);
		body.addChild(rightArm);

		rb_1 = new HAModelPart(this);
		rb_1.setRotationPoint(1.0F, -3.0F, -2.0F);
		setRotation(rb_1, 3.1416F, -0.0873F, 1.0472F);
		rightArm.addChild(rb_1);
		rb_1.addBox(40, 16, -1.5F, -3.0F, -2.0F, 4, 6, 4, 0.0F, true);

		rb_2 = new HAModelPart(this);
		rb_2.setRotationPoint(1.0F, 2.0F, 0.0F);
		setRotation(rb_2, 0.6109F, -1.658F, 0.4363F);
		rb_1.addChild(rb_2);
		rb_2.addBox(48, 34, -1.5F, 0.0F, -2.0F, 4, 7, 4, 0.0F, true);

		rb_3 = new HAModelPart(this);
		rb_3.setRotationPoint(1.0F, 2.0F, 0.0F);
		setRotation(rb_3, 0.6109F, -1.658F, 0.4363F);
		rb_1.addChild(rb_3);
		rb_3.addBox(58, 27, -0.5F, 7.0F, 0.5F, 2, 3, 1, 0.0F, true);

		leftLeg = new HAModelPart(this);
		leftLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
		setRotation(leftLeg, 0.0F, 0.0F, -0.1745F);
		body.addChild(leftLeg);

		ll_1 = new HAModelPart(this);
		ll_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(ll_1, 0.3491F, 1.6581F, 1.0472F);
		leftLeg.addChild(ll_1);
		ll_1.addBox(0, 16, -2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F, false);

		ll_2 = new HAModelPart(this);
		ll_2.setRotationPoint(0.0F, 11.5F, 0.0F);
		setRotation(ll_2, 0.5236F, -0.2618F, 0.0F);
		ll_1.addChild(ll_2);
		ll_2.addBox(48, 49, -2.1F, -5.5F, 1.0F, 4, 9, 4, 0.0F, false);

		rightLeg = new HAModelPart(this);
		rightLeg.setRotationPoint(2.9F, 11.0F, 0.0F);
		body.addChild(rightLeg);

		rl_1 = new HAModelPart(this);
		rl_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		rightLeg.addChild(rl_1);
		rl_1.addBox(0, 16, -2.0F, 0.0F, -2.0F, 4, 7, 4, 0.0F, true);

		rl_2 = new HAModelPart(this);
		rl_2.setRotationPoint(0.0F, 7.0F, 0.0F);
		setRotation(rl_2, 0.2618F, 0.0F, 0.0F);
		rl_1.addChild(rl_2);
		rl_2.addBox(48, 49, -1.9F, -1.0F, -2.0F, 4, 9, 4, 0.0F, true);

		extra = new HAModelPart(this);
		extra.setRotationPoint(0.0F, 22.0F, -14.0F);
		setRotation(extra, 1.5708F, 0.0F, 0.0F);

		hair_p_1 = new HAModelPart(this);
		hair_p_1.setRotationPoint(-8.5F, 1.0F, -11.0F);
		setRotation(hair_p_1, -0.9599F, 0.0873F, 0.0F);
		extra.addChild(hair_p_1);
		hair_p_1.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_2 = new HAModelPart(this);
		hair_p_2.setRotationPoint(2.5F, 1.0F, -11.0F);
		setRotation(hair_p_2, -0.9599F, -0.3491F, 0.0F);
		extra.addChild(hair_p_2);
		hair_p_2.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_3 = new HAModelPart(this);
		hair_p_3.setRotationPoint(10.5F, 2.0F, -7.0F);
		setRotation(hair_p_3, -0.9599F, -1.5708F, 0.0F);
		extra.addChild(hair_p_3);
		hair_p_3.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_4 = new HAModelPart(this);
		hair_p_4.setRotationPoint(10.5F, 3.0F, 6.0F);
		setRotation(hair_p_4, -0.9599F, -2.618F, 0.0F);
		extra.addChild(hair_p_4);
		hair_p_4.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_5 = new HAModelPart(this);
		hair_p_5.setRotationPoint(1.5F, 3.0F, 13.0F);
		setRotation(hair_p_5, -0.9599F, 2.1817F, 0.0F);
		extra.addChild(hair_p_5);
		hair_p_5.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_6 = new HAModelPart(this);
		hair_p_6.setRotationPoint(-10.5F, 3.0F, 9.0F);
		setRotation(hair_p_6, -0.9599F, 1.309F, 0.0F);
		extra.addChild(hair_p_6);
		hair_p_6.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);

		hair_p_7 = new HAModelPart(this);
		hair_p_7.setRotationPoint(-15.5F, 3.0F, -3.0F);
		setRotation(hair_p_7, -0.9599F, 0.6109F, 0.0F);
		extra.addChild(hair_p_7);
		hair_p_7.addBox(41, 9, -2.5F, -3.0F, 0.0F, 5, 6, 0, 0.0F, false);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 无动画兜底：静态渲染当前姿态（各部件已按构造值设好角度）
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		applyStandAnimation(ageTicks, speed, power);
		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/**
	 * 攻击态第一人称专用渲染： ModelOrgaFly.renderFirst 渲染飞行本体（绕 Z 180 +
	 * 前倾 35 度），工程口径与其它 ORGA 一致——第一人称不渲染本体，避免本体遮挡视野。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		// 第一人称不渲染任何部件
	}

	/** renderStand 共用的替身动画：整体漂浮 + Extra 绕 Z 持续旋转（照搬 render()）。 */
	private void applyStandAnimation(float ageTicks, float speed, float power) {
		//  render()：head/body/Extra 整体上下漂浮（offsetY 随 cos(0.1*age)）
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.15);
		head.offsetY = off;
		body.offsetY = off;
		extra.offsetY = off;

		//  setRotationAngles()：Extra.rotateAngleZ = rotateFloat / 2
		// （飞行态整体躺平，飘带组绕 Z 持续旋转）
		extra.rotateAngleZ = ageTicks / 2.0F;
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		extra.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
