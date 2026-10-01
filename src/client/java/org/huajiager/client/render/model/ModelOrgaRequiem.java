package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * ORGA REQUIEM 模型。
 *
 * 数值照搬自 Blockbench 导出模型（64x64 贴图），配合 RenderStandBase 的矩阵（180-yaw + scale(-1,-1,1)）渲染即为
 * 保持一致的正立悬浮造型（黑色长发 + 背后七根飘带 Extra）。
 * 动画与 render() 一致：整体上下漂浮（offsetY）、背后七根飘带各自相位上下摆动、
 * Extra 绕 Y 轴持续旋转； setRotationAngles / setPunch / renderFirst 均为空实现——
 * 没有挥拳动画，攻击态第一人称也不渲染本体（renderHandsStand 为空）。
 * 位置与 ModelOrgaRequiem.=(-0.5,-0.7,0.75) 对齐（右 0.5 / 高 0.7 / 后 0.75），
 * 由 RenderStandBase 的 idle/attack 偏移特判落实，模型本身不再做位移。
 */
public class ModelOrgaRequiem extends HAModelBase implements StandAnimatedModel {

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

	public ModelOrgaRequiem() {
		super(64, 64);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, 0.0F, 0.0F);
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
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(body, -0.0873F, -0.6981F, 0.0F);
		body.addBox(16, 16, -3.3572F, -0.0668F, -1.2369F, 8, 12, 4, 0.0F, true);

		leftArm = new HAModelPart(this);
		leftArm.setRotationPoint(-5.0F, 2.0F, -1.0F);
		setRotation(leftArm, 0.0F, 0.3491F, 0.5236F);

		lb_1 = new HAModelPart(this);
		lb_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		leftArm.addChild(lb_1);
		lb_1.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F, false);

		lb_2 = new HAModelPart(this);
		lb_2.setRotationPoint(-1.0F, 5.0F, 0.0F);
		setRotation(lb_2, 0.0F, 0.0F, -1.309F);
		leftArm.addChild(lb_2);
		lb_2.addBox(48, 34, -2.0F, -1.0F, -2.0F, 4, 7, 4, 0.0F, false);

		rightArm = new HAModelPart(this);
		rightArm.setRotationPoint(5.0F, 3.0F, 3.0F);

		rb_1 = new HAModelPart(this);
		rb_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(rb_1, 0.0F, 0.1745F, 0.0F);
		rightArm.addChild(rb_1);
		rb_1.addBox(40, 16, -1.0F, -3.0F, -2.0F, 4, 6, 4, 0.0F, true);

		rb_2 = new HAModelPart(this);
		rb_2.setRotationPoint(0.0F, 3.0F, 0.0F);
		setRotation(rb_2, -0.1745F, -0.4363F, 0.0F);
		rightArm.addChild(rb_2);
		rb_2.addBox(48, 34, -1.0F, 0.0F, -2.0F, 4, 7, 4, 0.0F, true);

		leftLeg = new HAModelPart(this);
		leftLeg.setRotationPoint(-2.9F, 11.0F, 0.0F);
		setRotation(leftLeg, -0.6109F, -0.4363F, 0.0F);

		ll_1 = new HAModelPart(this);
		ll_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(ll_1, -0.6109F, 0.0F, 0.0F);
		leftLeg.addChild(ll_1);
		ll_1.addBox(0, 16, -2.0F, 0.0F, -2.0F, 4, 9, 4, 0.0F, false);

		ll_2 = new HAModelPart(this);
		ll_2.setRotationPoint(0.0F, 6.5F, -2.0F);
		setRotation(ll_2, 0.6981F, 0.0873F, 0.0F);
		leftLeg.addChild(ll_2);
		ll_2.addBox(48, 49, -2.0F, -2.0F, -3.0F, 4, 9, 4, 0.0F, false);

		rightLeg = new HAModelPart(this);
		rightLeg.setRotationPoint(1.9F, 12.0F, 2.0F);
		setRotation(rightLeg, 0.0873F, -1.1344F, 0.0F);

		rl_1 = new HAModelPart(this);
		rl_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		rightLeg.addChild(rl_1);
		rl_1.addBox(0, 16, -2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F, true);

		rl_2 = new HAModelPart(this);
		rl_2.setRotationPoint(0.0F, 7.0F, 0.0F);
		setRotation(rl_2, 0.0F, 0.1745F, 0.0F);
		rightLeg.addChild(rl_2);
		rl_2.addBox(48, 49, -2.0F, -2.0F, -2.0F, 4, 9, 4, 0.0F, true);

		// 背后七根发带（Extra 子部件）：渲染时整体放大 1.2 倍（render scale*1.2）
		extra = new HAModelPart(this);
		extra.setRotationPoint(0.0F, 3.0F, 0.0F);
		setRotation(extra, 0.0F, 0.0F, -0.8727F);
		extra.xScale = 1.2F;
		extra.yScale = 1.2F;
		extra.zScale = 1.2F;

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
	 * 攻击态第一人称专用渲染： ModelOrgaRequiem.renderFirst 为空实现——
	 * 第一人称不渲染本体，此处保持空，避免本体遮挡视野。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		//  renderFirst 为空：第一人称不渲染任何部件
	}

	/** renderStand 共用的替身动画：上下漂浮 + 背后发带摆动 + Extra 绕 Y 旋转（照搬 render()）。 */
	private void applyStandAnimation(float ageTicks, float speed, float power) {
		//  render()：整体上下漂浮（offsetY 随 cos(0.1*age)）
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.15);
		head.offsetY = off;
		leftArm.offsetY = off * 0.8F;
		rightArm.offsetY = off * 0.8F;
		body.offsetY = off;
		leftLeg.offsetY = off;
		rightLeg.offsetY = off;

		// 背后七根发带：offsetY 随 sin(0.05*age + 相位) * 0.25，相位均布一圈
		float a = 0.05F * ageTicks;
		hair_p_1.offsetY = MathHelper.sin(a) * 0.25F;
		hair_p_2.offsetY = MathHelper.sin(a + 1.0471976F) * 0.25F;
		hair_p_3.offsetY = MathHelper.sin(a + 2.0943952F) * 0.25F;
		hair_p_4.offsetY = MathHelper.sin(a + (float) Math.PI) * 0.25F;
		hair_p_5.offsetY = MathHelper.sin(a + 4.1887903F) * 0.25F;
		hair_p_6.offsetY = MathHelper.sin(a + 5.2359877F) * 0.25F;
		hair_p_7.offsetY = MathHelper.sin(a + (float) (Math.PI * 2)) * 0.25F;

		//  render()：Extra.rotateAngleY = ageInTicks / 2 —— 背后发带组持续旋转
		extra.rotateAngleY = ageTicks / 2.0F;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		leftArm.render(matrices, vertices, light, overlay, r, g, b, a);
		rightArm.render(matrices, vertices, light, overlay, r, g, b, a);
		leftLeg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightLeg.render(matrices, vertices, light, overlay, r, g, b, a);
		extra.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
