package org.huajiager.client.render.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * 五五开头盔护甲模型，  * （Techne 模型树）。
 *
 * 以标准 ArmorEntityModel 网格为基底，将 head 节点整体替换为 Base1 外凸盔体
 * （Base2/Base3/Base4/p1~p9/tubes_y 子盒体），eyes 与 tubes_b 作为 head 子节点
 * 由渲染管线按 NBT 状态动态显示（lord 显管、open+active 发光）。
 * 贴图 textures/models/armor/blance_helmet.png（64x64）。
 */
public class ModelBlanceHelmet extends ArmorEntityModel<LivingEntity> {

	public static final Identifier TEXTURE = Identifier.of("huajiager",
			"textures/models/armor/blance_helmet.png");

	/** eyes 发光眼带（head 子节点，open+active 发光时渲染） */
	private final ModelPart eyes;
	/** tubes_b 后部黑管（head 子节点，lord 时显示，open+active 发光时高亮） */
	private final ModelPart tubesB;
	/** tubes_y 侧部斜管（head 子节点，lord 时显示） */
	private final ModelPart tubesY;

	private static ModelBlanceHelmet INSTANCE;

	public ModelBlanceHelmet(ModelPart root) {
		super(root);
		ModelPart head = root.getChild(EntityModelPartNames.HEAD);
		this.eyes = head.getChild("eyes");
		this.tubesB = head.getChild("tubes_b");
		this.tubesY = head.getChild("tubes_y");
	}

	/**
	 * 获取共享单例（首次调用时构建）。
	 * 原实现每帧在 ArmorRenderer 回调内重建整棵 ModelPart 树（15+ 节点 addChild +
	 * createPart bake），是 lord 翅膀扇动卡顿的主要来源；改为单例复用后，姿态由
	 * contextModel.copyBipedStateTo(model) 每帧覆盖、可见性由回调开头 setVisible(false)
	 * 递归重置，不会产生跨帧状态泄漏。
	 */
	public static ModelBlanceHelmet get() {
		if (INSTANCE == null) {
			INSTANCE = createBlanceArmorModel();
		}
		return INSTANCE;
	}

	/**
	 * 构建"外层护甲结构 + head 替换为 Base1 外凸盔体"的 ArmorEntityModel。
	 * 盒体数据（UV、偏移、尺寸、旋转角）与 ModelBlanceHelmet 逐字段一致。
	 */
	public static ModelBlanceHelmet createBlanceArmorModel() {
		ModelData data = ArmorEntityModel.getModelData(Dilation.NONE);
		ModelPartData root = data.getRoot();

		//  bipedHead = Base1：整体替换默认 head 盒体
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD,
				ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -9.0F, -5.0F, 10, 4, 10),
				ModelTransform.NONE);

		head.addChild("base2",
				ModelPartBuilder.create().uv(12, 5).cuboid(-4.0F, -5.0F, 4.0F, 8, 3, 1),
				ModelTransform.NONE);
		head.addChild("base3",
				ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -5.0F, 0.0F, 1, 5, 5),
				ModelTransform.NONE);
		head.addChild("base4",
				ModelPartBuilder.create().uv(0, 0).cuboid(4.0F, -5.0F, 0.0F, 1, 5, 5),
				ModelTransform.NONE);
		head.addChild("p1",
				ModelPartBuilder.create().uv(14, 28).cuboid(4.4667F, -6.0F, 0.0F, 2, 2, 2),
				ModelTransform.NONE);
		head.addChild("p2",
				ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -6.4667F, -6.0F, 1, 5, 6),
				ModelTransform.NONE);
		head.addChild("p3",
				ModelPartBuilder.create().uv(0, 21).cuboid(-5.0F, -7.0F, -6.0F, 10, 7, 1),
				ModelTransform.NONE);
		head.addChild("p4",
				ModelPartBuilder.create().uv(0, 28).cuboid(-6.4667F, -6.0F, 0.0F, 2, 2, 2),
				ModelTransform.NONE);
		head.addChild("p5",
				ModelPartBuilder.create().uv(0, 0).cuboid(5.0F, -6.4667F, -6.0F, 1, 5, 6),
				ModelTransform.NONE);
		head.addChild("p6",
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -7.0F, 8, 1, 1),
				ModelTransform.NONE);
		head.addChild("p7",
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -6.0F, -7.0F, 8, 1, 1),
				ModelTransform.NONE);
		head.addChild("p8",
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -5.0F, -7.0F, 1, 1, 1),
				ModelTransform.NONE);
		head.addChild("p9",
				ModelPartBuilder.create().uv(0, 0).cuboid(3.0F, -5.0F, -7.0F, 1, 1, 1),
				ModelTransform.NONE);

		head.addChild("eyes",
				ModelPartBuilder.create().uv(25, 30).cuboid(-3.5F, -5.5F, -6.2F, 7, 2, 0),
				ModelTransform.NONE);
		head.addChild("tubes_b",
				ModelPartBuilder.create()
						.uv(0, 37).cuboid(-6.5F, -6.0F, -6.5F, 1, 1, 4)
						.uv(0, 37).cuboid(-6.5F, -4.0F, -6.5F, 1, 1, 4)
						.uv(0, 37).cuboid(5.5F, -6.0F, -6.5F, 1, 1, 4)
						.uv(0, 37).cuboid(5.5F, -4.0F, -6.5F, 1, 1, 4),
				ModelTransform.NONE);
		head.addChild("tubes_y",
				ModelPartBuilder.create()
						.uv(0, 43).cuboid(4.5F, -6.0F, -1.0F, 1, 1, 8)
						.uv(20, 43).cuboid(4.5F, -7.0F, -1.0F, 1, 1, 8)
						.uv(0, 43).cuboid(-5.5F, -6.0F, -1.0F, 1, 1, 8)
						.uv(20, 43).cuboid(-5.5F, -7.0F, -1.0F, 1, 1, 8),
				ModelTransform.of(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		return new ModelBlanceHelmet(root.createPart(64, 64));
	}

	/** 设置 lord 态管子显示。 */
	public void setTubesVisible(boolean lord) {
		this.tubesB.visible = lord;
		this.tubesY.visible = lord;
	}

	/** 发光渲染 eyes 与 tubes_b。 */
	public void renderEyesTubes(MatrixStack matrices, VertexConsumer vertices) {
		this.eyes.visible = true;
		this.tubesB.visible = true;
		this.eyes.render(matrices, vertices, 0xF000F0, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
		this.tubesB.render(matrices, vertices, 0xF000F0, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
	}
}
