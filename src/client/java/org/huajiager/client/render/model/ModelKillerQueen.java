package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Killer Queen 官方模型 1.20.1 移植版（default / 待机态）。
 *
 * 数值照搬官方 Blockbench 导出
 * （），
 * textureWidth/Height=128 与官方 entity_killer_queen_default.png 贴图 UV 完全匹配。
 * 配合 RenderStandBase 的官方矩阵（180-yaw + scale(-1,-1,1)）渲染即为保持一致的
 * 正立悬浮待机造型；动画仅保留官方 render() 的上下漂浮 + 头部正视。
 * 官方 renderFirst 为空：第一人称不渲染本体（与绿法皇一致，避免贴脸遮挡视野）。
 */
public class ModelKillerQueen extends HAModelBase implements StandAnimatedModel {

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
	private final HAModelPart la_up;
	private final HAModelPart la_down;
	private final HAModelPart rightarm;
	private final HAModelPart ra_up;
	private final HAModelPart ra_down;
	private final HAModelPart head;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;

	public ModelKillerQueen() {
		super(128, 128);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, -1.0F, 0.0F);
		setRotation(body, -0.1746F, 0.0F, 0.0F);
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
		cloth1.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(cloth1, -0.1487F, 0.0F, 0.0F);
		cloth.addChild(cloth1);
		cloth1.addBox(0, 4, -1.0F, 11.9723F, -1.6662F, 2, 3, 1, 0.0F, false);

		cloth2 = new HAModelPart(this);
		cloth2.setRotationPoint(0.0F, 9.9128F, 0.9962F);
		setRotation(cloth2, 0.5479F, 0.0F, 0.0F);
		cloth.addChild(cloth2);
		cloth2.addBox(42, 30, -4.5F, 0.0F, 1.0F, 9, 5, 1, 0.0F, false);

		cloth3 = new HAModelPart(this);
		cloth3.setRotationPoint(-3.0F, 10.9128F, 0.9962F);
		setRotation(cloth3, -0.063F, 0.0873F, 1.1345F);
		cloth.addChild(cloth3);
		cloth3.addBox(46, 49, -2.0F, -0.4128F, -4.0F, 1, 5, 6, 0.0F, false);

		cloth4 = new HAModelPart(this);
		cloth4.setRotationPoint(3.0F, 9.9128F, 0.9962F);
		setRotation(cloth4, -0.063F, -0.0873F, -0.7854F);
		cloth.addChild(cloth4);
		cloth4.addBox(32, 49, 0.5F, 0.0F, -4.0F, 1, 5, 6, 0.0F, false);

		cloth5 = new HAModelPart(this);
		cloth5.setRotationPoint(3.0F, 9.9128F, 0.9962F);
		setRotation(cloth5, -0.2375F, -0.0873F, -0.4363F);
		cloth.addChild(cloth5);
		cloth5.addBox(0, 57, -3.5F, 0.0F, -4.0F, 5, 5, 1, 0.0F, false);

		cloth6 = new HAModelPart(this);
		cloth6.setRotationPoint(-3.0F, 9.9128F, 0.9962F);
		setRotation(cloth6, -0.2375F, 0.0873F, 0.6981F);
		cloth.addChild(cloth6);
		cloth6.addBox(40, 49, -1.5F, 0.0F, -4.0F, 5, 5, 1, 0.0F, false);

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

		la_up = new HAModelPart(this);
		la_up.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(la_up, -0.6109F, 0.6109F, 0.2618F);
		leftarm.addChild(la_up);
		la_up.addBox(14, 53, 0.0F, 1.0F, -0.5F, 3, 6, 4, 0.0F, false);

		la_down = new HAModelPart(this);
		la_down.setRotationPoint(0.0F, 7.0F, 1.5F);
		setRotation(la_down, 0.1745F, 0.0873F, 0.6981F);
		la_up.addChild(la_down);
		la_down.addBox(20, 15, 0.0F, -4.0F, -6.5F, 3, 4, 8, 0.0F, false);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-6.0F, 2.0F, 0.5F);
		setRotation(rightarm, -0.6109F, 0.0F, 0.0F);
		body.addChild(rightarm);
		rightarm.addBox(47, 0, -2.0F, -2.0F, -2.5F, 4, 4, 5, 0.0F, false);

		ra_up = new HAModelPart(this);
		ra_up.setRotationPoint(1.0F, 1.0F, -0.5F);
		setRotation(ra_up, -0.2618F, -0.2618F, 0.1745F);
		rightarm.addChild(ra_up);
		ra_up.addBox(52, 9, -2.0F, 1.0F, -1.0F, 3, 6, 4, 0.0F, false);

		ra_down = new HAModelPart(this);
		ra_down.setRotationPoint(1.0F, 5.0F, -1.0F);
		setRotation(ra_down, -0.9599F, -0.0873F, 0.0873F);
		ra_up.addChild(ra_down);
		ra_down.addBox(38, 23, -1.0F, -3.0F, 0.0F, 8, 3, 4, 0.0F, false);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, -1.0F, 0.0F);
		head.addBox(0, 0, -3.5F, -7.0F, -4.0F, 7, 7, 8, 0.0F, false);
		head.addBox(55, 55, 2.5F, -9.0F, -3.0F, 1, 2, 5, 0.0F, false);
		head.addBox(22, 0, -2.5F, -8.0F, -3.5F, 5, 1, 7, 0.0F, false);
		head.addBox(54, 44, -3.5F, -9.0F, -3.0F, 1, 2, 5, 0.0F, false);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 12.0F, -1.0F);
		setRotation(leftleg, -0.2876F, -0.3992F, -0.4341F);
		leftleg.addBox(37, 37, -2.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(1.1629F, 10.25F, 1.1822F);
		setRotation(legdownl, 0.5871F, -0.0501F, 0.0259F);
		leftleg.addChild(legdownl);
		legdownl.addBox(34, 8, -2.6629F, -4.5F, -2.1822F, 4, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 10.0F, 0.0F);
		setRotation(rightleg, -0.3635F, 0.3377F, 0.2482F);
		rightleg.addBox(0, 34, -3.0F, 1.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(0.6913F, 9.3405F, 1.5427F);
		setRotation(legdownr, 0.9506F, 0.2473F, 0.0614F);
		rightleg.addChild(legdownr);
		legdownr.addBox(24, 27, -1.6913F, -4.3405F, -2.5427F, 4, 9, 5, 0.0F, false);
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
		applyStandAnimation(ageTicks);
		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/** 官方 render()：整体上下漂浮 + 头部正视（替身朝向由渲染器矩阵 180-yaw 处理，此处不叠加）。 */
	private void applyStandAnimation(float ageTicks) {
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.1);
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;

		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
