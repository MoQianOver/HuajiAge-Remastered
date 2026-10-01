package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * HIEROPHANT GREEN 闲置态模型（Blockbench 后仰 + 背后翡翠条带造型）。
 *
 * 数值照搬自 Blockbench 导出模型，
 * 适配 HAModelPart / StandAnimatedModel 渲染体系。
 *
 * 造型：身体大幅后仰（body rotX=1.3963≈80°）悬浮，双臂自然垂落、双腿盘坐，
 * 背后伸出 11 节翡翠触手条带（strips），整体随 cos 漂浮、条带做差相起伏。
 * 贴图配套 entity_hierophant_green_idle.png（64x128 Blockbench UV）。
 * 闲置态渲染改用本模型，替代此前"回落 default 攻击模型"的错误实现。
 * 该模型在 extraEffect（stage>0）渲染 strips；本地渲染器无 stage 概念，
 * 直接并入 renderStand 一并渲染，观感与升级后一致。
 */
public class ModelHierophantGreenIdle extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart head;
	private final HAModelPart hat;
	private final HAModelPart hat_part_3;
	private final HAModelPart body;
	private final HAModelPart Shape1;
	private final HAModelPart Shape2;
	private final HAModelPart bodydown;
	private final HAModelPart crotch;
	private final HAModelPart part1;
	private final HAModelPart part1_1;
	private final HAModelPart part1_2;
	private final HAModelPart part1_4;
	private final HAModelPart part2;
	private final HAModelPart part1_3;
	private final HAModelPart part1_5;
	private final HAModelPart part1_6;
	private final HAModelPart part3;
	private final HAModelPart part4;
	private final HAModelPart part5;
	private final HAModelPart part6;
	private final HAModelPart part6_1;
	private final HAModelPart part6_2;
	private final HAModelPart rightarm;
	private final HAModelPart rightarmd;
	private final HAModelPart leftarm;
	private final HAModelPart leftarmd;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart strips;
	private final HAModelPart strip1;
	private final HAModelPart strip2;
	private final HAModelPart strip3;
	private final HAModelPart strip4;
	private final HAModelPart strip5;
	private final HAModelPart strip6;
	private final HAModelPart strip7;
	private final HAModelPart strip8;
	private final HAModelPart strip9;
	private final HAModelPart strip10;
	private final HAModelPart strip11;

	public ModelHierophantGreenIdle() {
		super(64, 128);

		head = new HAModelPart(this);
		head.setRotationPoint(0.0F, 0.0F, -1.0F);
		head.addBox(0, 0, -3.0F, -5.8F, -2.0F, 6, 6, 6, 0.0F, false);

		hat = new HAModelPart(this);
		hat.setRotationPoint(0.0F, 24.0F, 1.0F);
		head.addChild(hat);
		hat.addBox(35, 114, -3.5F, -30.0F, -3.5F, 7, 7, 7, 0.0F, false);
		hat.addBox(28, 0, -4.0F, -28.0F, 0.0F, 1, 2, 2, 0.0F, false);
		hat.addBox(28, 0, 3.0F, -28.0F, 0.0F, 1, 2, 2, 0.0F, true);
		hat.addBox(28, 5, -1.5F, -30.5F, -2.5F, 3, 1, 3, 0.0F, false);

		hat_part_3 = new HAModelPart(this);
		hat_part_3.setRotationPoint(2.0F, -27.0F, -5.0F);
		setRotation(hat_part_3, -0.1745F, 0.0F, 0.0F);
		hat.addChild(hat_part_3);
		hat_part_3.addBox(54, 107, -3.5F, 0.5F, 1.5F, 3, 3, 2, 0.0F, false);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(body, 1.3963F, 0.0F, 0.2618F);
		body.addBox(16, 16, -4.0F, 0.0F, -2.0F, 8, 6, 4, 0.0F, false);

		Shape1 = new HAModelPart(this);
		Shape1.setRotationPoint(0.0F, 0.0F, -2.3F);
		body.addChild(Shape1);
		Shape1.addBox(35, 56, -1.5F, 4.0F, 0.0F, 3, 3, 1, 0.0F, false);

		Shape2 = new HAModelPart(this);
		Shape2.setRotationPoint(0.1307F, -2.2291F, -3.4244F);
		setRotation(Shape2, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape2);
		Shape2.addBox(54, 30, -1.5F, 4.0F, 0.0F, 3, 2, 2, 0.0F, false);

		bodydown = new HAModelPart(this);
		bodydown.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(bodydown, -0.0175F, 0.0F, 0.0F);
		body.addChild(bodydown);
		bodydown.addBox(19, 66, -3.5F, 6.0246F, -1.6873F, 7, 5, 4, 0.0F, false);

		crotch = new HAModelPart(this);
		crotch.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(crotch, 0.0349F, 0.0F, 0.0F);
		body.addChild(crotch);
		crotch.addBox(16, 82, -4.0F, 7.5F, -2.8764F, 8, 2, 6, 0.0F, false);

		part1 = new HAModelPart(this);
		part1.setRotationPoint(0.0F, -0.5208F, -2.9544F);
		setRotation(part1, 0.0F, 0.0F, -0.7854F);
		body.addChild(part1);

		part1_1 = new HAModelPart(this);
		part1_1.setRotationPoint(-3.9035F, -2.0107F, 2.8264F);
		setRotation(part1_1, 0.0F, 0.0F, -0.3491F);
		part1.addChild(part1_1);
		part1_1.addBox(48, 54, -0.5F, -3.0F, -3.0F, 1, 6, 6, 0.0F, false);

		part1_2 = new HAModelPart(this);
		part1_2.setRotationPoint(-5.2936F, -2.0295F, 2.5669F);
		setRotation(part1_2, 0.0F, 0.0F, -0.6109F);
		part1.addChild(part1_2);
		part1_2.addBox(48, 54, -0.5F, -3.0F, -3.0F, 1, 7, 6, 0.0F, false);

		part1_4 = new HAModelPart(this);
		part1_4.setRotationPoint(-3.2071F, -2.7071F, 3.0F);
		setRotation(part1_4, 0.0F, 0.0F, -0.2618F);
		part1.addChild(part1_4);
		part1_4.addBox(48, 69, -2.2784F, -2.1619F, -2.5456F, 1, 3, 5, 0.0F, false);

		part2 = new HAModelPart(this);
		part2.setRotationPoint(0.0F, -0.5208F, -2.9544F);
		setRotation(part2, 0.0F, 0.0F, 0.7854F);
		body.addChild(part2);

		part1_3 = new HAModelPart(this);
		part1_3.setRotationPoint(3.9035F, -2.0107F, 2.8264F);
		setRotation(part1_3, 0.0F, 0.0F, 0.3491F);
		part2.addChild(part1_3);
		part1_3.addBox(48, 54, -0.5F, -3.0F, -3.0F, 1, 6, 6, 0.0F, false);

		part1_5 = new HAModelPart(this);
		part1_5.setRotationPoint(5.2936F, -2.0295F, 2.5669F);
		setRotation(part1_5, 0.0F, 0.0F, 0.6109F);
		part2.addChild(part1_5);
		part1_5.addBox(48, 54, -0.5F, -3.0F, -3.0F, 1, 7, 6, 0.0F, false);

		part1_6 = new HAModelPart(this);
		part1_6.setRotationPoint(3.2071F, -2.7071F, 3.0F);
		setRotation(part1_6, 0.0F, 0.0F, 0.2618F);
		part2.addChild(part1_6);
		part1_6.addBox(48, 69, 1.2784F, -2.1619F, -2.5456F, 1, 3, 5, 0.0F, false);

		part3 = new HAModelPart(this);
		part3.setRotationPoint(2.0872F, 4.8422F, -1.8075F);
		body.addChild(part3);
		part3.addBox(35, 62, -3.5F, -2.0F, 4.0F, 3, 2, 1, 0.0F, false);

		part4 = new HAModelPart(this);
		part4.setRotationPoint(-1.9052F, 4.7968F, 2.3473F);
		setRotation(part4, 0.0F, 0.0F, -0.5236F);
		body.addChild(part4);
		part4.addBox(48, 79, -2.5F, -1.0F, -3.5473F, 4, 2, 4, 0.0F, false);

		part5 = new HAModelPart(this);
		part5.setRotationPoint(1.9052F, 4.7968F, 2.3473F);
		setRotation(part5, 0.0F, 0.0F, 0.5236F);
		body.addChild(part5);
		part5.addBox(48, 79, -1.5F, -1.0F, -3.5473F, 4, 2, 4, 0.0F, true);

		part6 = new HAModelPart(this);
		part6.setRotationPoint(0.0F, -1.9696F, 0.3472F);
		body.addChild(part6);
		part6.addBox(0, 64, -2.0F, 2.9696F, 1.6528F, 4, 1, 1, 0.0F, false);

		part6_1 = new HAModelPart(this);
		part6_1.setRotationPoint(-4.1129F, 4.4536F, -3.5367F);
		setRotation(part6_1, 0.0F, 0.0F, -0.7854F);
		part6.addChild(part6_1);
		part6_1.addBox(0, 74, 2.0F, -2.0F, 0.0F, 1, 3, 6, 0.0F, true);

		part6_2 = new HAModelPart(this);
		part6_2.setRotationPoint(4.1129F, 4.4536F, -3.5367F);
		setRotation(part6_2, 0.0F, 0.0F, 0.7854F);
		part6.addChild(part6_2);
		part6_2.addBox(0, 74, -3.0F, -2.0F, 0.0F, 1, 3, 6, 0.0F, true);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(4.0667F, 4.2177F, 1.4699F);
		setRotation(rightarm, 0.0F, -0.7854F, -1.6581F);
		body.addChild(rightarm);
		rightarm.addBox(40, 16, -2.9332F, -0.5783F, -1.8622F, 4, 6, 4, 0.0F, false);

		rightarmd = new HAModelPart(this);
		rightarmd.setRotationPoint(-0.0667F, 5.0783F, -0.1378F);
		setRotation(rightarmd, 0.0F, -0.4363F, 0.2618F);
		rightarm.addChild(rightarmd);
		rightarmd.addBox(48, 4, -2.5F, 0.0F, -1.5F, 4, 7, 4, 0.0F, false);

		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(-4.0667F, 4.2177F, 1.4699F);
		setRotation(leftarm, 0.0F, 1.3963F, 1.6581F);
		body.addChild(leftarm);
		leftarm.addBox(40, 16, -1.0668F, -0.5783F, -1.8622F, 4, 6, 4, 0.0F, true);

		leftarmd = new HAModelPart(this);
		leftarmd.setRotationPoint(0.0667F, 5.0783F, -0.1378F);
		setRotation(leftarmd, 0.0F, 0.6981F, -0.3491F);
		leftarm.addChild(leftarmd);
		leftarmd.addBox(48, 4, -1.5F, 0.0F, -1.5F, 4, 7, 4, 0.0F, true);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(-2.0F, 10.0F, 1.0F);
		setRotation(leftleg, 0.0F, 0.0F, -0.0873F);
		body.addChild(leftleg);
		leftleg.addBox(21, 100, -3.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, true);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(0.0F, 7.0F, -0.5F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -2.0F, -0.5F, -2.5F, 4, 9, 5, 0.0F, true);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(2.0F, 10.0F, 1.0F);
		setRotation(rightleg, -1.309F, 0.2618F, 0.0F);
		body.addChild(rightleg);
		rightleg.addBox(21, 100, -2.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(0.0F, 7.0F, -0.5F);
		setRotation(legdownr, 2.1817F, 0.0F, 0.0F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -2.2F, -0.5F, -2.5F, 4, 9, 5, 0.0F, false);

		// 背后翡翠触手条带
		strips = new HAModelPart(this);
		strips.setRotationPoint(-2.0F, 5.0F, 25.0F);
		setRotation(strips, 0.0873F, 0.0F, 0.0F);

		strip1 = new HAModelPart(this);
		strip1.setRotationPoint(2.0F, 1.0F, 1.0F);
		strips.addChild(strip1);
		strip1.addBox(44, 36, -3.0F, -3.0F, -1.0F, 3, 3, 7, 0.0F, false);

		strip2 = new HAModelPart(this);
		strip2.setRotationPoint(-1.0F, -2.0F, 7.0F);
		setRotation(strip2, 0.3491F, 0.5236F, -0.1745F);
		strip1.addChild(strip2);
		strip2.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip3 = new HAModelPart(this);
		strip3.setRotationPoint(1.0F, 0.0F, 11.0F);
		setRotation(strip3, 0.3491F, 0.8727F, 0.6109F);
		strip2.addChild(strip3);
		strip3.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip4 = new HAModelPart(this);
		strip4.setRotationPoint(1.3493F, -0.2779F, 10.1046F);
		setRotation(strip4, 0.3491F, 1.4835F, 0.6981F);
		strip3.addChild(strip4);
		strip4.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip5 = new HAModelPart(this);
		strip5.setRotationPoint(1.1632F, 1.1236F, 11.0628F);
		setRotation(strip5, 0.3491F, 1.4835F, 0.6981F);
		strip4.addChild(strip5);
		strip5.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip6 = new HAModelPart(this);
		strip6.setRotationPoint(1.8064F, 0.502F, 11.423F);
		setRotation(strip6, 0.3491F, 1.4835F, 0.6981F);
		strip5.addChild(strip6);
		strip6.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip7 = new HAModelPart(this);
		strip7.setRotationPoint(-0.3493F, -0.2217F, 10.7624F);
		setRotation(strip7, 0.3491F, -1.309F, 0.6981F);
		strip6.addChild(strip7);
		strip7.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip8 = new HAModelPart(this);
		strip8.setRotationPoint(-0.2159F, 0.0822F, 11.4868F);
		setRotation(strip8, -0.4363F, -0.6109F, 0.6981F);
		strip7.addChild(strip8);
		strip8.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip9 = new HAModelPart(this);
		strip9.setRotationPoint(-0.5709F, 0.8538F, 10.9519F);
		setRotation(strip9, -0.4363F, -0.6109F, 0.6981F);
		strip8.addChild(strip9);
		strip9.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip10 = new HAModelPart(this);
		strip10.setRotationPoint(0.2116F, -0.0739F, 11.2227F);
		setRotation(strip10, -0.6981F, -0.7854F, -0.3491F);
		strip9.addChild(strip10);
		strip10.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);

		strip11 = new HAModelPart(this);
		strip11.setRotationPoint(-0.2968F, 1.3778F, 10.9551F);
		setRotation(strip11, -0.6981F, -1.7453F, -2.0944F);
		strip10.addChild(strip11);
		strip11.addBox(2, 48, -1.0F, -0.5F, -1.0F, 2, 2, 12, 0.0F, false);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 无动画兜底：静态渲染后仰盘坐 + 条带造型（各部件已按构造值设好角度）
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		applyStandAnimation(ageTicks);
		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/** renderStand 共用的闲置动画：整体漂浮 + 条带差相起伏（照搬 render()）。 */
	private void applyStandAnimation(float ageTicks) {
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.1);
		head.offsetY = off;
		body.offsetY = off;
		strips.offsetY = off;
		strip2.offsetY = off * 0.5f;
		strip4.offsetY = -off * 0.5f;
		strip6.offsetY = off * 0.5f;
		strip8.offsetY = -off * 0.5f;
		strip10.offsetY = off * 0.5f;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		strips.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
