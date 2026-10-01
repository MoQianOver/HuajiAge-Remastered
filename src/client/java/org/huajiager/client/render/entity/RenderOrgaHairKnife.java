package org.huajiager.client.render.entity;

import org.huajiager.client.render.HARenderUtil;
import org.huajiager.client.render.model.ModelOrgaHairKnife;
import org.huajiager.entity.EntityOrgaHairKnife;
import org.huajiager.init.loaders.ItemLoader;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 灰色波发符实体渲染器， RenderOrgaHairKnife。
 * 渲染物品模型（GROUND 模式），参照 RenderSecondFoil 用真实物品栈。
 */
public class RenderOrgaHairKnife extends EntityRenderer<EntityOrgaHairKnife> {

	private static final ModelOrgaHairKnife MODEL = new ModelOrgaHairKnife();
	private static final ItemStack STACK = new ItemStack(ItemLoader.orgaHairKnife);

	public RenderOrgaHairKnife(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntityOrgaHairKnife entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
								matrices.push();
		boolean flying = entity.getVelocity().lengthSquared() >= 1.0E-4D;
		float yawRot = flying ? entity.getYaw() : entity.getRotation();
		float pitchRot = entity.getPitch();
		// FIXED 立牌面朝+Z：Y(-yaw)+X(pitch) 把法线对准飞行方向，Z(age*40) 绕飞行方向竖转
		// （不再用 GROUND，避开其内置绕 Y 90° 导致的横置/准星偏移）
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flying ? -yawRot : yawRot));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitchRot));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.age * 40F));
		matrices.scale(0.5f, 0.5f, 0.5f);
		MODEL.render(matrices, vcp, light, 0, STACK);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityOrgaHairKnife entity) {
		return null;
	}
}
