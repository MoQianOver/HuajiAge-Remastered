package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * STAR PLATINUM 闲置态模型（Blockbench 抱胸盘腿 + 双拳残影造型）。
 *
 * 数值照搬自 Blockbench，
 * 适配 HAModelPart / StandAnimatedModel 渲染体系，与 ModelTheWorldIdle 口径一致。
 *
 * 造型：正立悬浮，双臂抱胸（ idle 手臂收拢、手拳上抬），双腿微收，
 * 通体上下漂浮（cos(0.1*age)），头顶礼帽、披风完整。后附"双拳残影"（
 * extraEffect 渲染 hands：放大 1.2 倍、alpha 随 0.15 频率波动），即白金
 * 时停/蓄力时拳头叠影观感。
 * 贴图配套 entity_star_platinum_idle.png（64x128 Blockbench UV）。
 *
 * 替换此前"白金闲置回落 default 模型（ModelStarPlatinum power=0 收手）仍在
 * 空中摆攻击姿态"的错误实现：闲置态应只蓄能漂浮，不应保持攻击挥拳造型。
 */
public class ModelStarPlatinumIdle extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart body;
	private final HAModelPart Shape11;
	private final HAModelPart bodydown;
	private final HAModelPart cloth1;
	private final HAModelPart cloth2;
	private final HAModelPart crotch;
	private final HAModelPart leftarm;
	private final HAModelPart handl;
	private final HAModelPart armorl;
	private final HAModelPart rightarm;
	private final HAModelPart handr;
	private final HAModelPart armorr;
	private final HAModelPart scarf;
	private final HAModelPart scarf2;
	private final HAModelPart head;
	private final HAModelPart hair1;
	private final HAModelPart hair2;
	private final HAModelPart hair3;
	private final HAModelPart hair4;
	private final HAModelPart hair5;
	private final HAModelPart hair6;
	private final HAModelPart hat;
	private final HAModelPart leftleg;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart legdownr;
	private final HAModelPart hands;

	public ModelStarPlatinumIdle() {
		super(64, 128);

		body = new HAModelPart(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(body, 0.0F, 0.0F, 0.0F);
		body.addBox(16, 16, -4.0F, 0.0F, -2.0F, 8, 7, 4, 0.0F, false);

		Shape11 = new HAModelPart(this);
		Shape11.setRotationPoint(0.0F, 0.0F, -2.3F);
		setRotation(Shape11, -0.0873F, 0.0F, 0.0F);
		body.addChild(Shape11);
		Shape11.addBox(35, 56, -1.5F, 4.0F, 0.0F, 3, 3, 1, 0.0F, false);

		bodydown = new HAModelPart(this);
		bodydown.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(bodydown, -0.0175F, 0.0F, 0.0F);
		body.addChild(bodydown);
		bodydown.addBox(19, 66, -3.5F, 7.0F, -2.0F, 7, 4, 4, 0.0F, false);

		cloth1 = new HAModelPart(this);
		cloth1.setRotationPoint(0.0F, 11.8157F, -4.3943F);
		setRotation(cloth1, -0.0614F, 0.0F, 0.0F);
		body.addChild(cloth1);
		cloth1.addBox(48, 67, -2.0F, -1.2338F, 0.4723F, 4, 8, 1, 0.0F, false);

		cloth2 = new HAModelPart(this);
		cloth2.setRotationPoint(0.0F, 10.9128F, 0.9962F);
		setRotation(cloth2, 0.4606F, 0.0F, 0.0F);
		body.addChild(cloth2);
		cloth2.addBox(48, 56, -2.5F, 0.0F, 1.0F, 5, 9, 1, 0.0F, false);

		crotch = new HAModelPart(this);
		crotch.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(crotch, 0.0349F, 0.0F, 0.0F);
		body.addChild(crotch);
		crotch.addBox(16, 82, -4.0F, 10.0F, -3.5F, 8, 2, 6, 0.0F, false);

		// 左臂：抬起抱拳（ idle：rot 0.2618, -0.0873, -0.4363）
		leftarm = new HAModelPart(this);
		leftarm.setRotationPoint(5.0F, 3.0F, 0.0F);
		setRotation(leftarm, 0.2618F, -0.0873F, -0.4363F);
		body.addChild(leftarm);
		leftarm.addBox(40, 16, -1.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, false);
		leftarm.addBox(46, 103, -1.0F, 2.0F, -1.5F, 3, 3, 4, 0.0F, false);

		handl = new HAModelPart(this);
		handl.setRotationPoint(0.2142F, 7.7366F, 0.4486F);
		setRotation(handl, -1.7453F, -0.2618F, 0.6981F);
		leftarm.addChild(handl);
		handl.addBox(48, 4, -2.0F, -2.0F, -4.4052F, 4, 8, 4, 0.0F, false);

		armorl = new HAModelPart(this);
		armorl.setRotationPoint(2.0F, -2.5F, 0.9F);
		leftarm.addChild(armorl);
		armorl.addBox(0, 74, -2.0F, -0.5F, -3.0F, 4, 1, 6, 0.0F, false);

		// 右臂：抬起抱拳（ idle：rot 0.1745, 0.4363, 0.5236）
		rightarm = new HAModelPart(this);
		rightarm.setRotationPoint(-5.0F, 3.0F, 0.0F);
		setRotation(rightarm, 0.1745F, 0.4363F, 0.5236F);
		body.addChild(rightarm);
		rightarm.addBox(40, 16, -3.0F, -2.0F, -2.0F, 4, 4, 5, 0.0F, true);
		rightarm.addBox(46, 103, -2.5F, 2.0F, -1.5F, 3, 3, 4, 0.0F, true);

		handr = new HAModelPart(this);
		handr.setRotationPoint(-1.3226F, 5.0458F, 0.1748F);
		setRotation(handr, -1.4835F, 0.0873F, -0.5236F);
		rightarm.addChild(handr);
		handr.addBox(48, 4, -2.0F, -3.0864F, -1.6742F, 4, 8, 4, 0.0F, true);

		armorr = new HAModelPart(this);
		armorr.setRotationPoint(5.0F, -2.0F, 1.0F);
		rightarm.addChild(armorr);
		armorr.addBox(0, 74, -9.0F, -1.0F, -3.1F, 4, 1, 6, 0.0F, true);

		scarf = new HAModelPart(this);
		scarf.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(scarf, 0.5817F, 0.0F, 0.0F);
		body.addChild(scarf);
		scarf.addBox(24, 35, -5.0F, -1.0F, -4.0F, 10, 2, 10, 0.0F, false);

		scarf2 = new HAModelPart(this);
		scarf2.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotation(scarf2, 0.2471F, 0.0F, 0.0F);
		body.addChild(scarf2);
		scarf2.addBox(28, 35, -4.5F, -1.0F, -4.5F, 9, 3, 9, 0.0F, false);

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

		// 左腿：微盘曲（ idle：pivot y12 略收）
		leftleg = new HAModelPart(this);
		leftleg.setRotationPoint(2.0F, 12.0F, 1.0F);
		setRotation(leftleg, -0.0258F, -0.1374F, -0.1723F);
		leftleg.addBox(21, 100, -2.0F, 0.0F, -3.5F, 5, 7, 5, 0.0F, false);

		legdownl = new HAModelPart(this);
		legdownl.setRotationPoint(0.4313F, 5.9635F, -0.0644F);
		setRotation(legdownl, 0.5236F, 0.0F, 0.0F);
		leftleg.addChild(legdownl);
		legdownl.addBox(0, 100, -2.5F, -0.5F, -2.5F, 5, 9, 5, 0.0F, false);

		rightleg = new HAModelPart(this);
		rightleg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		setRotation(rightleg, -0.1017F, 0.1631F, 0.1609F);
		rightleg.addBox(21, 100, -3.0F, 0.0F, -2.5F, 5, 7, 5, 0.0F, true);

		legdownr = new HAModelPart(this);
		legdownr.setRotationPoint(1.6154F, 6.7528F, 1.5678F);
		setRotation(legdownr, 0.9506F, 0.1601F, 0.1487F);
		rightleg.addChild(legdownr);
		legdownr.addBox(0, 100, -4.1154F, -1.9128F, -2.5F, 5, 9, 5, 0.0F, true);

		// 双拳残影（ hands，独立 group，仅 extraEffect 渲染）
		hands = new HAModelPart(this);
		hands.setRotationPoint(-7.0F, -4.0F, -8.0F);
		setRotation(hands, -1.5708F, 0.0F, 0.0F);
		hands.addBox(48, 4, -8.3226F, -19.0405F, -1.4994F, 4, 8, 4, 0.0F, true);
		hands.addBox(48, 4, 18.3226F, -12.0405F, -1.4994F, 4, 8, 4, 0.0F, false);
		hands.addBox(48, 4, -13.3226F, -13.0405F, 6.5006F, 4, 8, 4, 0.0F, true);
		hands.addBox(48, 4, 24.3226F, -20.0405F, 4.5006F, 4, 8, 4, 0.0F, false);
		hands.addBox(48, 4, -8.3226F, -19.0405F, 11.5006F, 4, 8, 4, 0.0F, true);
		hands.addBox(48, 4, 18.3226F, -17.0405F, 11.5006F, 4, 8, 4, 0.0F, false);
	}

	private static void setRotation(HAModelPart model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 无动画兜底：静态渲染抱拳盘腿姿态（各部件已按构造值设好角度）
		renderParts(matrices, vertices, light, overlay, r, g, b, a);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power) {
		//  render()：通体上下漂浮（cos(0.1*age)*0.1）
		float off = MathHelper.cos(0.1F * ageTicks) * 0.1F;
		head.offsetY = off;
		body.offsetY = off;
		leftleg.offsetY = off;
		rightleg.offsetY = off;
		hands.offsetY = off;

		// 头部朝向：实体替身朝向已在渲染器矩阵（180-yaw）处理，此处保持正面朝向
		head.rotateAngleX = 0.0F;
		head.rotateAngleY = 0.0F;

		renderParts(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/**
	 * 双拳残影（ extraEffect hands 组）单独绘制：半透明淡入淡出闪烁。
	 * alpha 用 (0.5+0.5*cos) 平滑波动（0→0.45 往返，周期约 2.1s），全程恒大于 0，
	 * 呈现"拳头叠影若隐若现"的淡入淡出效果；区别于 cos 负相位整段跳过渲染
	 * （突然出现-消失）以及固定 0.45 常驻（无闪烁）两种口径。
	 * 必须走 translucent 混合渲染层：cutout 层（renderStand 用的 getEntityCutoutNoCull）
	 * 不开启 alpha 混合，传入的 alpha 会被忽略，残影表现为不透明常驻。
	 */
	public void renderHandsFade(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay,
			float ageTicks, Identifier texture) {
		matrices.push();
		matrices.scale(1.2F, 1.2F, 1.2F);
		float handAlpha = 0.45F * (0.5F + 0.5F * MathHelper.cos(0.15F * ageTicks));
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(texture));
		hands.render(matrices, vc, light, overlay, 1f, 1f, 1f, handAlpha);
		matrices.pop();
	}

	private void renderParts(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		head.render(matrices, vertices, light, overlay, r, g, b, a);
		body.render(matrices, vertices, light, overlay, r, g, b, a);
		leftleg.render(matrices, vertices, light, overlay, r, g, b, a);
		rightleg.render(matrices, vertices, light, overlay, r, g, b, a);
	}
}
