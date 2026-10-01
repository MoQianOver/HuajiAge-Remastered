package org.huajiager.client.render.model;

import org.huajiager.client.render.HARenderUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * 翡翠弹实体模型， ModelEmeraldBullet。
 * 原逻辑为直接渲染物品（GROUND）：默认绿宝石，若实体 bulletType 为已注册物品 id 则用该物品。
 */
public class ModelEmeraldBullet extends HAModelBase {

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 物品渲染统一走 render(matrices, vcp, light, overlay, stack) 重载
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, ItemStack stack) {
		HARenderUtil.renderItem(matrices, vcp, stack, light, overlay);
	}
}
