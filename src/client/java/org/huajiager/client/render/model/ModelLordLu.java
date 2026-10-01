package org.huajiager.client.render.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.HuajiagerWingLayerFactory;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * 挂者核心背部翅膀模型，  * （Blockbench 模型树）。
 *
 * 模型树为 core（背部核心环）+ wing（八片零厚翼膜）+ wingBones（八根翼骨），
 * 全部盒体数据（UV、偏移、尺寸、旋转角）与逐字段一致，贴图 128x128
 * （textures/entity/lord_lu_power.png）。lord 态由 ArmorRenderer 在背部渲染，
 * 翼膜与翼骨按实体 tick 反向旋转形成扇动动画（同参考版 LayerLordLu）。
 */
public class ModelLordLu {

	public static final Identifier TEXTURE = Identifier.of("huajiager",
			"textures/entity/lord_lu_power.png");

	/**
	 * 翅膀专用渲染层：复用 entityTranslucentEmissive 着色器（含本工程
	 * shader 覆盖，无光照满亮），深度测试 LEQUAL 且不写深度（COLOR_MASK）。
	 * 正常透视渲染，任何视角玩家身体都自然遮住翅膀。
	 */
	public static final RenderLayer WING_RENDER_LAYER =
			HuajiagerWingLayerFactory.createWingLayer(TEXTURE);

	private final ModelPart root;
	private final ModelPart core;
	private final ModelPart wing;
	private final ModelPart wingBones;

	private static ModelLordLu INSTANCE;

