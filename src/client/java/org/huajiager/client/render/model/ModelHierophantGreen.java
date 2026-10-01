package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * HIEROPHANT GREEN 模型。
 *
 * 数值照搬自 Techne 导出模型，
 * 配合 RenderStandBase 的矩阵（180-yaw + scale(-1,-1,1)）渲染即为保持一致的正立悬浮造型
 * （头戴帽冠、胸口齿轮盘、背后翡翠触手 extra 旋转）。
 * 动画与 render() 一致：整体上下漂浮 + extra.rotateAngleX 随 age 持续旋转。 *  setRotationAngles / setPunch / renderFirst 均为空实现——绿法皇没有挥拳动画，
 * 攻击态第一人称也不渲染本体（renderHandsStand 为空）。
 */
public class ModelHierophantGreen extends HAModelBase implements StandAnimatedModel {

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
	private final HAModelPart leftarm;
	private final HAModelPart leftarmd;
	private final HAModelPart rightarm;
	private final HAModelPart rightarmd;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart extra;

	public ModelHierophantGreen() {
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
		setRotation(body, 0.1745F, 0.1745F, 0.0F);
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

		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(0.0F, 6.0F, 0.0F);
		setRotation(leftarm, -0.0873F, 0.0F, -0.9599F);
		leftarm.addBox(40, 16, 3.0F, 1.0872F, -1.9962F, 4, 6, 4, 0.0F, false);

		leftarmd = new HAModelPart(this);
		leftarmd.setRotationPoint(2.6753F, 5.2814F, -3.4392F);
		setRotation(leftarmd, 2.0071F, 3.0543F, -1.6581F);
		leftarm.addChild(leftarmd);
		leftarmd.addBox(48, 4, -2.0F, -3.5F, -2.0F, 4, 7, 4, 0.0F, false);

		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(0.0F, 4.0F, 0.0F);
		setRotation(rightarm, -0.7854F, 0.0F, 1.309F);
		rightarm.addBox(40, 16, -4.1335F, 1.8434F, 1.2756F, 4, 6, 4, 0.0F, true);

		rightarmd = new HAModelPart(this);
		rightarmd.setRotationPoint(-1.6845F, 7.7366F, 2.1856F);
		setRotation(rightarmd, -2.0944F, 0.2618F, -0.2618F);
		rightarm.addChild(rightarmd);
		rightarmd.addBox(48, 4, -2.3983F, -2.8966F, -1.1802F, 4, 7, 4, 0.0F, true);

		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 10.0F, 0.0F);
		setRotation(leftleg, -0.5494F, -0.3992F, -0.4341F);
		leftleg.addBox(21, 100, -2.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(1.8266F, 3.8994F, 3.2559F);
		setRotation(legdownl, 1.8088F, -0.1374F, -0.4105F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -4.5885F, -4.6434F, -2.9332F, 4, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 10.0F, 2.0F);
		setRotation(rightleg, -0.2762F, 0.2504F, 0.1609F);
		rightleg.addBox(21, 100, -3.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, true);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(-0.9457F, 10.0163F, 1.7692F);
		setRotation(legdownr, 0.6015F, 0.0728F, 0.0614F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -1.5F, -4.5F, -2.5F, 4, 9, 5, 0.0F, true);

		extra = new HAModelPart(this);
		extra.setRotationPoint(-0.6176F, 4.3949F, -4.7861F);
		setRotation(extra, 0.4363F, 0.0F, 0.349F);
		extra.addBox(0, 36, -5.2887F, -1.5893F, -1.4862F, 10, 3, 3, 0.0F, false);
		extra.addBox(0, 44, -4.7113F, -0.4107F, -0.5139F, 10, 1, 1, 0.0F, false);
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
	 * 攻击态第一人称专用渲染： ModelHierophantGreen.renderFirst 为空实现——
	 * 绿法皇第一人称不渲染本体（只有弹幕特效），此处保持空，避免本体遮挡视野。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		//  renderFirst 为空：第一人称不渲染任何部件
	}

	/** renderStand 共用的替身动画：漂浮 + extra（背后翡翠盘）持续旋转。 */
	private void applyStandAnimation(float ageTicks, float speed, float power) {
		//  render()：整体上下漂浮
		float off = (float) (MathHelper.cos((float) (0.1 * ageTicks)) * 0.1);
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;
		leftarm.offsetY = off;
		rightarm.offsetY = off;
		extra.offsetY = off;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0f;
		head.rotateAngleY = 0f;

		//  render()：extra.rotateAngleX = ageInTicks*2 —— 背后翡翠齿轮盘持续旋转
		extra.rotateAngleX = ageTicks * 2;
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		leftarm.render(matrices, vertices, light, overlay, r, g, b, a);
		rightarm.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
		extra.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
