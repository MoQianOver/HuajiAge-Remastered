package org.huajiager.client.render.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

/**
 * ModelRenderer 的 1.20.1 兼容适配层（）。
 * 兼容旧版语义（rotationPoint / rotateAngle / mirror / addBox / addChild），
 * 内部惰性构建 Fabric 1.20.1 ModelPart（Cuboid + children），渲染时同步 pivot/angles。
 */
public class HAModelPart {

	private static final EnumSet<Direction> ALL_SIDES = EnumSet.allOf(Direction.class);

	private final HAModelBase base;

	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	/** ModelRenderer.offsetX/Y/Z：渲染时叠加到 rotationPoint 上，官方动画（悬浮/抖动）用。 */
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	public float xScale = 1f;
	public float yScale = 1f;
	public float zScale = 1f;
	public boolean mirror;
	public boolean isHidden;
	public boolean showModel = true;

	private final List<ModelPart.Cuboid> cuboids = new ArrayList<>();
	private final List<HAModelPart> children = new ArrayList<>();
	private ModelPart delegate;

	private int texU;
	private int texV;

	public HAModelPart() {
		this(null);
	}

	public HAModelPart(HAModelBase base) {
		this.base = base;
	}

	public HAModelPart(int u, int v) {
		this(null, u, v);
	}

	public HAModelPart(HAModelBase base, int u, int v) {
		this.base = base;
		this.texU = u;
		this.texV = v;
	}

	public HAModelPart addBox(float x, float y, float z, float w, float h, float d) {
		return addBox(texU, texV, x, y, z, w, h, d, 0f, false);
	}

	public HAModelPart addBox(float x, float y, float z, float w, float h, float d, float delta) {
		return addBox(texU, texV, x, y, z, w, h, d, delta, false);
	}

	public HAModelPart addBox(int u, int v, float x, float y, float z, float w, float h, float d) {
		return addBox(u, v, x, y, z, w, h, d, 0f, false);
	}

	public HAModelPart addBox(int u, int v, float x, float y, float z, float w, float h, float d, float delta,
			boolean mirror) {
		int texW = base != null ? base.textureWidth : 64;
		int texH = base != null ? base.textureHeight : 32;
		cuboids.add(new ModelPart.Cuboid(u, v, x, y, z, w, h, d, delta, delta, delta, mirror || this.mirror,
				(float) texW, (float) texH, ALL_SIDES));
		return this;
	}

	public HAModelPart addChild(HAModelPart child) {
		children.add(child);
		return this;
	}

	public void setRotationPoint(float x, float y, float z) {
		this.rotationPointX = x;
		this.rotationPointY = y;
		this.rotationPointZ = z;
	}

	public void setHidden(boolean hidden) {
		this.isHidden = hidden;
	}

	public void setTextureOffset(int u, int v) {
		this.texU = u;
		this.texV = v;
	}

	public void setTextureSize(int w, int h) {
		// 1.20.1 Cuboid 构造时即锁定了纹理尺寸，此处置空以兼容旧调用
	}

	public HAModelPart setMirror(boolean m) {
		this.mirror = m;
		return this;
	}

	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		render(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}

	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		if (delegate == null) {
			build();
		}
		sync();
		delegate.render(matrices, vertices, light, overlay, r, g, b, a);
	}

	private void build() {
		Map<String, ModelPart> childMap = new LinkedHashMap<>();
		for (int i = 0; i < children.size(); i++) {
			HAModelPart c = children.get(i);
			if (c.delegate == null) {
				c.build();
			}
			childMap.put("part" + i, c.delegate);
		}
		delegate = new ModelPart(cuboids, childMap);
	}

	private void sync() {
		delegate.setPivot(rotationPointX + offsetX, rotationPointY + offsetY, rotationPointZ + offsetZ);
		delegate.setAngles(rotateAngleX, rotateAngleY, rotateAngleZ);
		delegate.xScale = xScale;
		delegate.yScale = yScale;
		delegate.zScale = zScale;
		delegate.visible = showModel && !isHidden;
		for (HAModelPart c : children) {
			c.sync();
		}
	}
}