	private ModelLordLu() {
		ModelData data = new ModelData();
		ModelPartData rootData = data.getRoot();

		rootData.addChild("core",
				ModelPartBuilder.create().uv(0, 27).cuboid(-10.0F, -10.0F, 0.0F, 21, 21, 0),
				ModelTransform.pivot(0.0F, -1.0F, 7.0F));

		ModelPartData wingData = rootData.addChild("wing",
				ModelPartBuilder.create(), ModelTransform.pivot(0.4F, -1.0F, 8.0F));

		ModelPartData wings1 = wingData.addChild("wings_1",
				ModelPartBuilder.create(), ModelTransform.NONE);
		wings1.addChild("wing_1",
				ModelPartBuilder.create().uv(86, 2).cuboid(-7.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(-0.6145F, -14.9912F, 0.0F, 0.0F, 0.0F, -1.5708F));
		wings1.addChild("wing_2",
				ModelPartBuilder.create().uv(86, 7).cuboid(-10.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(0.3855F, 18.0088F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData wings2 = wingData.addChild("wings_2",
				ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
		wings2.addChild("wing_3",
				ModelPartBuilder.create().uv(86, 12).cuboid(-7.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(-0.6145F, -14.9912F, 0.0F, 0.0F, 0.0F, -1.5708F));
		wings2.addChild("wing_4",
				ModelPartBuilder.create().uv(86, 17).cuboid(-10.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(0.3855F, 18.0088F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData wings3 = wingData.addChild("wings_3",
				ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.3562F));
		wings3.addChild("wing_5",
				ModelPartBuilder.create().uv(86, 22).cuboid(-7.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(-0.6145F, -14.9912F, 0.0F, 0.0F, 0.0F, -1.5708F));
		wings3.addChild("wing_6",
				ModelPartBuilder.create().uv(86, 27).cuboid(-10.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(0.3855F, 18.0088F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData wings4 = wingData.addChild("wings_4",
				ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
		wings4.addChild("wing_7",
				ModelPartBuilder.create().uv(86, 32).cuboid(-7.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(-0.6145F, -14.9912F, 0.0F, 0.0F, 0.0F, -1.5708F));
		wings4.addChild("wing_8",
				ModelPartBuilder.create().uv(86, 37).cuboid(-10.7855F, -2.0088F, 0.0F, 20, 5, 0),
				ModelTransform.of(0.3855F, 18.0088F, 0.0F, 0.0F, 0.0F, 1.5708F));

		ModelPartData wingBonesData = rootData.addChild("wingBones",
				ModelPartBuilder.create(), ModelTransform.of(0.4F, -1.0F, 8.0F, 0.0F, 0.0F, 0.3927F));
		// 8 根翼骨：rotationZ 为各骨绕 z 的分布角（此前误传入 uv 参数未生效，8 根重叠成 1 根）
		wingBonesData.addChild("wing_bone_1", wingBone(52, 52, 8, 52), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -0.3927F));
		wingBonesData.addChild("wing_bone_2", wingBone(52, 52, 6, 55), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.3927F));
		wingBonesData.addChild("wing_bone_3", wingBone(55, 52, 6, 55), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 1.1781F));
		wingBonesData.addChild("wing_bone_4", wingBone(52, 55, 6, 55), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 1.9635F));
		wingBonesData.addChild("wing_bone_5", wingBone(55, 55, 6, 55), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 2.7489F));
		wingBonesData.addChild("wing_bone_6", wingBone(55, 55, 8, 52), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -2.7489F));
		wingBonesData.addChild("wing_bone_7", wingBone(52, 55, 8, 52), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -1.9635F));
		wingBonesData.addChild("wing_bone_8", wingBone(55, 52, 8, 52), ModelTransform.of(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -1.1781F));

		this.root = data.getRoot().createPart(128, 128);
		this.core = this.root.getChild("core");
		this.wing = this.root.getChild("wing");
		this.wingBones = this.root.getChild("wingBones");
	}

	/** 翼骨：三段渐缩骨架，UV 严格对齐 wing_bone_1~8（第一段 v / 第二段 v / 第三段 u,v） */
	private static ModelPartBuilder wingBone(int uvFirst, int uvSecond, int uvThirdU, int uvThirdV) {
		return ModelPartBuilder.create()
				.uv(0, uvFirst).cuboid(-5.4F, -23.0F, 0.0F, 10, 1, 0)
				.uv(3, uvSecond).cuboid(-4.4F, -20.0F, 0.0F, 8, 1, 0)
				.uv(uvThirdU, uvThirdV).cuboid(-3.4F, -17.0F, 0.0F, 6, 1, 0);
	}

	public static ModelLordLu get() {
		if (INSTANCE == null) {
			INSTANCE = new ModelLordLu();
		}
		return INSTANCE;
	}

	/**
	 * 以全亮无光照渲染背部翅膀（core + 翼膜 + 翼骨），并按渲染帧插值播放扇动动画。
	 * 渲染层与 LayerLordPower 语义对齐：enableBlend 混合 + disableLighting +
	 * 固定满亮 lightmap(240,240)。
	 *
	 * 黑色根因（两轮误判后才定位）：1.20.1 官方 rendertype_entity_translucent_emissive
	 * 并非"无光照"——其顶点着色器带 minecraft_mix_light(Light0_Direction, Light1_Direction,
	 * Normal, Color) 法线光照调制，且片元着色器以 light 参数越界采样 Sampler1 作
	 * overlayColor。本模组 0 厚度翼面法线朝 x/y，受固定方向光调制后亮度跌至 0.4 系数，
	 * 暗黄绿贴图被压成近黑；叠加半透明混合后观感即"黑色翅膀"。
	 * 已在 assets/minecraft/shaders/core/rendertype_entity_translucent_emissive.vsh/.fsh
	 * 覆盖官方 shader：vsh 去掉 mix_light（vertexColor = Color，等价 disableLighting），
	 * fsh 去掉 overlay 越界采样，保留 alpha<0.1 discard（翼膜区域贴图 alpha=0，与
	 * enableBlend 下透明一致）与雾化。该 emissive 层全工程仅本模型使用，无副作用。
	 *
	 * 卡顿根因： ageInTicks = ticksExisted + partialTicks（含渲染帧插值），移植版
	 * 传 entity.age 整数 tick，动画每秒只更新 20 次，视觉上翅膀似 15-30 帧跳变。	 * 故调用方须补传 MinecraftClient.getInstance().getTickDelta() 插值。
	 * 调用前 matrices 需已平移到玩家背部位置。
	 */
	public void renderWings(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
						   int ageInTicks, float tickDelta) {
		float age = ageInTicks + tickDelta;
		VertexConsumer vc = vertexConsumers.getBuffer(WING_RENDER_LAYER);
		this.core.render(matrices, vc, 0xF000F0, 0xFFFFFF, 1.0f, 1.0f, 1.0f, 1.0f);
		this.wing.roll = -age / 20.0f;
		this.wing.render(matrices, vc, 0xF000F0, 0xFFFFFF, 1.0f, 1.0f, 1.0f, 1.0f);
		this.wingBones.roll = age / 10.0f;
		this.wingBones.render(matrices, vc, 0xF000F0, 0xFFFFFF, 1.0f, 1.0f, 1.0f, 1.0f);
	}
}
