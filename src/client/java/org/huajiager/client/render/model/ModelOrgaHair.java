package org.huajiager.client.render.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * 奥尔加发型客户端模型， （Techne）。
 *
 * 按 ModelOrgaArmor 的模型树完整复刻：
 * 以标准外层护甲网格（ArmorEntityModel.getModelData）为基底，在 head 节点下
 * addChild 10 段发型盒体（hair1~hair10）。盒体数据（UV、偏移、尺寸、旋转角）与
 * 与参考模型逐字段一致，贴图 64x64（textures/models/armor/orga.png）。
 *
 * 渲染交由 HuajiAgeRemasteredClient 中与滑稽套装完全一致的 ArmorRenderer 管线
 * （外层护甲模型 + copyBipedStateTo 姿态同步 + 仅 HEAD 可见），保证与已验证可见的
 * 套装渲染路径同构，规避"自定义裸 ModelPart 平级树 + 手动 root.render 与护甲
 * 渲染阶段不匹配导致整层不可见"的问题。
 */
public class ModelOrgaHair {

	public static final Identifier TEXTURE = new Identifier("huajiager", "textures/models/armor/orga.png");

	private static ArmorEntityModel<LivingEntity> INSTANCE;

	/**
	 * 获取共享单例（首次调用时构建）。
	 * 原实现每帧在 ArmorRenderer 回调内重建整棵 ModelPart 树（10 段发型 addChild +
	 * createPart bake），与 ModelBlanceHelmet 一起构成护甲渲染链路的卡顿源。	 * 改为单例复用后，姿态由 copyBipedStateTo 每帧覆盖、可见性由回调开头
	 * setVisible(false) 递归重置，不会产生跨帧状态泄漏。
	 */
	public static ArmorEntityModel<LivingEntity> get() {
		if (INSTANCE == null) {
			INSTANCE = createHairAttachedArmorModel();
		}
		return INSTANCE;
	}

	/**
	 * 构建"外层护甲结构 + 头部挂载 10 段发型盒体"的 ArmorEntityModel。
	 * 模型树与参考模型一致，bake 为 64x64 纹理布局。
	 */
	public static ArmorEntityModel<LivingEntity> createHairAttachedArmorModel() {
		ModelData data = ArmorEntityModel.getModelData(Dilation.NONE);
		ModelPartData head = data.getRoot().getChild(EntityModelPartNames.HEAD);

		head.addChild("hair10", hair(31, 40, -1, -8.9f, 1.533333f, 2, 3, 5),
				ModelTransform.of(0f, 0f, 0f, 0.5576792f, 0f, 0f));
		head.addChild("hair9", hair(0, 40, -2.033333f, -8.666667f, -0.4666667f, 4, 3, 7),
				ModelTransform.of(0f, 0f, 0f, 0.3346075f, 0f, 0f));
		head.addChild("hair8", hair(31, 51, -0.8666667f, -8.533334f, -1f, 2, 2, 6),
				ModelTransform.of(0f, 0f, 0f, 0.1858931f, -0.2602503f, -0.4089647f));
		head.addChild("hair7", hair(31, 51, -2.066667f, -8.533334f, -1.4f, 2, 2, 6),
				ModelTransform.of(0f, 0f, 0f, 0.1487144f, 0.1487144f, 0.5205006f));
		head.addChild("hair6", hair(0, 51, -3, -8.6f, -2.066667f, 6, 2, 7),
				ModelTransform.of(0f, 0f, 0f, 0.1858931f, 0f, 0f));
		head.addChild("hair5", hair(11, 4, -3.266667f, -8, -5.266667f, 2, 4, 1),
				ModelTransform.of(0f, 0f, 0f, -0.1487144f, 0.0371786f, 0.6320364f));
		head.addChild("hair4", hair(10, 1, 0.2f, -8.6f, -4.8f, 2, 4, 1),
				ModelTransform.of(0f, 0f, 0f, -0.0743572f, -0.0371786f, -0.5205006f));
		head.addChild("hair3", hair(9, 1, -1, -8.633333f, -4.833333f, 2, 4, 1),
				ModelTransform.of(0f, 0f, 0f, -0.0743572f, 0f, 0f));
		head.addChild("hair2", hair(33, 0, -4, -8, -4.2f, 7, 6, 8),
				ModelTransform.of(0f, 0f, 0f, 0f, 0f, 0f));
		head.addChild("hair1", hair(11, 0, -1, -6, -4.5f, 1, 1, 1),
				ModelTransform.of(0f, 0f, 0f, 0f, 0f, 0f));

		return new ArmorEntityModel<>(data.getRoot().createPart(64, 64));
	}

	private static ModelPartBuilder hair(int u, int v, float x, float y, float z,
										 float sizeX, float sizeY, float sizeZ) {
		return ModelPartBuilder.create().uv(u, v).cuboid(x, y, z, sizeX, sizeY, sizeZ);
	}
}
