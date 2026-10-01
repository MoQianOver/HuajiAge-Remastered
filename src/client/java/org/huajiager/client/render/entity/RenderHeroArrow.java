package org.huajiager.client.render.entity;

import org.huajiager.entity.EntityHeroArrow;
import org.huajiager.init.loaders.ItemLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * 英雄之箭渲染器， RenderHeroArrow。
 *
 * 直接以已的「普通滑稽之星」huajiStar 物品模型渲染英雄之箭，
 * 替代原先 HARenderUtil.renderPlaceholder 的品红占位方块
 * （用户反馈"粉色方块一直冒火"即为占位贴图 + 飞行动画粒子叠加的观感）。 * 后应要求把多重叠加态 huajiStarPoly 换成普通 huajiStar。
 * 星形模型轴对称，无需按飞行朝向旋转，缩小到 0.5 渲染。
 */
public class RenderHeroArrow extends EntityRenderer<EntityHeroArrow> {

	private static final ItemStack HERO_ARROW_ICON = new ItemStack(ItemLoader.huajiStar);

	public RenderHeroArrow(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(EntityHeroArrow entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		// 面朝相机（billboard）： RenderSnowball 渲染时乘 dispatcher.getRotation()，
		// 使星形物品贴图始终正面面对玩家，否则 2D 平面固定世界朝向会"斜着、不面对玩家"，
		// 视觉上还会让弹道看起来从准星偏左偏右。
		matrices.multiply(this.dispatcher.getRotation());
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
		matrices.scale(0.5f, 0.5f, 0.5f);
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		BakedModel model = itemRenderer.getModel(HERO_ARROW_ICON, entity.getWorld(), null, entity.getId());
		itemRenderer.renderItem(HERO_ARROW_ICON, ModelTransformationMode.FIXED, false, matrices, vcp,
				LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV, model);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EntityHeroArrow entity) {
		return null;
	}
}
