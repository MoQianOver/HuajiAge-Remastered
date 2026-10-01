package org.huajiager.client.render.model;

import org.huajiager.client.render.HARenderUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * 压路机/黑色轿车实体模型， ModelRoadRoller。
 * 原逻辑按实体 rollType 选择 roadRoller / blackCar 物品（ 未，由调用方传入兜底 stack）。
 */
public class ModelRoadRoller extends HAModelBase {

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 物品渲染统一走 render(matrices, vcp, light, overlay, stack) 重载
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, ItemStack stack) {
		HARenderUtil.renderItem(matrices, vcp, stack, light, overlay);
	}
}
