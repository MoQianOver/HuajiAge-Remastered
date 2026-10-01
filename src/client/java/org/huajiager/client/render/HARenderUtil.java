package org.huajiager.client.render;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * GlStateManager/Tessellator 兼容工具（面向当前渲染接口的适配层）。
 * 提供：广告牌贴图四边形、物品渲染、占位立方体（缺物品时兜底）。
 */
public final class HARenderUtil {

	private HARenderUtil() {
	}

	/** 在实体局部空间渲染一个面向相机的贴图广告牌四边形（GL_QUADS POSITION_TEX 方式）。 */
	public static void renderBillboardQuad(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture,
			float width, float height, int light, int overlay, float alpha) {
		renderBillboardQuad(matrices, vcp, texture, width, height, light, overlay, 1f, 1f, 1f, alpha);
	}

	public static void renderBillboardQuad(MatrixStack matrices, VertexConsumerProvider vcp, Identifier texture,
			float width, float height, int light, int overlay, float r, float g, float b, float a) {
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(texture));
		MatrixStack.Entry e = matrices.peek();
		quad(e.getPositionMatrix(), e.getNormalMatrix(), vc, -width / 2f, -height / 2f, width / 2f, height / 2f, 0f,
				0f, 0f, 1f, 1f, light, overlay, r, g, b, a);
	}

	public static void quad(Matrix4f pos, Matrix3f normalMat, VertexConsumer vc, float x0, float y0, float x1, float y1,
			float z, float u0, float v0, float u1, float v1, int light, int overlay, float r, float g, float b, float a) {
		// 顶点顺序与 RenderFivePower/RenderDiscCommand 的 GL_QUADS 一致：
		// 上(y1)配 v0(纹理顶部)、下(y0)配 v1(纹理底部)，避免贴图上下颠倒。
		vertex(pos, normalMat, vc, x0, y1, z, u0, v0, light, overlay, r, g, b, a);
		vertex(pos, normalMat, vc, x0, y0, z, u0, v1, light, overlay, r, g, b, a);
		vertex(pos, normalMat, vc, x1, y0, z, u1, v1, light, overlay, r, g, b, a);
		vertex(pos, normalMat, vc, x1, y1, z, u1, v0, light, overlay, r, g, b, a);
	}

	private static void vertex(Matrix4f pos, Matrix3f normalMat, VertexConsumer vc, float x, float y, float z,
			float u, float v, int light, int overlay, float r, float g, float b, float a) {
		vc.vertex(pos, x, y, z).color(r, g, b, a).texture(u, v).overlay(overlay).light(light)
				.normal(normalMat, 0f, 0f, 1f).next();
	}

	/** 用物品渲染管线在实体局部空间渲染一个物品。 */
	public static void renderItem(MatrixStack matrices, VertexConsumerProvider vcp, ItemStack stack, int light,
			int overlay) {
		renderItem(matrices, vcp, stack, light, overlay, ModelTransformationMode.GROUND);
	}

	/** 指定模式的物品渲染。注意：1.20 的 GROUND 变换内置绕 Y 转 90°（物品平放），套到竖直旋转的飞刀上会横置/偏离准星，须改用 FIXED。 */
	public static void renderItem(MatrixStack matrices, VertexConsumerProvider vcp, ItemStack stack, int light,
			int overlay, ModelTransformationMode mode) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		MinecraftClient mc = MinecraftClient.getInstance();
		ItemRenderer ir = mc.getItemRenderer();
		BakedModel model = ir.getModel(stack, mc.world, null, 0);
		ir.renderItem(stack, mode, false, matrices, vcp, light, overlay, model);
	}

		/**  未注册的物品统一用空 ItemStack 兜底（渲染器内判定为空时走占位盒）。 */
	public static final ItemStack PLACEHOLDER_STACK = ItemStack.EMPTY;

	/** 占位渲染别名，语义与 renderPlaceholderBox 一致。 */
	public static void renderPlaceholder(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
		renderPlaceholderBox(matrices, vcp, light, overlay);
	}

	/** 缺物品兜底：渲染一个醒目的品红色立方体，便于游戏测试识别"物品未"。 */
	public static void renderPlaceholderBox(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getDebugQuads());
		Matrix4f m = matrices.peek().getPositionMatrix();
		Matrix3f n = matrices.peek().getNormalMatrix();
		box(m, n, vc, -0.25f, -0.25f, -0.25f, 0.25f, 0.25f, 0.25f, 1f, 0f, 1f, 1f, light, overlay);
	}

	public static void box(Matrix4f m, Matrix3f n, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1,
			float z1, float r, float g, float b, float a, int light, int overlay) {
		// 上
		boxFace(m, n, vc, x0, y1, z0, x1, y1, z1, 0f, 1f, 0f, r, g, b, a);
		// 下
		boxFace(m, n, vc, x0, y0, z1, x1, y0, z0, 0f, -1f, 0f, r, g, b, a);
		// 北
		boxFace(m, n, vc, x0, y0, z0, x1, y1, z0, 0f, 0f, -1f, r, g, b, a);
		// 南
		boxFace(m, n, vc, x0, y0, z1, x1, y1, z1, 0f, 0f, 1f, r, g, b, a);
		// 西
		boxFace(m, n, vc, x0, y0, z0, x0, y1, z1, -1f, 0f, 0f, r, g, b, a);
		// 东
		boxFace(m, n, vc, x1, y0, z0, x1, y1, z1, 1f, 0f, 0f, r, g, b, a);
	}

	private static void boxFace(Matrix4f m, Matrix3f n, VertexConsumer vc, float x0, float y0, float z0, float x1,
			float y1, float z1, float nx, float ny, float nz, float r, float g, float b, float a) {
		vc.vertex(m, x0, y0, z0).color(r, g, b, a).normal(n, nx, ny, nz).next();
		vc.vertex(m, x1, y0, z1).color(r, g, b, a).normal(n, nx, ny, nz).next();
		vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(n, nx, ny, nz).next();
		vc.vertex(m, x0, y1, z0).color(r, g, b, a).normal(n, nx, ny, nz).next();
	}
}
