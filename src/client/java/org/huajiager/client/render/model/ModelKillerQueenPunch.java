package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Killer Queen 模型（punch / 攻击态）。
 *
 * 数值照搬自 Blockbench 导出；
 * textureWidth/Height=128 与 entity_killer_queen_punch.png 贴图 UV 完全匹配。
 * 攻击态保留 render() 的上下漂浮 + 头部正视，以及 setRotationAngles 的
 * 十指"欧拉"挥拳摆动（power 驱动，speed 取替身速度）。
 * renderHandsStand 对齐 renderFirst：第一人称仅渲染两只舞动的拳头，
 * 本体从视野中消失（Killer Queen 攻击态位于玩家正前方，只呈现前方拳头）。
 */
public class ModelKillerQueenPunch extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart body;
	private final HAModelPart Shape10;
	private final HAModelPart Shape11;
	private final HAModelPart bodydown;
	private final HAModelPart cloth;
	private final HAModelPart cloth1;
	private final HAModelPart cloth2;
	private final HAModelPart cloth3;
	private final HAModelPart cloth4;
	private final HAModelPart cloth5;
	private final HAModelPart cloth6;
	private final HAModelPart crotch;
	private final HAModelPart leftarm;
	private final HAModelPart rightarm;
	private final HAModelPart head;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart hands_r;
	private final HAModelPart r_hand1;
	private final HAModelPart r_hand2;
	private final HAModelPart r_hand3;
	private final HAModelPart r_hand4;
	private final HAModelPart r_hand5;
	private final HAModelPart hands_l;
	private final HAModelPart l_hand1;
	private final HAModelPart l_hand2;
	private final HAModelPart l_hand3;
	private final HAModelPart l_hand4;
	private final HAModelPart l_hand5;

	public ModelKillerQueenPunch() {
		super(128, 128);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, -2.0F, 0.0F);
		setRotation(body, 0.1745F, 0.0F, 0.0F);
		body.addBox(0, 23, -4.0F, 0.0F, -2.0F, 8, 7, 4, 0.0F, false);

		Shape10 = new HAModelPart(this);
		Shape10.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape10, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape10);
		Shape10.addBox(52, 36, -3.5F, 0.2F, -2.5F, 7, 4, 1, 0.0F, false);

		Shape11 = new HAModelPart(this);
		Shape11.setRotationPoint(0.0F, 0.0F, -2.3F);
		setRotation(Shape11, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape11);
		Shape11.addBox(0, 0, -1.5F, 4.0F, 0.0F, 3, 3, 1, 0.0F, false);

		bodydown = new HAModelPart(this);
		bodydown.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(bodydown, -0.0175F, 0.0F, 0.0F);
		body.addChild(bodydown);
		bodydown.addBox(16, 45, -3.5F, 7.0F, -2.0F, 7, 4, 4, 0.0F, false);

		cloth = new HAModelPart(this);
		cloth.setRotationPoint(0.0F, 0.0F, 0.0F);
		body.addChild(cloth);

		cloth1 = new HAModelPart(this);
		cloth1.setRotationPoint(0.0F, 12.2987F, -4.151F);
		setRotation(cloth1, -0.4978F, 0.0F, 0.0F);
		cloth.addChild(cloth1);
		cloth1.addBox(0, 4, -1.0F, -2.5F, -0.5F, 2, 3, 1, 0.0F, false);

		cloth2 = new HAModelPart(this);
		cloth2.setRotationPoint(0.0F, 10.9128F, 0.9962F);
		setRotation(cloth2, 0.6352F, 0.0F, 0.0F);
		cloth.addChild(cloth2);
		cloth2.addBox(42, 30, -4.5F, 0.0F, 1.0F, 9, 5, 1, 0.0F, false);

		cloth3 = new HAModelPart(this);
		cloth3.setRotationPoint(-3.0F, 10.9128F, 0.9962F);
		setRotation(cloth3, -0.063F, 0.0873F, 0.6109F);
		cloth.addChild(cloth3);
		cloth3.addBox(46, 49, -2.0F, -0.4128F, -4.0F, 1, 5, 6, 0.0F, false);

		cloth4 = new HAModelPart(this);
		cloth4.setRotationPoint(3.0F, 9.9128F, 0.9962F);
		setRotation(cloth4, -0.063F, -0.0873F, -0.7854F);
		cloth.addChild(cloth4);
		cloth4.addBox(32, 49, 0.5F, 0.0F, -4.0F, 1, 5, 6, 0.0F, false);

		cloth5 = new HAModelPart(this);
		cloth5.setRotationPoint(3.8146F, 11.6044F, 0.3294F);
		setRotation(cloth5, -0.5866F, -0.0873F, -0.4363F);
		cloth.addChild(cloth5);
		cloth5.addBox(0, 57, -3.8146F, 0.0F, -4.0F, 5, 5, 1, 0.0F, false);

		cloth6 = new HAModelPart(this);
		cloth6.setRotationPoint(-3.6516F, 10.6473F, 0.8066F);
		setRotation(cloth6, -0.412F, 0.1746F, 0.4363F);
		cloth.addChild(cloth6);
		cloth6.addBox(40, 49, -0.5F, 0.0F, -4.0F, 5, 5, 1, 0.0F, false);

		crotch = new HAModelPart(this);
		crotch.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(crotch, 0.0349F, 0.0F, 0.0F);
		cloth.addChild(crotch);
		crotch.addBox(0, 15, -4.0F, 10.0F, -3.5F, 8, 2, 6, 0.0F, false);

		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(leftarm, -0.2618F, 0.0F, 0.0F);
		body.addChild(leftarm);
		leftarm.addBox(0, 48, -1.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, false);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-6.0F, 2.0F, 0.5F);
		setRotation(rightarm, -0.6109F, 0.0F, 0.0F);
		body.addChild(rightarm);
		rightarm.addBox(47, 0, -2.0F, -2.0F, -2.5F, 4, 4, 5, 0.0F, false);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, -1.0F, 0.0F);
		head.addBox(0, 0, -3.5F, -7.0F, -4.0F, 7, 7, 8, 0.0F, false);
		head.addBox(55, 55, 2.5F, -9.0F, -3.0F, 1, 2, 5, 0.0F, false);
		head.addBox(22, 0, -2.5F, -8.0F, -3.5F, 5, 1, 7, 0.0F, false);
		head.addBox(54, 44, -3.5F, -9.0F, -3.0F, 1, 2, 5, 0.0F, false);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 11.0F, 3.0F);
		setRotation(leftleg, -0.2003F, -0.0502F, -0.2595F);
		leftleg.addBox(37, 37, -2.0F, -2.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(0.1482F, 6.2009F, -0.5752F);
		setRotation(legdownl, 0.6109F, 0.0F, 0.0F);
		leftleg.addChild(legdownl);
		legdownl.addBox(34, 8, -1.6482F, -1.5F, -1.5F, 4, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 10.0F, 4.0F);
		setRotation(rightleg, -0.538F, -0.0114F, 0.2482F);
		rightleg.addBox(0, 34, -3.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(0.2077F, 3.6937F, 3.8524F);
		setRotation(legdownr, 0.7854F, 0.0F, 0.0F);
		rightleg.addChild(legdownr);
		legdownr.addBox(24, 27, -2.7077F, -1.5F, -7.5F, 4, 9, 5, 0.0F, false);

		hands_r = new HAModelPart(this);
		hands_r.setRotationPoint(0.0F, 0.0F, 0.0F);

		r_hand1 = new HAModelPart(this);
		r_hand1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(r_hand1, -1.5708F, 1.5708F, 0.0F);
		hands_r.addChild(r_hand1);
		r_hand1.addBox(38, 23, -8.0F, 19.0F, -1.0F, 8, 3, 4, 0.0F, false);

		r_hand2 = new HAModelPart(this);
		r_hand2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(r_hand2, -1.5708F, 1.5708F, 0.0F);
		hands_r.addChild(r_hand2);
		r_hand2.addBox(38, 23, -3.0F, 12.0F, 5.0F, 8, 3, 4, 0.0F, false);

		r_hand3 = new HAModelPart(this);
		r_hand3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(r_hand3, -1.5708F, 1.5708F, 0.0F);
		hands_r.addChild(r_hand3);
		r_hand3.addBox(38, 23, 0.0F, 12.0F, -8.0F, 8, 3, 4, 0.0F, false);

		r_hand4 = new HAModelPart(this);
		r_hand4.setRotationPoint(-2.0F, 6.0F, 7.0F);
		setRotation(r_hand4, -1.5708F, 1.5708F, 0.0F);
		hands_r.addChild(r_hand4);
		r_hand4.addBox(38, 23, 4.0F, 10.0F, -8.0F, 8, 3, 4, 0.0F, false);

		r_hand5 = new HAModelPart(this);
		r_hand5.setRotationPoint(-3.0F, 4.0F, -5.0F);
		setRotation(r_hand5, -1.5708F, 1.5708F, 0.0F);
		hands_r.addChild(r_hand5);
		r_hand5.addBox(38, 23, 0.0F, 12.0F, -8.0F, 8, 3, 4, 0.0F, false);

		hands_l = new HAModelPart(this);
		hands_l.setRotationPoint(0.0F, 0.0F, 0.0F);

		l_hand1 = new HAModelPart(this);
		l_hand1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(l_hand1, -1.5708F, -1.5708F, 0.0F);
		hands_l.addChild(l_hand1);
		l_hand1.addBox(38, 23, -12.0F, 18.0F, -1.0F, 8, 3, 4, 0.0F, true);

		l_hand2 = new HAModelPart(this);
		l_hand2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(l_hand2, -1.5708F, -1.5708F, 0.0F);
		hands_l.addChild(l_hand2);
		l_hand2.addBox(38, 23, -4.0F, 13.0F, 5.0F, 8, 3, 4, 0.0F, true);

		l_hand3 = new HAModelPart(this);
		l_hand3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(l_hand3, -1.5708F, -1.5708F, 0.0F);
		hands_l.addChild(l_hand3);
		l_hand3.addBox(38, 23, 0.0F, 13.0F, -8.0F, 8, 3, 4, 0.0F, true);

		l_hand4 = new HAModelPart(this);
		l_hand4.setRotationPoint(3.0F, 6.0F, 4.0F);
		setRotation(l_hand4, -1.5708F, -1.5708F, 0.0F);
		hands_l.addChild(l_hand4);
		l_hand4.addBox(38, 23, 0.0F, 10.0F, -8.0F, 8, 3, 4, 0.0F, true);

		l_hand5 = new HAModelPart(this);
		l_hand5.setRotationPoint(-2.0F, 3.0F, -10.0F);
		setRotation(l_hand5, -1.5708F, -1.5708F, 0.0F);
		hands_l.addChild(l_hand5);
		l_hand5.addBox(38, 23, 0.0F, 13.0F, -8.0F, 8, 3, 4, 0.0F, true);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		applyStandAnimation(ageTicks, speed, power);
		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/**
	 * 攻击态第一人称专用渲染：只画两只挥拳（hands_l/hands_r），不画本体。
	 * 对齐 ModelKillerQueenPunch.renderFirst（第一人称叠加层仅渲染双手）。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power, float alpha) {
		applyStandAnimation(ageTicks, speed, power);
		// 第一人称攻击态拳头半透明：对齐 renderFirst 的 GlStateManager.color(1,1,1,alpha)，
		// 调用方需传入 translucent 层（cutout 层忽略 alpha）。
		hands_l.render(matrices, vertices, light, overlay, 1f, 1f, 1f, alpha);
		hands_r.render(matrices, vertices, light, overlay, 1f, 1f, 1f, alpha);
	}

	/**  render() 漂浮 + 头部正视 + 挥拳动画（setRotationAngles）。 */
	private void applyStandAnimation(float ageTicks, float speed, float power) {
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.1);
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;

		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;

		setRotationAngles(ageTicks, 0f, 0f, power, speed);
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
		hands_r.render(matrices, vertices, light, overlay, r, g, b, a);
		hands_l.render(matrices, vertices, light, overlay, r, g, b, a);
	}

	/**
	 *  setRotationAngles：十指围绕挥拳摆动。rotateFloat=ageTicks，
	 * rotateYaw/rotatePitch 替身实体场景传 0。频率沿用 ModelTheWorld 口径
	 * （speed*4/3，speed<=0 时按 1.6 兜底）。
	 */
	public void setRotationAngles(float rotateFloat, float rotateYaw, float rotatePitch, float power, float speed) {
		float freq = speed > 0 ? speed * 4.0F / 3.0F : 1.6F;
		float f = rotateFloat * freq;

		l_hand1.rotateAngleY = -45 + MathHelper.cos(f) * 1.2F * power;
		l_hand2.rotateAngleY = -45 + MathHelper.cos(f + (float) Math.PI / 3) * 1.4F * power;
		l_hand3.rotateAngleY = -45 + MathHelper.cos(f + (float) (2 * Math.PI / 3)) * 1.6F * power;
		l_hand4.rotateAngleY = -45 + MathHelper.cos(f + (float) (2.5 * Math.PI / 3)) * 1.3F * power;
		l_hand5.rotateAngleY = -45 + MathHelper.cos(f + (float) (3 * Math.PI / 3)) * 1.3F * power;
		r_hand1.rotateAngleY = 45 - MathHelper.cos(f + (float) (3.5 * Math.PI / 3)) * 1.2F * power;
		r_hand2.rotateAngleY = 45 - MathHelper.cos(f + (float) (6 * Math.PI / 3)) * 1.1F * power;
		r_hand3.rotateAngleY = 45 - MathHelper.cos(f + (float) (9 * Math.PI / 3)) * 1.2F * power;
		r_hand4.rotateAngleY = 45 - MathHelper.cos(f + (float) (10 * Math.PI / 3)) * 0.7F * power;
		r_hand5.rotateAngleY = 45 - MathHelper.cos(f + (float) (1.5 * Math.PI / 3)) * 1.6F * power;

		hands_l.rotateAngleX = rotatePitch * 0.017453292F;
		hands_l.rotateAngleY = rotateYaw * 0.017453292F;
		hands_r.rotateAngleX = rotatePitch * 0.017453292F;
		hands_r.rotateAngleY = rotateYaw * 0.017453292F;
	}
}
