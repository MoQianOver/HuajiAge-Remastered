package org.huajiager.client.render.entity;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.client.render.HARenderUtil;
import org.huajiager.entity.EntityDiscCommand;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 命令唱片实体渲染器， RenderDiscCommand。面向相机渲染唱片贴图广告牌四边形。
 */
public class RenderDiscCommand extends EntityRenderer<EntityDiscCommand> {

	private static final Identifier TEX = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/item/disc/disc_null.png");

	public RenderDiscCommand(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntityDiscCommand entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		Entity cam = MinecraftClient.getInstance().getCameraEntity();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MinecraftClient.getInstance().gameRenderer.getCamera().getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MinecraftClient.getInstance().gameRenderer.getCamera().getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
		HARenderUtil.renderBillboardQuad(matrices, vcp, TEX, 1f, 1f, light, OverlayTexture.DEFAULT_UV, 1f);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityDiscCommand entity) {
		return TEX;
	}
}
