package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * STAR PLATINUM 官方模型 1.20.1 移植版。
 *
 * 数值照搬官方 （Techne 导出 ），
 * 配合 RenderStandBase 的官方矩阵（180-yaw + scale(-1,-1,1)）渲染即为保持一致的正立悬浮造型。
 * 结构/动画实现与 ModelTheWorld 同构：漂浮 offsetY + 手部十二连 cos 摆动（power 驱动），
 * 攻击态第一人称 renderHandsStand 只画双手挥拳。
 */
public class ModelStarPlatinum extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart head;
	private final HAModelPart hair1;
	private final HAModelPart hair2;
	private final HAModelPart hair3;
	private final HAModelPart hair4;
	private final HAModelPart hair5;
	private final HAModelPart hair6;
	private final HAModelPart hat;
	private final HAModelPart body;
	private final HAModelPart Shape10;
	private final HAModelPart Shape11;
	private final HAModelPart armorl;
	private final HAModelPart armorr;
	private final HAModelPart bodydown;
	private final HAModelPart cloth1;
	private final HAModelPart cloth2;
	private final HAModelPart crotch;
	private final HAModelPart leftarm;
	private final HAModelPart rightarm;
	private final HAModelPart scarf;
	private final HAModelPart scarf2;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart left_hands;
	private final HAModelPart handl1;
	private final HAModelPart handl2;
	private final HAModelPart handl3;
	private final HAModelPart handl4;
	private final HAModelPart handl5;
	private final HAModelPart right_hands;
	private final HAModelPart handr1;
	private final HAModelPart handr2;
	private final HAModelPart handr3;
	private final HAModelPart handr4;
	private final HAModelPart handr5;

	public ModelStarPlatinum() {
		super(64, 128);

		body = new HAModelPart(this, 16, 16);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(body, -0.0873F, 0.0F, 0.0F);
		body.addBox(16, 16, -4.0F, 0.0F, -2.0F, 8, 7, 4, 0.0F, false);

		Shape10 = new HAModelPart(this);
		Shape10.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape10, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape10);
		Shape10.addBox(35, 49, -3.5F, 0.2F, -2.5F, 7, 4, 1, 0.0F, false);

		Shape11 = new HAModelPart(this);
		Shape11.setRotationPoint(0.0F, 0.0F, -2.3F);
		setRotation(Shape11, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape11);
		Shape11.addBox(35, 56, -1.5F, 4.0F, 0.0F, 3, 3, 1, 0.0F, false);

		armorl = new HAModelPart(this);
		armorl.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(armorl, 0.5236F, 0.0F, 0.0F);
		body.addChild(armorl);
		armorl.addBox(0, 74, 5.0F, -1.0F, -3.0F, 4, 1, 6, 0.0F, false);

		armorr = new HAModelPart(this);
		armorr.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(armorr, 0.5236F, 0.0F, 0.0F);
		body.addChild(armorr);
		armorr.addBox(0, 74, -9.0F, -1.0F, -3.0F, 4, 1, 6, 0.0F, true);

		bodydown = new HAModelPart(this);
		bodydown.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(bodydown, -0.0175F, 0.0F, 0.0F);
		body.addChild(bodydown);
		bodydown.addBox(19, 66, -3.5F, 7.0F, -2.0F, 7, 4, 4, 0.0F, false);

		cloth1 = new HAModelPart(this);
		cloth1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(cloth1, -0.1487F, 0.0F, 0.0F);
		body.addChild(cloth1);
		cloth1.addBox(48, 67, -2.0F, 11.0F, -1.9F, 4, 8, 1, 0.0F, false);

		cloth2 = new HAModelPart(this);
		cloth2.setRotationPoint(0.0F, 10.9128F, 0.9962F);
		setRotation(cloth2, 0.2861F, 0.0F, 0.0F);
		body.addChild(cloth2);
		cloth2.addBox(48, 56, -2.5F, 0.0F, 1.0F, 5, 9, 1, 0.0F, false);

		crotch = new HAModelPart(this);
		crotch.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(crotch, 0.0349F, 0.0F, 0.0F);
		body.addChild(crotch);
		crotch.addBox(16, 82, -4.0F, 10.0F, -3.5F, 8, 2, 6, 0.0F, false);

		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(leftarm, 0.5236F, 0.0F, 0.0F);
		body.addChild(leftarm);
		leftarm.addBox(40, 16, -1.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, false);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		setRotation(rightarm, 0.5236F, 0.0F, 0.0F);
		body.addChild(rightarm);
		rightarm.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, true);

		scarf = new HAModelPart(this);
		scarf.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(scarf, 0.4072F, 0.0F, 0.0F);
		body.addChild(scarf);
		scarf.addBox(24, 35, -5.0F, -1.0F, -4.0F, 10, 2, 10, 0.0F, false);

		scarf2 = new HAModelPart(this);
		scarf2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(scarf2, 0.0726F, 0.0F, 0.0F);
		body.addChild(scarf2);
		scarf2.addBox(28, 35, -4.5F, -1.0F, -4.5F, 9, 3, 9, 0.0F, false);

		left_hands = new HAModelPart(this);
		left_hands.setRotationPoint(0.0F, 0.0F, 0.0F);

		handl1 = new HAModelPart(this);
		handl1.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(handl1, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl1);
		handl1.addBox(48, 4, 8.0F, 10.0F, 3.0F, 4, 8, 4, 0.0F, false);

		handl2 = new HAModelPart(this);
		handl2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handl2, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl2);
		handl2.addBox(48, 4, 9.0F, 3.0F, 0.0F, 4, 8, 4, 0.0F, false);

		handl3 = new HAModelPart(this);
		handl3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handl3, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl3);
		handl3.addBox(48, 4, 7.0F, -5.0F, 4.0F, 4, 8, 4, 0.0F, false);

		handl4 = new HAModelPart(this);
		handl4.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handl4, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl4);
		handl4.addBox(48, 4, 15.0F, -11.0F, -1.0F, 4, 8, 4, 0.0F, false);

		handl5 = new HAModelPart(this);
		handl5.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handl5, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl5);
		handl5.addBox(48, 4, 11.0F, -3.0F, -4.0F, 4, 8, 4, 0.0F, false);

		right_hands = new HAModelPart(this);
		right_hands.setRotationPoint(0.0F, 0.0F, 0.0F);

		handr1 = new HAModelPart(this);
		handr1.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(handr1, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr1);
		handr1.addBox(48, 4, -14.0F, 11.0F, -1.0F, 4, 8, 4, 0.0F, true);

		handr2 = new HAModelPart(this);
		handr2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handr2, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr2);
		handr2.addBox(48, 4, -14.0F, -5.0F, -4.0F, 4, 8, 4, 0.0F, true);

		handr3 = new HAModelPart(this);
		handr3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handr3, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr3);
		handr3.addBox(48, 4, -17.0F, 8.0F, 1.0F, 4, 8, 4, 0.0F, true);

		handr4 = new HAModelPart(this);
		handr4.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handr4, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr4);
		handr4.addBox(48, 4, -20.0F, -10.0F, 2.0F, 4, 8, 4, 0.0F, true);

		handr5 = new HAModelPart(this);
		handr5.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(handr5, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr5);
		handr5.addBox(48, 4, -13.0F, 2.0F, 6.0F, 4, 8, 4, 0.0F, true);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addBox(0, 0, -3.5F, -6.0F, -4.0F, 7, 6, 8, 0.0F, false);

		hair1 = new HAModelPart(this);
		hair1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair1, 0.6093F, -0.1487F, 0.0349F);
		head.addChild(hair1);
		hair1.addBox(0, 35, -4.0F, -8.0F, 0.5F, 3, 3, 6, 0.0F, false);

		hair2 = new HAModelPart(this);
		hair2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair2, 0.1267F, 0.0F, 0.0F);
		head.addChild(hair2);
		hair2.addBox(0, 49, -3.9F, -8.3F, -2.5F, 8, 4, 8, 0.0F, false);

		hair3 = new HAModelPart(this);
		hair3.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair3, 0.5729F, 0.0F, 0.0F);
		head.addChild(hair3);
		hair3.addBox(0, 35, -2.0F, -9.0F, 0.0F, 4, 4, 8, 0.0F, false);

		hair4 = new HAModelPart(this);
		hair4.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair4, 0.4363F, 0.2269F, 0.2094F);
		head.addChild(hair4);
		hair4.addBox(0, 49, 0.2F, -8.0F, 0.0F, 3, 4, 7, 0.0F, false);

		hair5 = new HAModelPart(this);
		hair5.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair5, 0.4363F, -0.2269F, -0.2094F);
		head.addChild(hair5);
		hair5.addBox(0, 49, -3.2F, -8.0F, 0.0F, 2, 4, 7, 0.0F, false);

		hair6 = new HAModelPart(this);
		hair6.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(hair6, 0.6109F, 0.1396F, -0.0349F);
		head.addChild(hair6);
		hair6.addBox(0, 35, 1.0F, -8.0F, 0.5F, 3, 3, 6, 0.0F, false);

		hat = new HAModelPart(this);
		hat.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(hat);
		hat.addBox(0, 118, -4.0F, -6.5F, -4.5F, 8, 2, 8, 0.0F, false);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 11.0F, 0.0F);
		setRotation(leftleg, -0.5494F, -0.3992F, -0.4341F);
		leftleg.addBox(21, 100, -2.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(-0.8371F, -0.75F, -1.3178F);
		setRotation(legdownl, 1.1107F, -0.1374F, 0.2004F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -0.5F, 3.5F, -7.0F, 5, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 11.0F, 0.0F);
		setRotation(rightleg, -0.3635F, 0.2504F, 0.2482F);
		rightleg.addBox(21, 100, -3.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, true);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(0.6913F, -0.6595F, 1.0427F);
		setRotation(legdownr, 0.427F, 0.0728F, 0.0614F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -2.5F, 5.5F, -6.0F, 5, 9, 5, 0.0F, true);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 无动画兜底：静态渲染当前姿态（各部件已按官方构造值设好角度）
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		applyStandAnimation(ageTicks, speed, power);
		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/**
	 * 攻击态第一人称专用渲染：只画两只挥拳（left_hands/right_hands），不画本体。
	 * 对齐官方 ModelStarPlatinum.renderFirst（第一人称叠加层仅渲染双手）。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power, float alpha) {
		applyStandAnimation(ageTicks, speed, power);
		// 第一人称攻击态拳头半透明：对齐官方 renderFirst 的 GlStateManager.color(1,1,1,alpha)，
		// 调用方需传入 translucent 层（cutout 层忽略 alpha）。
		left_hands.render(matrices, vertices, light, overlay, 1f, 1f, 1f, alpha);
		right_hands.render(matrices, vertices, light, overlay, 1f, 1f, 1f, alpha);
	}

	/** renderStand / renderHandsStand 共用的替身动画：漂浮、头部正视、挥拳（setRotationAngles+setPunch）。 */
	private void applyStandAnimation(float ageTicks, float speed, float power) {
		// 官方 render()：整体上下漂浮
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.1);
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;

		setRotationAngles(ageTicks, 0f, 0f, power, speed);
		if (power > 0) {
			setPunch(ageTicks, 0f, 0f, 0.3f, speed);
		}
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
		left_hands.render(matrices, vertices, light, overlay, r, g, b, a);
		right_hands.render(matrices, vertices, light, overlay, r, g, b, a);
	}

	/**
	 * 官方 setRotationAngles：十二连爪手部围绕摆动。rotateFloat=ageTicks，
	 * rotateYaw/rotatePitch 替身实体场景传 0。
	 * 频率与 ModelTheWorld 同语义：speed*4/3（STAR_PLATINUM speed=1.5 → 挥拳频率约 2.0 rad/tick），
	 * speed<=0 时按 1.6 兜底。
	 */
	public void setRotationAngles(float rotateFloat, float rotateYaw, float rotatePitch, float power, float speed) {
		float freq = speed > 0 ? speed * 4.0F / 3.0F : 1.6F;
		float f = rotateFloat * freq;
		// 手张开角：攻击 = 伸出呈扇形出击；闲置 = 收拢下垂自然垂放
		float open = power > 0 ? -1.5708F : -0.45F;
		handl1.rotateAngleX = open;
		handl2.rotateAngleX = open;
		handl3.rotateAngleX = open;
		handl4.rotateAngleX = open;
		handl5.rotateAngleX = open;
		handr1.rotateAngleX = open;
		handr2.rotateAngleX = open;
		handr3.rotateAngleX = open;
		handr4.rotateAngleX = open;
		handr5.rotateAngleX = open;

		handl1.rotateAngleY = MathHelper.cos(f) * 1.2F * power;
		handl2.rotateAngleY = MathHelper.cos(f + (float) Math.PI / 3) * 1.4F * power;
		handl3.rotateAngleY = MathHelper.cos(f + (float) (2 * Math.PI / 3)) * 1.6F * power;
		handl4.rotateAngleY = MathHelper.cos(f + (float) (2.5 * Math.PI / 3)) * 1.3F * power;
		handl5.rotateAngleY = MathHelper.cos(f + (float) (3 * Math.PI / 3)) * 1.7F * power;
		handr1.rotateAngleY = MathHelper.cos(f + (float) (3.5 * Math.PI / 3)) * 1.2F * power;
		handr2.rotateAngleY = MathHelper.cos(f + (float) (6 * Math.PI / 3)) * 1.1F * power;
		handr3.rotateAngleY = MathHelper.cos(f + (float) (8 * Math.PI / 3)) * 1.4F * power;
		handr4.rotateAngleY = MathHelper.cos(f + (float) (10 * Math.PI / 3)) * 1.5F * power;
		handr5.rotateAngleY = MathHelper.cos(f + (float) (1.5 * Math.PI / 3)) * 1.6F * power;

		left_hands.rotateAngleX = rotatePitch * 0.017453292F;
		left_hands.rotateAngleY = rotateYaw * 0.017453292F;
		right_hands.rotateAngleX = rotatePitch * 0.017453292F;
		right_hands.rotateAngleY = rotateYaw * 0.017453292F;
	}

	/**
	 * 官方 setPunch：手部随机抖动（"前冲 + 左右错位"的冲拳力度）。
	 * 口径与 ModelTheWorld 一致：官方 数值作用在叠加层小模型里是像素级，
	 * 本工程为独立实体渲染（模型单位为格），统一 ÷16 换算回 1/16 格的亚格抖动。
	 */
	public void setPunch(float rotateFloat, float rotateYaw, float rotatePitch, float power, float speed) {
		float r = (float) Math.random();
		float offysin = (MathHelper.sin(r * speed * rotateFloat) * power - 0.25f) / 16f;
		float offycos = (MathHelper.cos(r * speed * rotateFloat) * power - 0.25f) / 16f;
		handl1.offsetZ = offysin;
		handl2.offsetZ = offycos;
		handl3.offsetZ = offysin;
		handl4.offsetZ = offycos;
		handl5.offsetZ = offysin;
		handr1.offsetZ = offycos;
		handr2.offsetZ = offysin;
		handr3.offsetZ = offycos;
		handr4.offsetZ = offysin;
		handr5.offsetZ = offycos;
		float offxl = (r * MathHelper.sin(r * speed * rotateFloat) * power + 0.2f) / 16f;
		float offxr = (r * MathHelper.sin(r * speed * rotateFloat) * power - 0.2f) / 16f;
		left_hands.offsetX = offxl;
		right_hands.offsetX = offxr;
	}
}
