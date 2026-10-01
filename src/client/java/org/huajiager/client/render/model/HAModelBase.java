package org.huajiager.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * ModelBase 的 1.20.1 兼容适配层。
 *  ，保留 textureWidth/Height。
 */
public abstract class HAModelBase {

	public int textureWidth = 64;
	public int textureHeight = 32;

	public HAModelBase() {
	}

	public HAModelBase(int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		render(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	/** 子类实现：渲染所有 ModelPart，scale 由调用方通过 MatrixStack 统一施加。 */
	public abstract void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r,
			float g, float b, float a);

	protected void renderPart(HAModelPart part, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		part.render(matrices, vertices, light, overlay);
	}
}
