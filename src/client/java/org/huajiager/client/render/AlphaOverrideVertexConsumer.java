package org.huajiager.client.render;

import net.minecraft.client.render.VertexConsumer;

/**
 * 顶点消费者包装：强制把写入顶点的 alpha 覆盖为固定值，用于第一人称飞行态
 * 替身半透明渲染（底层 buffer 必须来自 RenderLayer.getEntityTranslucent* 才会混合）。
 */
public class AlphaOverrideVertexConsumer implements VertexConsumer {

	private final VertexConsumer delegate;
	private final float alpha;

	public AlphaOverrideVertexConsumer(VertexConsumer delegate, float alpha) {
		this.delegate = delegate;
		this.alpha = alpha;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		delegate.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		delegate.color(red, green, blue, (int) (this.alpha * 255.0F));
		return this;
	}

	@Override
	public VertexConsumer color(float red, float green, float blue, float alpha) {
		delegate.color(red, green, blue, this.alpha);
		return this;
	}

	@Override
	public VertexConsumer texture(float u, float v) {
		delegate.texture(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlay(int u, int v) {
		delegate.overlay(u, v);
		return this;
	}

	@Override
	public VertexConsumer light(int u, int v) {
		delegate.light(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void fixedColor(int red, int green, int blue, int alpha) {
		delegate.fixedColor(red, green, blue, (int) (this.alpha * 255.0F));
	}

	@Override
	public void unfixColor() {
		delegate.unfixColor();
	}

	@Override
	public void next() {
		delegate.next();
	}
}
