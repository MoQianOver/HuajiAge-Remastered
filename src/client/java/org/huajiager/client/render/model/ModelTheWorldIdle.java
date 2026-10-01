package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * THE WORLD 闲置态模型（Blockbench 抱胸盘腿造型）。
 *
 * 数值照搬自 Blockbench 模型 ModelTheWorldIdle
 * 适配 HAModelPart / StandAnimatedModel 渲染体系。
 *
 * 造型：正立悬浮，身体侧倾微转，双臂抱胸，双腿盘坐，身后双齿轮缓慢转动。
 * 贴图配套 entity_the_world_idle.png（64x128 Blockbench UV）。
 * 闲置态渲染改用本模型，替代此前"default 模型收手下垂"的错误实现。
 */
public class ModelTheWorldIdle extends HAModelBase implements StandAnimatedModel {

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
	private final HAModelPart leftarm;
	private final HAModelPart handl;
	private final HAModelPart rightarm;
	private final HAModelPart handr;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart gears;
	private final HAModelPart gear1;
	private final HAModelPart gear2;

	public ModelTheWorldIdle() {
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
		setRotation(body, 0.1746F, -0.3491F, -0.3491F);
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

		// 左臂：抬起抱胸（rot 0.6981, 0.5236, -0.5236）
		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(5.0F, 2.0F, 0.0F);
		setRotation(leftarm, 0.6981F, 0.5236F, -0.5236F);
		body.addChild(leftarm);
		leftarm.addBox(40, 16, -1.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, false);
		leftarm.addBox(49, 27, -1.0F, 2.0F, -1.5F, 3, 4, 4, 0.0F, false);

		handl = new HAModelPart(this);
		handl.setRotationPoint(2.0F, 7.0F, 2.0F);
		setRotation(handl, -1.2218F, 0.0F, 0.0F);
		leftarm.addChild(handl);
		handl.addBox(48, 4, -3.2F, -1.0F, -4.0F, 4, 8, 4, 0.0F, false);

		// 右臂：横抱胸前（rot -1.2218, 0.1745, 1.0472）
		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		setRotation(rightarm, -1.2218F, 0.1745F, 1.0472F);
		body.addChild(rightarm);
		rightarm.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, true);
		rightarm.addBox(49, 27, -2.5F, 2.0F, -1.5F, 3, 4, 4, 0.0F, true);

		handr = new HAModelPart(this);
		handr.setRotationPoint(-1.0F, 6.0F, 1.0F);
		setRotation(handr, -0.4364F, -0.2618F, 0.0873F);
		rightarm.addChild(handr);
		handr.addBox(48, 4, -2.0F, 0.0F, -2.0F, 4, 8, 4, 0.0F, true);

		// 双腿盘坐（idle 造型的位置/旋转与 default 不同）
		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(6.0F, 11.0F, 5.0F);
		setRotation(leftleg, -0.6981F, -0.6981F, -0.8203F);
		leftleg.addBox(21, 100, -2.0F, 0.0F, -3.0F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(1.1276F, 3.9224F, 0.6341F);
		setRotation(legdownl, 2.6086F, 0.0F, 0.1745F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -2.2773F, -1.4722F, -3.5434F, 5, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(1.0F, 11.0F, 2.0F);
		setRotation(rightleg, 0.2618F, -0.2618F, -0.576F);
		rightleg.addBox(21, 100, -3.0F, 0.0F, -3.0F, 5, 7, 5, 0.0F, true);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(-0.4521F, 7.6393F, 0.1241F);
		setRotation(legdownr, 0.427F, 0.0872F, 0.0F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -2.5F, -1.5F, -1.5F, 5, 9, 5, 0.0F, false);

		// 身后双齿轮（专用 pivot(0,24,0)，动画阶段再下移放大）
		gears = new HAModelPart(this);
		gears.setRotationPoint(0.0F, 24.0F, 0.0F);

		gear1 = new HAModelPart(this);
		gear1.setRotationPoint(-4.9073F, -21.3328F, 3.0076F);
		setRotation(gear1, 0.0F, 0.0873F, -0.8727F);
		gears.addChild(gear1);
		gear1.addBox(48, 79, -2.0F, -0.5F, -2.0F, 4, 1, 4, 0.0F, false);
		gear1.addBox(48, 88, -1.0F, -0.5F, 2.0F, 2, 1, 1, 0.0F, false);
		gear1.addBox(48, 88, -1.0F, -0.5F, -3.0F, 2, 1, 1, 0.0F, false);
		gear1.addBox(58, 87, -3.0F, -0.5F, -1.0F, 1, 1, 2, 0.0F, false);
		gear1.addBox(58, 87, 2.0F, -0.5F, -1.0F, 1, 1, 2, 0.0F, false);

		gear2 = new HAModelPart(this);
		gear2.setRotationPoint(5.9703F, -25.7181F, 4.6108F);
		setRotation(gear2, 0.0F, 0.0F, 0.8727F);
		gears.addChild(gear2);
		gear2.addBox(48, 79, -2.0F, -0.5F, -2.0F, 4, 1, 4, 0.0F, false);
		gear2.addBox(48, 88, -1.0F, -0.5F, 2.0F, 2, 1, 1, 0.0F, false);
		gear2.addBox(48, 88, -1.0F, -0.5F, -3.0F, 2, 1, 1, 0.0F, false);
		gear2.addBox(58, 87, -3.0F, -0.5F, -1.0F, 1, 1, 2, 0.0F, false);
		gear2.addBox(58, 87, 2.0F, -0.5F, -1.0F, 1, 1, 2, 0.0F, false);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 无动画兜底：静态渲染抱胸盘腿姿态（各部件已按构造值设好角度）
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		// 动画：通体上下漂浮（cos(0.1*age)），齿轮下移放大并缓慢自转
		float off = MathHelper.cos(0.1F * ageTicks);
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;

		// 齿轮：pivot 相对 (0,24) 再下移 20px、后移 8px、放大 2 倍，双齿轮反向自转
		gears.offsetY = 20.0F;
		gears.offsetZ = -8.0F;
		gears.xScale = 2.0F;
		gears.yScale = 2.0F;
		gears.zScale = 2.0F;
		gear1.rotateAngleY = ageTicks / 10.0F;
		gear2.rotateAngleY = ageTicks / 15.0F;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0.0F;
		head.rotateAngleY = 0.0F;

		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
		gears.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
