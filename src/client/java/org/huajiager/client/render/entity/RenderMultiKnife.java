package org.huajiager.client.render.entity;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.client.render.model.ModelMuliKnife;
import org.huajiager.entity.EntityMultiKnife;

import net.minecraft.client.render.LightmapTextureManager;
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
 * 万刀实体渲染器， RenderMultiKnife。按 isLight() 切换明/暗纹理。
 */
public class RenderMultiKnife extends EntityRenderer<EntityMultiKnife> {

	private static final Identifier TEX = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_multi_knife.png");
	private static final Identifier TEX_LIGHT = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_multi_knife_shiny.png");
	private static final ModelMuliKnife MODEL = new ModelMuliKnife();

	public RenderMultiKnife(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntityMultiKnife entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		// 对齐 mrqx0195(1.20.1 移植版 RenderMultiKnife) 变换序列：
		// 渲染位抬升到眼睛高度1.5倍再下移0.3，Y(朝向)→X(90)刀尖朝前→X(-俯仰)，scale(1,-1,-1)，无额外缩放。
		matrices.translate(0, entity.getStandingEyeHeight() * 1.5, 0);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
		// 俯仰用 X(+pitch)（对齐 RenderMultiKnife 字节码 X(90)+X(+pitch)）：
		// 合成 X(90+pitch)，pitch=+90(正下方)时刀尖竖直朝下插地，pitch=-90(正上方)时刀尖竖直朝上，
		// 刀尖始终指向发射/速度方向，即"箭式"插地姿态。此前 X(-pitch) 会让 pitch=+90 时退回 X(0) 保持水平朝前。
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getKnifePitch()));
		matrices.translate(0, -0.3, 0);

		boolean lit = entity.isLight();
		int l = lit ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutout(lit ? TEX_LIGHT : TEX));

		matrices.scale(1f, -1f, -1f);
		// 关键：HAModelPart 底层是标准 1.20.1 ModelPart，addBox 像素坐标渲染时已自动 /16，
		// 刀身 12 单位 = 0.75 格，与 mrqx0195 烘焙模型一致； 0.03 是 「像素×scale」语义，
		// 1.20.1 下再乘 0.03 会让刀身缩到 0.02 格导致肉眼不可见（此前"非常小"的根源）。
		MODEL.render(matrices, vc, l, OverlayTexture.DEFAULT_UV);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityMultiKnife entity) {
		return TEX;
	}
}
