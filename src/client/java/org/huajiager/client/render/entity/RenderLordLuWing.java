package org.huajiager.client.render.entity;

import org.huajiager.client.render.model.ModelLordLu;
import org.huajiager.entity.EntityLordLuWing;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Lord.Lu 翅膀展示实体渲染器（独立实体渲染，替代原 ArmorRenderer 背部渲染）。
 *
 * 矩阵上下文与 ArmorRenderer 时期一致：修正到玩家实时位置后，先补玩家模型高度
 * 抬升 translate(0,1.5,0)（LivingEntityRenderer 对玩家模型自带，独立实体需手动补齐，
 * 否则翅膀矮约 1.5 格），再 rotateY(180-bodyYaw) + scale(-1,-1,1)（玩家模型标准翻转
 * 空间，z 正=玩家背后；bodyYaw 使转视角时翅膀不跟着漂移），
 * 再 translate(0,0,0.5) 平移背部，最后调用 ModelLordLu.renderWings。
 * 实体 tick 把位置放在玩家背后 0.5 格仅用于渲染排序（背视角翅膀比玩家近 → 后画
 * → 覆盖披风/身体），此处先按"玩家插值位置 - 实体插值位置"把矩阵修正回玩家位置，
 * 避免该排序偏移影响实际绘制位置。
 */
public class RenderLordLuWing extends EntityRenderer<EntityLordLuWing> {

	public RenderLordLuWing(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	/**
	 * 紧贴玩家的展示实体：默认按极小可见盒做视锥剔除会频繁闪没，显式不剔除
	 * （与 RenderStandBase 同款处理，EntityLordLuWing.getVisibilityBoundingBox 亦已放大）。
	 */
	@Override
	public boolean shouldRender(EntityLordLuWing entity, Frustum frustum, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(EntityLordLuWing entity, float yaw, float tickDelta, MatrixStack matrices,
			net.minecraft.client.render.VertexConsumerProvider vcp, int light) {
		matrices.push();
		PlayerEntity user = entity.getOwner();
		float renderYaw = entity.getYaw();
		if (user != null) {
			// 实时贴人：用宿主玩家本帧插值位置与实时 yaw 重算，消除服务端同步 1~2 tick 滞后
			Vec3d tiePos = user.getLerpedPos(tickDelta);
			Vec3d entPos = entity.getLerpedPos(tickDelta);
			matrices.translate((float) (tiePos.x - entPos.x), (float) (tiePos.y - entPos.y),
					(float) (tiePos.z - entPos.z));
			// 用身体朝向（bodyYaw）而非视线 yaw：玩家原地转视角（转头）时身体不转，
			// 翅膀不跟着漂移；仅当角色身体真正转向（移动转向/转身动画）时翅膀随身体平滑转向。
			renderYaw = user.getBodyYaw();
		}
		// 玩家模型标准渲染上下文（同 ArmorRenderer 回调时的矩阵）：
		// 1) 先补玩家模型高度抬升 1.5 格——LivingEntityRenderer 渲染玩家模型前有
		//    translate(0,1.501,0)（把模型原点从脚底抬到身体中部），ArmorRenderer 回调
		//    自带该上下文，独立实体渲染没有，缺了翅膀整体矮约 1.5 格。		//    该 translate 必须放在 scale(-1,-1,1) 之前（平移量会随 y 翻转变号，替身渲染器同款坑）
		matrices.translate(0.0F, 1.5F, 0.0F);
		// 2) rotateY(180-bodyYaw) 使模型朝向身体（z 正=背后，同玩家模型渲染）
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - renderYaw));
		// 3) scale(-1,-1,1) 玩家模型标准翻转空间
		matrices.scale(-1.0F, -1.0F, 1.0F);
		// 平移背部（ArmorRenderer 时期同款），模型树自带 z=7/8px 背部偏移
		matrices.translate(0.0F, 0.0F, 0.5F);
		ModelLordLu.get().renderWings(matrices, vcp, entity.age, tickDelta);
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vcp, light);
	}

	@Override
	public Identifier getTexture(EntityLordLuWing entity) {
		return ModelLordLu.TEXTURE;
	}
}
