package org.huajiager.client.render.entity;

import org.huajiager.client.render.HARenderUtil;
import org.huajiager.entity.EntitySecondFoil;
import org.huajiager.init.loaders.ItemLoader;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 波普飞机实体渲染器， RenderSecondFoil（RenderSnowball，渲染 secondFoilEntity 物品）。
 */
public class RenderSecondFoil extends EntityRenderer<EntitySecondFoil> {

	private static final ItemStack STACK = new ItemStack(ItemLoader.secondFoilEntity);

	public RenderSecondFoil(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntitySecondFoil entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		// 物品图标默认基准约半格，2.0f 缩放后视觉约一格方块大
		matrices.scale(2.0f, 2.0f, 2.0f);
		// billboard：对齐 RenderSnowball——乘相机旋转使物品始终正面朝向玩家，
		// 否则只有朝正北时才是正面，侧面变横截面、正南成镜像
		matrices.multiply(this.dispatcher.getRotation());
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
		HARenderUtil.renderItem(matrices, vcp, STACK, light, 0);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntitySecondFoil entity) {
		return null;
	}
}
