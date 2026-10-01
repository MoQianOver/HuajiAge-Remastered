package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * THE WORLD 官方模型 1.20.1 移植版。
 *
 * 数值照搬官方 （Techne 导出 ），
 * 配合 RenderStandBase 的官方矩阵（180-yaw + scale(-1,-1,1)）渲染即为保持一致的正立悬浮盘腿造型。
 * 动画（漂浮 offsetY + 手部十二连 cos 摆动）一并保留；闲置态收手静止、
 * 攻击态张开连打，由 power（renderStand 传入）驱动。
 */
public class ModelTheWorld extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart head;
	private final HAModelPart Shape1;
	private final HAModelPart Shape2;
	private final HAModelPart glass;
	private final HAModelPart Shape3;
	private final HAModelPart Shape4;
	private final HAModelPart Shape5;
	private final HAModelPart Shape6;
	private final HAModelPart Shape7;
	private final HAModelPart body;
	private final HAModelPart bodydown;
	private final HAModelPart Shape8;
	private final HAModelPart Shape9;
	private final HAModelPart Shape10;
	private final HAModelPart Shape11;
	private final HAModelPart Shape12;
	private final HAModelPart Shape13;
	private final HAModelPart Shape14;
	private final HAModelPart back1;
	private final HAModelPart back2;
	private final HAModelPart rightarm;
	private final HAModelPart leftarm;
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

	public ModelTheWorld() {
		super(64, 128);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addBox(0, 0, -3.5F, -6.0F, -4.0F, 7, 6, 8, 0.0F, false);

		Shape1 = new HAModelPart(this);
		Shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape1, 0.4014F, 0.0F, 0.0F);
		head.addChild(Shape1);
		Shape1.addBox(0, 35, -4.5F, -8.0F, -1.0F, 9, 3, 9, 0.0F, false);

		Shape2 = new HAModelPart(this);
		Shape2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape2, 0.0524F, 0.0F, 0.0F);
		head.addChild(Shape2);
		Shape2.addBox(0, 49, -4.5F, -7.2F, -3.0F, 9, 4, 8, 0.0F, false);

		glass = new HAModelPart(this);
		glass.setRotationPoint(0.0F, 0.0F, -0.5F);
		head.addChild(glass);

		Shape3 = new HAModelPart(this);
		Shape3.setRotationPoint(0.0F, 0.0F, 1.0F);
		setRotation(Shape3, 0.0F, 1.0996F, 0.0F);
		glass.addChild(Shape3);
		Shape3.addBox(0, 63, 2.8F, -7.0F, -3.0F, 3, 4, 6, 0.0F, false);

		Shape4 = new HAModelPart(this);
		Shape4.setRotationPoint(0.0F, 0.0F, 1.0F);
		setRotation(Shape4, 0.0F, -1.0996F, 0.0F);
		glass.addChild(Shape4);
		Shape4.addBox(0, 63, -5.8F, -7.0F, -3.0F, 3, 4, 6, 0.0F, true);

		Shape5 = new HAModelPart(this);
		Shape5.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape5, -0.1505F, 0.0F, 0.0F);
		head.addChild(Shape5);
		Shape5.addBox(0, 75, -4.5F, -4.0F, 1.0F, 9, 2, 4, 0.0F, false);

		Shape6 = new HAModelPart(this);
		Shape6.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape6, 0.0F, 0.0F, 0.7854F);
		head.addChild(Shape6);
		Shape6.addBox(0, 30, -1.0F, 0.0F, -5.0F, 2, 1, 1, 0.0F, false);

		Shape7 = new HAModelPart(this);
		Shape7.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape7, 0.0F, 0.0F, 0.7854F);
		head.addChild(Shape7);
		Shape7.addBox(0, 30, 0.0F, -1.0F, -5.0F, 1, 1, 1, 0.0F, false);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(body, 0.0873F, 0.0F, 0.0F);
		body.addBox(16, 16, -4.0F, 0.0F, -2.0F, 8, 7, 4, 0.0F, false);

		bodydown = new HAModelPart(this);
		bodydown.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(bodydown, 0.0873F, 0.0F, 0.0F);
		body.addChild(bodydown);
		bodydown.addBox(19, 66, -3.5F, 7.0F, -2.0F, 7, 4, 4, 0.0F, false);

		Shape8 = new HAModelPart(this);
		Shape8.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape8, 0.0873F, 0.0F, 0.0F);
		body.addChild(Shape8);
		Shape8.addBox(37, 30, 1.0F, -1.0F, -3.0F, 2, 11, 6, 0.0F, false);

		Shape9 = new HAModelPart(this);
		Shape9.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape9, 0.0873F, 0.0F, 0.0F);
		body.addChild(Shape9);
		Shape9.addBox(37, 30, -3.0F, -1.0F, -3.0F, 2, 11, 6, 0.0F, true);

		Shape10 = new HAModelPart(this);
		Shape10.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape10, 0.0873F, 0.0F, 0.0F);
		body.addChild(Shape10);
		Shape10.addBox(35, 49, -3.5F, 0.2F, -2.5F, 7, 4, 1, 0.0F, false);

		Shape11 = new HAModelPart(this);
		Shape11.setRotationPoint(0.0F, 0.0F, -2.3F);
		setRotation(Shape11, 0.0873F, 0.0F, 0.0F);
		body.addChild(Shape11);
		Shape11.addBox(35, 56, -1.5F, 4.2F, 0.0F, 3, 3, 1, 0.0F, false);

		Shape12 = new HAModelPart(this);
		Shape12.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape12, 0.0873F, 0.0F, 0.0F);
		body.addChild(Shape12);
		Shape12.addBox(16, 82, -4.0F, 10.0F, -3.5F, 8, 2, 7, 0.0F, false);

		Shape13 = new HAModelPart(this);
		Shape13.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape13, 0.0873F, 0.0F, 0.7854F);
		body.addChild(Shape13);
		Shape13.addBox(0, 30, 7.0F, 8.0F, -3.5F, 2, 1, 1, 0.0F, false);

		Shape14 = new HAModelPart(this);
		Shape14.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(Shape14, 0.0873F, 0.0F, 0.7854F);
		body.addChild(Shape14);
		Shape14.addBox(0, 30, 8.0F, 7.0F, -3.5F, 1, 1, 1, 0.0F, false);

		back1 = new HAModelPart(this);
		back1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(back1, 0.0873F, 0.0F, 0.0F);
		body.addChild(back1);
		back1.addBox(0, 83, 0.5F, 0.0F, 3.0F, 2, 7, 2, 0.0F, false);

		back2 = new HAModelPart(this);
		back2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(back2, 0.0873F, 0.0F, 0.0F);
		body.addChild(back2);
		back2.addBox(0, 83, -2.5F, 0.0F, 3.0F, 2, 7, 2, 0.0F, true);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		setRotation(rightarm, 0.5236F, 0.0F, 0.0F);
		body.addChild(rightarm);
		rightarm.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, true);

		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(leftarm, 0.5236F, 0.0F, 0.0F);
		body.addChild(leftarm);
		leftarm.addBox(40, 16, -1.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, false);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 11.0F, 0.0F);
		setRotation(leftleg, -0.6981F, -0.4363F, -0.4712F);
		leftleg.addBox(21, 100, -2.0F, 0.0F, -3.0F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(1.1276F, 4.9224F, 4.6341F);
		setRotation(legdownl, 1.2124F, 0.0F, 0.2618F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -2.2773F, -3.4722F, -4.5434F, 5, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 11.0F, 0.0F);
		setRotation(rightleg, -0.6981F, 0.4363F, 0.4712F);
		rightleg.addBox(21, 100, -3.0F, 0.0F, -3.0F, 5, 7, 5, 0.0F, true);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(-0.0181F, 7.2866F, 3.8689F);
		setRotation(legdownr, 1.2124F, -0.0873F, -0.3491F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -2.5F, -4.5F, -2.5F, 5, 9, 5, 0.0F, false);

		left_hands = new HAModelPart(this);
		left_hands.setRotationPoint(0.0F, 5.0F, 2.0F);

		handl1 = new HAModelPart(this);
		handl1.setRotationPoint(8.0F, 2.0F, 0.0F);
		setRotation(handl1, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl1);
		handl1.addBox(48, 4, 4.0F, 1.0F, -11.0F, 4, 8, 4, 0.0F, false);

		handl2 = new HAModelPart(this);
		handl2.setRotationPoint(3.0F, 0.0F, 0.0F);
		setRotation(handl2, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl2);
		handl2.addBox(48, 4, 14.0F, 8.0F, -6.0F, 4, 8, 4, 0.0F, false);

		handl3 = new HAModelPart(this);
		handl3.setRotationPoint(3.0F, 0.0F, 0.0F);
		setRotation(handl3, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl3);
		handl3.addBox(48, 4, 8.0F, -6.0F, -5.0F, 4, 8, 4, 0.0F, false);

		handl4 = new HAModelPart(this);
		handl4.setRotationPoint(3.0F, 0.0F, 0.0F);
		setRotation(handl4, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl4);
		handl4.addBox(48, 4, 10.0F, 5.0F, 1.0F, 4, 8, 4, 0.0F, false);

		handl5 = new HAModelPart(this);
		handl5.setRotationPoint(3.0F, 0.0F, 0.0F);
		setRotation(handl5, -1.5708F, 0.0F, 0.0F);
		left_hands.addChild(handl5);
		handl5.addBox(48, 4, 12.0F, 1.0F, -2.0F, 4, 8, 4, 0.0F, false);

		right_hands = new HAModelPart(this);
		right_hands.setRotationPoint(0.0F, 5.0F, 2.0F);

		handr1 = new HAModelPart(this);
		handr1.setRotationPoint(3.0F, 2.0F, 0.0F);
		setRotation(handr1, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr1);
		handr1.addBox(48, 4, -19.0F, 3.0F, -1.0F, 4, 8, 4, 0.0F, true);

		handr2 = new HAModelPart(this);
		handr2.setRotationPoint(-2.0F, 0.0F, 0.0F);
		setRotation(handr2, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr2);
		handr2.addBox(48, 4, -13.0F, -1.0F, -9.0F, 4, 8, 4, 0.0F, true);

		handr3 = new HAModelPart(this);
		handr3.setRotationPoint(-2.0F, 0.0F, 0.0F);
		setRotation(handr3, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr3);
		handr3.addBox(48, 4, -13.0F, -6.0F, -3.0F, 4, 8, 4, 0.0F, true);

		handr4 = new HAModelPart(this);
		handr4.setRotationPoint(-2.0F, 0.0F, 0.0F);
		setRotation(handr4, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr4);
		handr4.addBox(48, 4, -19.0F, 8.0F, -3.0F, 4, 8, 4, 0.0F, true);

		handr5 = new HAModelPart(this);
		handr5.setRotationPoint(-2.0F, 0.0F, 0.0F);
		setRotation(handr5, -1.5708F, 0.0F, 0.0F);
		right_hands.addChild(handr5);
		handr5.addBox(48, 4, -17.0F, 3.0F, -6.0F, 4, 8, 4, 0.0F, true);
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
	 * 对齐官方 ModelTheWorld.renderFirst（第一人称叠加层仅渲染双手、隐藏 body/head/leg
	 * 等本体部件）——本工程为独立实体渲染，攻击态替身位于玩家前方，第一人称靠本方法
	 * 只呈现前方舞动的拳头，替身本体从视野中消失。
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

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向，不再叠加
		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;

		setRotationAngles(ageTicks, 0f, 0f, power, speed);
		// 攻击态补齐官方 setPunch：拳头 Z 向前冲 + X 左右错位的随机抖动，
		// 与 setRotationAngles 的 Y 向十二连摆叠加即为"欧拉欧拉"挥拳效果。
		// 0.3 系数对齐 Astral Regenesis 移植版（hand*.z 抖动 ±(0.05~0.55)px、错位 0.2px）。
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
	 * rotateYaw/rotatePitch 官方由 headPitch/netHeadYaw 换算，替身实体场景传 0。
	 *
	 * 频率说明：官方 LayerStand 以 speed = stand.getSpeed()*4/3 驱动拳动
	 * （THE_WORLD speed=1.2 → 1.6），本地替身副本早前 speed=0 导致 cos 恒常数
	 * 不会有任何挥拳动画，曾改用固定 0.35 兜底但节奏偏慢；现改回官方语义
	 * 「speed*4/3」，THE_WORLD 攻击态挥拳频率 ≈1.6 rad/tick（周期约 0.2s），
	 * 速度明显加快、更接近"欧拉欧拉"连打的观感。speed<=0 时按 1.6 兜底。
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
	 *
	 * 单位修正（关键）：官方 该位移作用在 LayerStand 叠加层的小模型上下文里，
	 * 数值是「像素」级（hand*.offsetZ 抖动 ±(0.05~0.55)px、left/right_hands 错位 0.2px，
	 * 1px = 1/16 格）。本工程为独立实体渲染，模型单位为格，若直接套用会放大 16 倍——
	 * 拳头每帧随机前后窜动 ±0.5 格、贴上相机近裁剪面，第一人称贴脸时被冲出视野/裁剪，
	 * 走路、飞行镜头摆动叠加后即成"拳头时不时消失"。此处统一 ÷16 换算回 1/16 格的
	 * 亚格抖动，保留官方冲拳力度与随机错位观感、消除凭空消失。
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
