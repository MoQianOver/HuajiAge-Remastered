package org.huajiager.client.render.model;

import org.huajiager.client.render.HARenderUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * 灰色波发符实体模型， ModelOrgaHairKnife（原渲染 ItemLoader.orgaHairKnife 物品 GROUND）。
 *  物品未，由调用方传入兜底 ItemStack（可能为空 → 渲染占位盒）。
 */
public class ModelOrgaHairKnife extends HAModelBase {

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		// 物品渲染统一走 render(matrices, vcp, light, overlay, stack) 重载
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, ItemStack stack) {
		// 1.20 GROUND 内置绕 Y 转 90°（物品平放变换），直接套用会横置/偏移准星；改用 FIXED 立牌渲染
		HARenderUtil.renderItem(matrices, vcp, stack, light, overlay, net.minecraft.client.render.model.json.ModelTransformationMode.FIXED);
	}
}
