package org.huajiager.client.render.entity;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.client.render.HARenderUtil;
import org.huajiager.client.render.model.ModelEmeraldBullet;
import org.huajiager.entity.EmeraldBulletEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 翡翠弹实体渲染器， RenderEmeraldBullet。默认绿宝石；bulletType 为已注册物品则渲染对应物品。
 */
public class RenderEmeraldBullet extends EntityRenderer<EmeraldBulletEntity> {

	private static final ModelEmeraldBullet MODEL = new ModelEmeraldBullet();

	public RenderEmeraldBullet(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EmeraldBulletEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getRotationRandom()));

		ItemStack stack = resolveStack(entity);
		MODEL.render(matrices, vcp, light, OverlayTexture.DEFAULT_UV, stack);
		matrices.pop();
	}

	private static ItemStack resolveStack(EmeraldBulletEntity entity) {
		ItemStack stack = new ItemStack(Items.EMERALD);
		String type = entity.getBulletType();
		if (type != null && !type.isEmpty()) {
			Identifier id = Identifier.tryParse(type);
			if (id != null) {
				Item item = Registries.ITEM.get(id);
				if (item != Items.AIR) {
					stack = new ItemStack(item);
				}
			}
		}
		return stack;
	}

	@Override
	public Identifier getTexture(EmeraldBulletEntity entity) {
		return null;
	}
}
