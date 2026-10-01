package org.huajiager.client.render.entity;

import org.huajiager.client.render.model.ModelSheerHeartAttack;
import org.huajiager.entity.EntitySheerHeartAttack;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 心锁炸弹实体渲染器， RenderSheerHeartAttack（RenderLiving）。
 * 目标侧使用 ModelSheerHeartAttack（Blockbench 模型）+ 实体贴图绘制。
 */
public class RenderSheerHeartAttack extends EntityRenderer<EntitySheerHeartAttack> {

	private static final Identifier TEXTURE = Identifier.of("huajiager",
			"textures/entity/entity_sheer_heart_attack.png");

	private final ModelSheerHeartAttack model = new ModelSheerHeartAttack();

	public RenderSheerHeartAttack(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntitySheerHeartAttack entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		// 沿用 RenderStandBase 已验证矩阵：translate 抬升必须放在 scale 之前，
		// 否则 scale(-1,-1,1) 翻转后的模型会被压到脚底以下（"始终在脚下/遁地"的根源）。
		// lift=1.5 与模型最高 pivot(24像素=1.5格) 对齐，使翻转后的模型贴地站立。
		matrices.translate(0.0f, 1.5f, 0.0f);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - entity.getYaw()));
		matrices.scale(-1f, -1f, 1f);
		VertexConsumer vertices = vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
		// 头部朝向简化为 0（RenderLiving 按实体朝向驱动，心锁炸弹无寻头行为可保持默认）
		model.setRotationAngles(0f, 0f);
		// overlay 必须传 OverlayTexture.DEFAULT_UV：传 0 会采样 OverlayTexture 左上角
		// 半透明红色像素（RGBA 255,0,0,178），导致模型始终泛红（"受击闪红"同源纹理）。
		model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vcp, light);
	}

	@Override
	public Identifier getTexture(EntitySheerHeartAttack entity) {
		return TEXTURE;
	}
}
