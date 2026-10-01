package org.huajiager.client.render.entity;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.client.render.HARenderUtil;
import org.huajiager.entity.EntityFivePower;

import net.minecraft.client.MinecraftClient;
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
 * 五力替身实体渲染器， RenderFivePower。面向相机渲染广告牌，按 isDe() 切换纹理。
 */
public class RenderFivePower extends EntityRenderer<EntityFivePower> {

	private static final Identifier TEX_0 = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/particle/de_bullet_0.png");
	private static final Identifier TEX_1 = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/particle/de_bullet_1.png");

	public RenderFivePower(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntityFivePower entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		Identifier tex = entity.isDe() ? TEX_1 : TEX_0;
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MinecraftClient.getInstance().gameRenderer.getCamera().getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MinecraftClient.getInstance().gameRenderer.getCamera().getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(tex));
		HARenderUtil.renderBillboardQuad(matrices, vcp, tex, 1f, 1f, light, OverlayTexture.DEFAULT_UV, 1f);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityFivePower entity) {
		return entity.isDe() ? TEX_1 : TEX_0;
	}
}
