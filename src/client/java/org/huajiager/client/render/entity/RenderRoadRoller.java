package org.huajiager.client.render.entity;

import org.huajiager.client.render.HARenderUtil;
import org.huajiager.client.render.model.ModelRoadRoller;
import org.huajiager.entity.EntityRoadRoller;
import org.huajiager.init.loaders.ItemLoader;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 压路机/黑色轿车实体渲染器， RenderRoadRoller。
 *  ModelRoadRoller 按实体 rollType 渲染 roadRoller/blackCar 物品模型，
 * 这里按类型选择对应已注册物品（物品缺失时回退占位盒便于排查）。
 */
public class RenderRoadRoller extends EntityRenderer<EntityRoadRoller> {

	private static final ModelRoadRoller MODEL = new ModelRoadRoller();

	public RenderRoadRoller(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	private static ItemStack stackFor(EntityRoadRoller entity) {
		if (EntityRoadRoller.enumTYPE.CAR.getName().equals(entity.getRollType())) {
			return ItemLoader.blackCar == null ? ItemStack.EMPTY : new ItemStack(ItemLoader.blackCar);
		}
		return ItemLoader.roadRoller == null ? ItemStack.EMPTY : new ItemStack(ItemLoader.roadRoller);
	}

	@Override
	public void render(EntityRoadRoller entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));
		// 在 X180 之后还有 rotate(90F, 0F, -1F, 0F)，即绕 Y 轴 -90°，缺了这一步轮胎会横躺
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-Math.abs(entity.getPitch())));
		matrices.scale(1f, -1f, -1f);
		ItemStack stack = stackFor(entity);
		if (stack.isEmpty()) {
			HARenderUtil.renderPlaceholder(matrices, vcp, light, 0);
		} else {
			MODEL.render(matrices, vcp, light, 0, stack);
		}
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityRoadRoller entity) {
		return null;
	}
}
