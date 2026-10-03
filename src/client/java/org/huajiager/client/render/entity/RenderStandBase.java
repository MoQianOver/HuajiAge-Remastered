package org.huajiager.client.render.entity;

import org.huajiager.client.render.AlphaOverrideVertexConsumer;
import org.huajiager.client.render.model.HAModelBase;
import org.huajiager.client.render.model.ModelCrazyDiamond;
import org.huajiager.client.render.model.ModelCrazyDiamondIdle;
import org.huajiager.client.render.model.ModelHierophantGreen;
import org.huajiager.client.render.model.ModelHierophantGreenIdle;
import org.huajiager.client.render.model.ModelHermitPurple;
import org.huajiager.client.render.model.ModelHermitPurpleOverdrive;
import org.huajiager.client.render.model.ModelKillerQueen;
import org.huajiager.client.render.model.ModelKillerQueenPunch;
import org.huajiager.client.render.model.ModelOrgaFly;
import org.huajiager.client.render.model.ModelOrgaRequiem;
import org.huajiager.client.render.model.ModelStandDefault;
import org.huajiager.client.render.model.ModelStarPlatinum;
import org.huajiager.client.render.model.ModelStarPlatinumIdle;
import org.huajiager.client.render.model.ModelTheWorld;
import org.huajiager.client.render.model.ModelTheWorldIdle;
import org.huajiager.client.render.model.ModelWhiteSnake;
import org.huajiager.client.render.model.ModelWhiteSnakePunch;
import org.huajiager.client.render.model.StandAnimatedModel;
import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.ParticleLoader;
import org.huajiager.util.HAMathHelper;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.CustomRenderLayers;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.Map;

/**
 * 替身实体渲染器（独立实体渲染）。
 *
 * 背景：替身外观由玩家身上的叠加层（LayerStand / ModelStandBase 体系）绘制，Fabric 端
 * 采用独立实体 + HAModelPart 模型渲染。矩阵与叠加层上下文保持一致：180-yaw + scale(-1,-1,1)，
 * 并额外补 root 上移 translate(0,H,0)（叠加层以玩家脚底为原点即自带该上下文，独立实体需补齐）。
 * THE_WORLD 使用 Blockbench 模型 ModelTheWorld（default/攻击：正立悬浮盘腿十二连挥拳）
 * + 64x128 贴图；闲置态切换 ModelTheWorldIdle（抱胸盘腿 + 双齿轮转动）+ Idle 专属贴图，
 * 未被注册的其它替身回退 ModelStandDefault 标准人形占位。
 */
public class RenderStandBase extends EntityRenderer<EntityStandBase> {


	private static final Identifier FALLBACK_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_the_world_default.png");
	/** THE_WORLD 闲置态贴图（ModelTheWorldIdle 专用 64x128 Blockbench UV）。 */
	private static final Identifier THE_WORLD_IDLE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_the_world_idle.png");
	/** STAR_PLATINUM 闲置态贴图（ModelStarPlatinumIdle 专用 64x128 Blockbench UV）。 */
	private static final Identifier STAR_PLATINUM_IDLE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_star_platinum_idle.png");
	/** HIEROPHANT_GREEN 闲置态贴图（ModelHierophantGreenIdle 专用 64x128 Blockbench UV）。 */
	private static final Identifier HIEROPHANT_GREEN_IDLE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_hierophant_green_idle.png");
	/** KILLER_QUEEN 攻击态贴图（ModelKillerQueenPunch 专用 128x128 Blockbench UV）。 */
	private static final Identifier KILLER_QUEEN_PUNCH_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_killer_queen_punch.png");
	/** ORGA_REQUIEM 飞行态贴图（ModelOrgaFly 专用 64x64  UV）。 */
	private static final Identifier ORGA_REQUIEM_FLY_TEXTURE = Identifier
			.of("huajiager", "textures/entity/entity_orga_requiem_fly.png");
	/** CRAZY_DIAMOND 贴图（ModelCrazyDiamond 专用 128x128 Blockbench UV）。 */
	private static final Identifier CRAZY_DIAMOND_TEXTURE = Identifier
			.of("huajiager", "textures/entity/crazy_diamond.png");
	/** CRAZY_DIAMOND 闲置态贴图（ModelCrazyDiamondIdle 专用 128x128 Blockbench UV）。 */
	private static final Identifier CRAZY_DIAMOND_IDLE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/crazy_diamond_idle.png");
	/** CRAZY_DIAMOND 治疗态粉红罩贴图：直接用 crazy_diamond_heal.png 左下角粉红区，不改贴图像素。 */
	private static final Identifier CRAZY_DIAMOND_HALO_TEXTURE = Identifier
			.of("huajiager", "textures/entity/crazy_diamond_heal.png");
	/** HERMIT_PURPLE 贴图（ModelHermitPurple 专用 64x64 Blockbench UV）。 */
	private static final Identifier HERMIT_PURPLE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/hermit_purple.png");
	/** HERMIT_PURPLE 爆发态贴图（ModelHermitPurpleOverdrive 专用 64x64 Blockbench UV）。 */
	private static final Identifier HERMIT_PURPLE_OVERDRIVE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/hermit_purple_overdrive.png");
	/** WHITE_SNAKE 贴图（ModelWhiteSnake 专用 128x128 Blockbench UV）。 */
	private static final Identifier WHITE_SNAKE_TEXTURE = Identifier
			.of("huajiager", "textures/entity/white_snake.png");
	/** WHITE_SNAKE 攻击态贴图（ModelWhiteSnakePunch 专用 128x128 Blockbench UV）。 */
	private static final Identifier WHITE_SNAKE_PUNCH_TEXTURE = Identifier
			.of("huajiager", "textures/entity/white_snake_punch.png");

	private final HAModelBase defaultModel;
	private final Map<String, HAModelBase> standModels;
	/** THE_WORLD 闲置态模型（default 模型的 power=0 收手下垂实现已废弃，改为抱胸盘腿造型）。 */
	private final ModelTheWorldIdle theWorldIdleModel;
	/** STAR_PLATINUM 闲置态模型（抱胸抱拳 + 双拳残影，区别于 default 攻击挥拳造型）。 */
	private final ModelStarPlatinumIdle starPlatinumIdleModel;
	/** HIEROPHANT_GREEN 闲置态模型（后仰盘坐 + 背后翡翠条带造型，区别于 default 攻击悬浮造型）。 */
	private final ModelHierophantGreenIdle hierophantGreenIdleModel;
	/** CRAZY_DIAMOND 闲置态模型（蹲伏双拳造型 ModelCrazyDiamondIdle，区别于 default 正立悬浮造型）。 */
	private final ModelCrazyDiamondIdle crazyDiamondIdleModel;
	/** HERMIT_PURPLE 爆发态模型（藤蔓缠绕造型 ModelHermitPurpleOverdrive，区别于 default 待机造型）。 */
	private final ModelHermitPurpleOverdrive hermitPurpleOverdriveModel;
	/** KILLER_QUEEN 攻击态模型（十指挥拳造型 ModelKillerQueenPunch，区别于 default 待机造型）。 */
	private final ModelKillerQueenPunch killerQueenPunchModel;
	/** ORGA_REQUIEM 飞行态模型（躺平飞行造型 ModelOrgaFly，区别于 default 直立悬浮造型）。 */
	private final ModelOrgaFly orgaFlyModel;
	/** WHITE_SNAKE 攻击态模型（挥拳造型 ModelWhiteSnakePunch，区别于 default 待机造型）。 */
	private final ModelWhiteSnakePunch whiteSnakePunchModel;

	public RenderStandBase(EntityRendererFactory.Context ctx) {
		super(ctx);
		// 原版实体模型借用表需要模型加载器，才能在用到时构建对应的模型树
		org.huajiager.client.model.custom.VanillaStandModels.bind(ctx.getModelLoader());
		// 借用表自检：确认已登记的原版模型确实能取到（失败只在日志留痕，不影响内置替身）
		org.huajiager.client.model.custom.VanillaStandModels.find("minecraft:warden_default");
		this.defaultModel = new ModelStandDefault();
		this.standModels = new HashMap<>();
		// THE_WORLD 启用模型：ModelTheWorld 即 Blockbench 正立悬浮盘腿造型
		// （head y-6..0、body 0..7、腿 pivot y11 弯曲），配贴图 UV 完全匹配。
		register(StandLoader.THE_WORLD.getName(), new ModelTheWorld());
		// STAR_PLATINUM 启用模型：ModelStarPlatinum（正立悬浮造型 + 十二连挥拳），配 entity_star_platinum_default.png 贴图 UV
		// 完全匹配，修复此前回落标准人形占位导致的"贴图错乱 / 位置在脚下"。
		register(StandLoader.STAR_PLATINUM.getName(), new ModelStarPlatinum());
		// HIEROPHANT_GREEN 启用模型：ModelHierophantGreen（正立悬浮造型 + 背后翡翠齿轮盘旋转），配 entity_hierophant_green_default.png
		// 贴图 UV 完全匹配，修复此前回落标准人形占位导致的"贴图错乱 / 位置在脚下"。
		register(StandLoader.HIEROPHANT_GREEN.getName(), new ModelHierophantGreen());
		// KILLER_QUEEN 启用模型：ModelKillerQueen（待机悬浮造型），配 entity_killer_queen_default.png 贴图 UV 完全匹配，
		// 修复此前回落标准人形占位导致的"贴图错乱 / 位置在脚下"。
		register(StandLoader.KILLER_QUEEN.getName(), new ModelKillerQueen());
		// ORGA_REQUIEM 启用模型：ModelOrgaRequiem（黑色长发 + 背后七根飘带），配 entity_orga_requiem_default.png 贴图 UV
		// 完全匹配，修复此前回落标准人形占位导致的"贴图错乱 / 本体贴脸"。
		register(StandLoader.ORGA_REQUIEM.getName(), new ModelOrgaRequiem());
		// CRAZY_DIAMOND 启用模型：ModelCrazyDiamond
		// Blockbench JSON（crazy_diamond.json，128x128 UV，正立悬浮出拳造型），
		// 配 crazy_diamond.png 贴图 UV 完全匹配，修复此前回落标准人形占位
		// 导致的"贴图错乱 / 位置在脚下"。
		// 注意：StandCustom 的 name 来自 JSON 的 stand 字段 = "huajiager:crazy_diamond"
		// （带命名空间），pickModel 用 getName() 查表，必须同时注册带/不带命名空间两个 key，
		// 否则 getName() = "huajiager:crazy_diamond" 查不到短名 key → 回落默认人形。
		register("crazy_diamond", new ModelCrazyDiamond());
		register("huajiager:crazy_diamond", new ModelCrazyDiamond());
		// HERMIT_PURPLE 启用模型：ModelHermitPurple
		// Blockbench JSON（hermit_purple.json，64x64 UV，藤蔓缠绕悬浮造型），
		// 配 hermit_purple.png 贴图 UV 完全匹配，修复此前回落标准人形占位。
		// 注意：StandCustom 的 name 来自 JSON 的 stand 字段 = "huajiager:hermit_purple"
		// （带命名空间），pickModel 用 getName() 查表，必须同时注册带/不带命名空间两个 key，
		// 否则 getName() = "huajiager:hermit_purple" 查不到短名 key → 回落默认人形。
		register("hermit_purple", new ModelHermitPurple());
		register("huajiager:hermit_purple", new ModelHermitPurple());
		// WHITE_SNAKE 启用模型：ModelWhiteSnake
		// Blockbench JSON（white_snake.json，128x128 UV，待机悬浮造型），
		// 配 white_snake.png 贴图 UV 完全匹配，修复此前回落标准人形占位。
		// 注意：StandCustom 的 name 来自 JSON 的 stand 字段 = "huajiager:white_snake"
		// （带命名空间），pickModel 用 getName() 查表，必须同时注册带/不带命名空间两个 key。
		register("white_snake", new ModelWhiteSnake());
		register("huajiager:white_snake", new ModelWhiteSnake());
		this.theWorldIdleModel = new ModelTheWorldIdle();
		this.starPlatinumIdleModel = new ModelStarPlatinumIdle();
		this.hierophantGreenIdleModel = new ModelHierophantGreenIdle();
		this.crazyDiamondIdleModel = new ModelCrazyDiamondIdle();
		this.hermitPurpleOverdriveModel = new ModelHermitPurpleOverdrive();
		this.killerQueenPunchModel = new ModelKillerQueenPunch();
		this.orgaFlyModel = new ModelOrgaFly();
		this.whiteSnakePunchModel = new ModelWhiteSnakePunch();
	}

	private void register(String standName, HAModelBase model) {
		standModels.put(standName, model);
	}

	/**
	 * 借用表：自定义替身用 JS 的 modelId 借内置模型时，同步借用的默认贴图。
	 * key 与 {@link #register} 使用的替身名一致（短名与带命名空间两种 key 都登记）。
	 */
	private static final Map<String, Identifier> BORROW_TEXTURES = new HashMap<>();
	static {
		BORROW_TEXTURES.put(StandLoader.THE_WORLD.getName(), nativeTex(StandLoader.THE_WORLD));
		BORROW_TEXTURES.put(StandLoader.STAR_PLATINUM.getName(), nativeTex(StandLoader.STAR_PLATINUM));
		BORROW_TEXTURES.put(StandLoader.HIEROPHANT_GREEN.getName(), nativeTex(StandLoader.HIEROPHANT_GREEN));
		BORROW_TEXTURES.put(StandLoader.KILLER_QUEEN.getName(), nativeTex(StandLoader.KILLER_QUEEN));
		BORROW_TEXTURES.put(StandLoader.ORGA_REQUIEM.getName(), nativeTex(StandLoader.ORGA_REQUIEM));
		BORROW_TEXTURES.put("crazy_diamond", CRAZY_DIAMOND_TEXTURE);
		BORROW_TEXTURES.put("huajiager:crazy_diamond", CRAZY_DIAMOND_TEXTURE);
		BORROW_TEXTURES.put("hermit_purple", HERMIT_PURPLE_TEXTURE);
		BORROW_TEXTURES.put("huajiager:hermit_purple", HERMIT_PURPLE_TEXTURE);
		BORROW_TEXTURES.put("white_snake", WHITE_SNAKE_TEXTURE);
		BORROW_TEXTURES.put("huajiager:white_snake", WHITE_SNAKE_TEXTURE);
	}

	/** 原生替身的贴图路径转 Identifier（texPath 形如 textures/entity/xxx.png）。 */
	private static Identifier nativeTex(StandBase stand) {
		String path = stand == null ? null : stand.getTexPath();
		return path == null || path.isEmpty()
				? null
				: Identifier.of(org.huajiager.HuajiAgeRemastered.MOD_ID, path);
	}

	/** 当前状态：由宿主玩家的替身数据取，与其余状态判定（isIdle / isPunch / isFly）同源。 */
	private static StandStateBase currentState(EntityStandBase entity) {
		LivingEntity user = entity.getUser();
		if (user == null) {
			return null;
		}
		IExposedData data = StandUtil.getStandData(user);
		if (data == null) {
			return null;
		}
		return StandStates.getStandState(data.getStand(), data.getState()) instanceof StandStateBase base
				? base
				: null;
	}

	/**
	 * 把状态声明的 modelId 归一到 {@link #standModels} 的注册 key：
	 * 先原样查，再剔除状态后缀（加载器会把 modelId 拼成 &lt;id&gt;_&lt;stateId&gt;），
	 * 最后按去掉命名空间的 path 短名再查一遍。查不到返回 null（按默认人形/自带贴图处理）。
	 */
	/** 诊断用：已打印过的模型键，避免渲染每帧刷屏。 */
	private static final java.util.Set<String> LOGGED_MODEL_KEYS = java.util.concurrent.ConcurrentHashMap.newKeySet();

	private String resolveModelKey(String modelId) {
		if (modelId == null || modelId.isEmpty()) {
			return null;
		}
		String resolved = null;
		for (String candidate : new String[] { modelId, stripStateSuffix(modelId) }) {
			if (standModels.containsKey(candidate)) {
				resolved = candidate;
				break;
			}
		}
		if (resolved == null) {
			int colon = modelId.indexOf(':');
			String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
			for (String candidate : new String[] { path, stripStateSuffix(path) }) {
				if (standModels.containsKey(candidate)) {
					resolved = candidate;
					break;
				}
			}
		}
		// 诊断日志：确认替身模型最终命中的注册 key（null 表示落到默认人形）
		if (LOGGED_MODEL_KEYS.add(modelId + " -> " + resolved)) {
			org.slf4j.LoggerFactory.getLogger("huajiager").info(
					"[HuajiAge] stand model key: {} -> {}", modelId, resolved);
		}
		return resolved;
	}

	/** 剔除状态后缀：注册 key 都是不带状态后缀的替身名。 */
	private static String stripStateSuffix(String id) {
		for (String suffix : new String[] { "_default", "_idle", "_heal", "_punch", "_overdrive", "_fly" }) {
			if (id.endsWith(suffix)) {
				return id.substring(0, id.length() - suffix.length());
			}
		}
		return id;
	}

	/**
	 * 按替身名取模型并处理闲置态：THE_WORLD / STAR_PLATINUM 闲置时切换各自独立
	 * 抱拳盘腿闲置模型（ModelTheWorldIdle / ModelStarPlatinumIdle），
	 * 其余替身无独立闲置模型时回落其 default 模型（power=0 收手）。
	 */
	private HAModelBase pickModel(EntityStandBase entity) {
		StandBase s = entity.getStand();
		if (s != null) {
			// ORGA_REQUIEM 飞行态（state 含 "fly" extraData）：切躺平飞行模型
			// ModelOrgaFly（身体绕 X 躺平 + 头部后仰 + 飘带绕 Z 旋转），
			// 与 default 直立悬浮造型区分——"姿势没变"的修复点。
			if (isFly(entity) && StandLoader.ORGA_REQUIEM.getName().equals(s.getName())) {
				return orgaFlyModel;
			}
			if (isIdle(entity) && StandLoader.THE_WORLD.getName().equals(s.getName())) {
				return theWorldIdleModel;
			}
			if (isIdle(entity) && StandLoader.STAR_PLATINUM.getName().equals(s.getName())) {
				return starPlatinumIdleModel;
			}
			if (isIdle(entity) && StandLoader.HIEROPHANT_GREEN.getName().equals(s.getName())) {
				return hierophantGreenIdleModel;
			}
			if (isIdle(entity) && isCrazyDiamond(s)) {
				return crazyDiamondIdleModel;
			}
			// HERMIT_PURPLE 爆发态（OVERDRIVE）切藤蔓缠绕模型 ModelHermitPurpleOverdrive
			if (isOverdrive(entity) && isHermitPurple(s)) {
				return hermitPurpleOverdriveModel;
			}
			// KILLER_QUEEN 攻击态（PUNCH）切十指挥拳模型；待机/闲置态用 default 待机造型
			if (isPunch(entity) && StandLoader.KILLER_QUEEN.getName().equals(s.getName())) {
				return killerQueenPunchModel;
			}
			// WHITE_SNAKE 攻击态（PUNCH）切挥拳模型；待机/闲置态用 default 待机造型
			if (isPunch(entity) && isWhiteSnake(s)) {
				return whiteSnakePunchModel;
			}
			HAModelBase m = standModels.get(s.getName());
			if (m != null) {
				return m;
			}
			// 自定义替身：名字查不到时按状态声明的 modelId 借内置模型
			// （JS 里写 modelId: "huajiager:crazy_diamond" 即可复用该模型与贴图）。
			StandStateBase stateBase = currentState(entity);
			String modelKey = stateBase == null ? null : resolveModelKey(stateBase.getModelID());
			if (modelKey != null) {
				HAModelBase byId = standModels.get(modelKey);
				if (byId != null) {
					return byId;
				}
			}
			// 仍取不到时问资源包声明的模型（stand_model.json + models/entity/*.json）：
			// 第三方替身在这里拿到自己的几何造型。
			if (stateBase != null) {
				org.huajiager.client.model.custom.RuntimeStandModel custom =
						org.huajiager.client.model.custom.CustomModelLoader.find(stateBase.getModelID());
				if (custom != null) {
					return custom;
				}
				// 再问原版实体模型借用表（modelId 写 minecraft:warden 这类）
				org.huajiager.client.model.custom.VanillaStandModel vanilla =
						org.huajiager.client.model.custom.VanillaStandModels.find(stateBase.getModelID());
				if (vanilla != null) {
					return vanilla;
				}
			}
		}
		return defaultModel;
	}

	/**
	 * 绿法皇攻击态水花：位置取翡翠弹的发射点（玩家 + 相对点(-0.55,-0.6)、高度 +2.2，
	 * 与 StateHierophantGreenDefault 发弹处同口径），每帧 3 颗、速度沿玩家朝向一半
	 * 再叠随机抖动；用重力为 0 的绿色水花粒子，因此是往外喷而不是往下掉。
	 * 另有约 1/10 概率补一颗白烟。开启 useHuajiSplash 时换成滑稽粒子。
	 */
	private void spawnHierophantSplash(EntityStandBase entity, boolean idle) {
		StandBase stand = entity.getStand();
		if (idle || stand == null || !StandLoader.HIEROPHANT_GREEN.getName().equals(stand.getName())) {
			return;
		}
		LivingEntity user = entity.getUser();
		if (user == null || user.getWorld() == null) {
			return;
		}
		Vec3d shootPoint = HAMathHelper.getPostionRelative2D(user, -0.55f, -0.6f);
		double px = user.getX() + shootPoint.x;
		double py = user.getY() + 2.2;
		double pz = user.getZ() + shootPoint.z;
		Vec3d forward = user.getRotationVector();
		float rf1 = user.getWorld().random.nextFloat() * 2.0f - 1.0f;
		float rf2 = user.getWorld().random.nextFloat() * 2.0f - 1.0f;
		float rf3 = user.getWorld().random.nextFloat() * 2.0f - 1.0f;
		double vx = forward.x / 2.0 + rf1 / 5.0;
		double vy = forward.y / 2.0 + rf2 / 5.0;
		double vz = forward.z / 2.0 + rf3 / 5.0;
		ParticleEffect splash = ConfigHuaji.Stands.useHuajiSplash
				? ParticleLoader.HUAJI_SPLASH
				: ParticleLoader.EMERALD_SPLASH;
		for (int i = 0; i < 3; i++) {
			user.getWorld().addParticle(splash, px, py, pz, vx, vy, vz);
		}
		if (rf1 > 0.9f) {
			user.getWorld().addParticle(ParticleTypes.POOF, px, py, pz, vx, vy, vz);
		}
	}

	/**
	 * 兜底：本替身紧贴玩家（攻击态恒在正前方1格、拳头又上浮到眼睛高度），默认按可见盒
	 * 做视锥剔除，行走/飞行的视角晃动会让极小可见盒频繁进出视锥，整颗实体一帧帧闪没。
	 * 主修复在 EntityStandBase.getVisibilityBoundingBox() 已放大可见盒，此处再显式不剔除。
	 *
	 * <p>替身可见性规则（仅当本机玩家拥有替身时才渲染替身模型）：
	 * <ul>
	 *   <li>白蛇替身例外：对所有玩家可见（含无替身者）；</li>
	 *   <li>本机玩家未觉醒替身（STAND_DATA 为空/未同步）→ 不渲染其他玩家的替身实体；</li>
	 *   <li>本机玩家拥有替身 → 照常渲染（含自己的替身与其他玩家的替身）。</li>
	 * </ul>
	 */
	@Override
	public boolean shouldRender(EntityStandBase entity, Frustum frustum, double camX, double camY, double camZ) {
		// 白蛇替身例外：无替身玩家也能看见白蛇替身
		StandBase stand = entity.getStand();
		if (stand != null && isWhiteSnake(stand)) {
			return true;
		}
		// 无替身玩家看不到其他玩家的替身实体
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null || mc.player == null) {
			return true;
		}
		IExposedData data = StandUtil.getStandData(mc.player);
		if (data == null || data.getStand() == null || data.getStand().isEmpty()
				|| data.getStand().equals(StandLoader.EMPTY)) {
			return false;
		}
		return true;
	}

	@Override
	public void render(EntityStandBase entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vcp, int light) {
		matrices.push();
		// 女仆替身：借用车万自己的女仆渲染器（模型与皮肤都取女仆本人），
		// 取不到车万时继续走下面自带几何/贴图的流程
		if (org.huajiager.client.compat.tlm.MaidStandRenderer.renderStand(entity, yaw, tickDelta, matrices, vcp, light)) {
			matrices.pop();
			return;
		}
		boolean idle = isIdle(entity);
		spawnHierophantSplash(entity, idle);
		// 攻击态 + 所属玩家本机第一人称：替身本体不可见，只渲染双手挥拳
		// （对齐 ModelTheWorld.renderFirst —— 第一人称下玩家只见替身的拳头）。
		// 实体本身位于玩家前方（攻击态按实体逻辑置于正前方同高），第一人称视野
		// 恰好呈现前方舞动的拳头、本体不遮挡画面。闲置态替身在背后，第一人称
		// 本来不可见，无需特殊处理。
		boolean handsOnly = !idle && !isFly(entity) && isFirstPersonOwner(entity);
		// 绿法皇例外：闲置态位置在玩家左前方（0.9 前 / 0.8 左 / 高 0.75），
		// 第一人称会直接看到本体；白金之星/世界闲置都在背后所以天然不可见。
		// 故绿法皇第一人称一律隐藏本体（renderHandsStand 为空 = 完全不可见，
		// 仅保留翡翠弹幕特效），与白金之星/世界观感一致。
		if (isFirstPersonOwner(entity)) {
			StandBase s = entity.getStand();
			if (s != null && StandLoader.HIEROPHANT_GREEN.getName().equals(s.getName())) {
				handsOnly = true;
			}
		}
		// —— 实时贴人（消灭跑步/飞行"跟不上、看不到拳头"）——
		// 实体位置由服务端每 tick 挪到玩家相对点，经网络同步+插值至少有 1~2 tick 滞后：
		// 静止察觉不出，跑步/飞行时替身就以可感知的延迟飘在身后，第一人称攻击态的拳头
		// 也滞后、跟不上挥拳节奏。这里在渲染期用宿主玩家本帧位置与实时 yaw 重算贴附点，
		// 通过平移把矩阵从"实体网络位置"修正到"玩家实时相对位"，本体与拳头完全跟手。		// 朝向同样优先取玩家实时 yaw（实体 yaw 与位置同源滞后）。偏移口径与
		// EntityStandBase.tick 一致：闲置=白金正背后1格+高0.5，其它背后1格+右偏0.5+高0.3；攻击=正前方1格同高。
		LivingEntity tieUser = entity.getUser();
		float renderYaw = entity.getYaw();
		if (tieUser != null) {
			double yawRad = Math.toRadians(tieUser.getYaw());
			double nx = -Math.sin(yawRad);
			double nz = Math.cos(yawRad);
			// 一律取 tickDelta 插值坐标（而非本 tick 离散 getX/getY/getZ）：
			// 本地玩家/网络实体的裸坐标每 20 tick 步进一次，直接用作修正量会让替身
			// 以 20Hz 频率一帧帧跳格，走路/飞行观感"一卡一卡"；用插值坐标才是每帧
			// 连续的轨迹，替身完全平滑贴合玩家。
			Vec3d tiePos = tieUser.getLerpedPos(tickDelta);
			Vec3d entPos = entity.getLerpedPos(tickDelta);
			double ox;
			double oz;
			double oy;
			if (idle) {
				StandBase s = entity.getStand();
				boolean isStarPlatinum = s != null
						&& StandLoader.STAR_PLATINUM.getName().equals(s.getName());
				boolean isHierophantGreen = s != null
						&& StandLoader.HIEROPHANT_GREEN.getName().equals(s.getName());
				boolean isOrgaRequiem = s != null
						&& StandLoader.ORGA_REQUIEM.getName().equals(s.getName());
				if (isOrgaRequiem) {
					// ORGA_REQUIEM 闲置态（ModelOrgaRequiem.=(-0.5,-0.7,0.75)：
					// 右侧 0.5、背后 0.75、高 0.7）——替身飘在玩家右后方偏高
					final double OFF_BACK = 0.75D;
					final double OFF_LEFT = -0.5D;
					ox = tiePos.x - nx * OFF_BACK + nz * OFF_LEFT;
					oz = tiePos.z - nz * OFF_BACK - nx * OFF_LEFT;
					oy = tiePos.y + 0.7D;
				} else if (isHierophantGreen) {
					// 绿法皇闲置态（ModelHierophantGreenIdle.=(0.8,-0.75,-0.7)：
					// 左侧 0.8、前方 0.9、高 0.75）——闲置飘在玩家左前方（前方按用户实测微调 0.7→0.9）
					final double OFF_FRONT = 0.9D;
					final double OFF_LEFT = 0.8D;
					ox = tiePos.x + nx * OFF_FRONT + nz * OFF_LEFT;
					oz = tiePos.z + nz * OFF_FRONT - nx * OFF_LEFT;
					oy = tiePos.y + 0.75D;
				} else if (isCrazyDiamond(s)) {
					// 疯狂钻石闲置态（用户要求）：玩家背后 1 格、高 1 格、从后看往右 0.3 格
					// （OFF_LEFT 负值 = 玩家右侧，即背后视角的右侧）
					final double OFF_BACK = 1.0D;
					final double OFF_LEFT = -0.3D;
					ox = tiePos.x - nx * OFF_BACK + nz * OFF_LEFT;
					oz = tiePos.z - nz * OFF_BACK - nx * OFF_LEFT;
					oy = tiePos.y + 1.0D;
				} else if (s != null && isHermitPurple(s)) {
					// 隐者之紫缠绕型：
					// 贴附玩家位置，模型坐标自带 body y=24px（1.5 格）定位缠绕身体
					ox = tiePos.x;
					oz = tiePos.z;
					oy = tiePos.y;
				} else if (isStarPlatinum) {
					// 白金之星（用户要求）：玩家正背后 1 格、高 0.5 格
					ox = tiePos.x - nx * 1.0D;
					oz = tiePos.z - nz * 1.0D;
					oy = tiePos.y + 0.5D;
				} else {
					// 其它替身（含 THE_WORLD）：保持原状——背后 1 格 + 右偏 0.5 格 + 高 0.3 格
					final double OFF_LEFT = -0.5D;
					ox = tiePos.x - nx * 1.0D + nz * OFF_LEFT;
					oz = tiePos.z - nz * 1.0D - nx * OFF_LEFT;
					oy = tiePos.y + 0.3D;
				}
			} else {
				StandBase s = entity.getStand();
				boolean isHierophantGreen = s != null
						&& StandLoader.HIEROPHANT_GREEN.getName().equals(s.getName());
				boolean isKillerQueen = s != null
						&& StandLoader.KILLER_QUEEN.getName().equals(s.getName());
				boolean isStarPlatinum = s != null
						&& StandLoader.STAR_PLATINUM.getName().equals(s.getName());
				boolean isOrgaRequiem = s != null
						&& StandLoader.ORGA_REQUIEM.getName().equals(s.getName());
				if (isOrgaRequiem && isFly(entity)) {
					// ORGA_REQUIEM 飞行态（ ModelOrgaFly.=(0,-0.9,0)）：
					// 水平居中、高 0.9——飞行姿态不左右偏，替身躺平悬浮在玩家正上方偏后。
					// 第一人称下压 0.5 格（用户要求）：飞行本体不再悬浮过高、贴近玩家身体。
					oy = tiePos.y + 0.9D;
					if (isFirstPersonOwner(entity)) {
						oy -= 0.5D;
					}
					ox = tiePos.x;
					oz = tiePos.z;
				} else if (isOrgaRequiem) {
					// ORGA_REQUIEM 攻击态（ ModelOrgaRequiem.setPunch 为空、位置不变）：
					// 仍按 =(-0.5,-0.7,0.75) 飘在玩家右后方偏高——远程替身本体不近身，
					// 第一人称也不会看到本体贴脸。
					final double OFF_BACK = 0.75D;
					final double OFF_LEFT = -0.5D;
					ox = tiePos.x - nx * OFF_BACK + nz * OFF_LEFT;
					oz = tiePos.z - nz * OFF_BACK - nx * OFF_LEFT;
					oy = tiePos.y + 0.7D;
				} else if (isHierophantGreen) {
					// 绿法皇攻击态（ModelHierophantGreen.=(0.5,-1.0,0.75)：
					// 左侧 0.5、背后 0.75、高 1.0）——与世界攻击态相反，绿法皇攻击时
					// 飘在玩家背后左上方。
					final double OFF_BACK = 0.75D;
					final double OFF_LEFT = 0.5D;
					ox = tiePos.x - nx * OFF_BACK + nz * OFF_LEFT;
					oz = tiePos.z - nz * OFF_BACK - nx * OFF_LEFT;
					oy = tiePos.y + 1.0D;
				} else if (isKillerQueen && !isPunch(entity)) {
					// KQ 待机态（ModelKillerQueen.=(0.9,-0.1,-0.8)：
					// 左侧 0.9、前方 0.8、高 0.1）——按数值映射为实体偏移，不再沉底
					final double OFF_FRONT = 0.8D;
					final double OFF_LEFT = 0.9D;
					ox = tiePos.x + nx * OFF_FRONT + nz * OFF_LEFT;
					oz = tiePos.z + nz * OFF_FRONT - nx * OFF_LEFT;
					oy = tiePos.y + 0.4D;
				} else if (isKillerQueen) {
					// KQ 攻击态（ModelKillerQueenPunch.=(0,0,-0.9)：
					// 正前方 0.9、与玩家同高）——十指挥拳正前方贴身，位置调高 0.3 格（用户要求）
					final double OFF_FRONT = 0.9D;
					ox = tiePos.x + nx * OFF_FRONT;
					oz = tiePos.z + nz * OFF_FRONT;
					oy = tiePos.y + 0.3D;
				} else if (s != null && isHermitPurple(s)) {
					// 隐者之紫缠绕型：
					// 贴附玩家位置，Overdrive 切换只换模型与闪电速度，位置不变
					ox = tiePos.x;
					oz = tiePos.z;
					oy = tiePos.y;
				} else if (isCrazyDiamond(s)) {
					// 疯狂钻石攻击/治疗态：正前方 0.7 格，高度比玩家高 0.3 格（用户实测）
					final double OFF_FRONT = 0.7D;
					ox = tiePos.x + nx * OFF_FRONT;
					oz = tiePos.z + nz * OFF_FRONT;
					oy = tiePos.y + 0.3D;
				} else if (isStarPlatinum) {
					// 白金之星攻击态：正前方 0.7 格，位置调高 0.3 格（用户要求）
					final double OFF_FRONT = 0.7D;
					ox = tiePos.x + nx * OFF_FRONT;
					oz = tiePos.z + nz * OFF_FRONT;
					oy = tiePos.y + 0.3D;
				} else {
					final double OFF_FRONT = 0.7D; // 攻击态正前方 0.7 格（用户要求由 1 格缩近）
					ox = tiePos.x + nx * OFF_FRONT;
					oz = tiePos.z + nz * OFF_FRONT;
					oy = tiePos.y;
				}
			}
			matrices.translate((float) (ox - entPos.x), (float) (oy - entPos.y),
					(float) (oz - entPos.z));
			renderYaw = tieUser.getYaw();
		}
		// 实体渲染矩阵（大类同 LivingEntityRenderer）：
		//   1) translate 必须放在 scale(-1,-1,1) 之前——平移量会随 y 翻转变号，
		//      旧实现写在 scale 之后等效于把模型压到脚底以下 H 格（"始终在脚下"的根源）。
		//      此处 H 为正上移量：模型头部盒位于模型空间 -0.375..0，加 H 后头顶 ≈ H..H+0.375 格。
		//      H=1.2 时头约在玩家胸口~肩部、盘腿底贴近地面微浮；可微调（0.2=贴脚踝、1.5=更高）。
		//      第一人称攻击态（handsOnly）：实体与玩家同高（y=脚底），拳头手腕锚点约在
		//      脚底上方 1.2+5/16≈1.51 格（略低于准星/眼睛 1.62），额外抬高 0.3 格使拳头
		//      大体与准星齐平（HAND_FIRST_PERSON_LIFT=1.5）。
		float lift = handsOnly ? HAND_FIRST_PERSON_LIFT : STAND_FLOAT_HEIGHT;
		// 白金之星闲置态额外补高（仅白金）：实体已比玩家高 0.5 格，渲染悬浮再补 0.3，
		// 使替身整体明显高出玩家约半格；其它替身保持原悬浮高度。
		if (idle) {
			StandBase s = entity.getStand();
			if (s != null && StandLoader.STAR_PLATINUM.getName().equals(s.getName())) {
				lift = STAND_FLOAT_HEIGHT + 0.3F;
			}
		}
		// ORGA_REQUIEM 飞行态：ModelOrgaFly 根 pivot Y=22。注意 scale(-1,-1,1) 只换坐标
		// 符号约定、物理方向不变——模型空间向下 22px 在渲染空间仍是向下 22/16≈1.375 格，
		// 旋转中心会被压到玩家脚底下方（旧值 1.2-22/16≈-0.175 → 旋转中心 -0.65 格 → 遁地）。
		// 正确做法 lift 取 +22/16，把旋转中心抬回贴人锚点（=玩家脚底上方 oy≈0.9 格）处，
		// 躺平姿态恰好锚在玩家身体高度，不遁地。
		if (isFly(entity)) {
			lift = 22.0F / 16.0F;
		}
		// HERMIT_PURPLE 缠绕型：模型坐标按 convertPivot(y=24-pivot.y) 生成，藤蔓主体骨骼
		// （mb1~mb4 及 frames/手臂）的 MC 模型空间 y 落在 -0.5~7 之间，经 scale(-1,-1,1) 翻转后
		// 整体渲染在贴附点(玩家脚底)附近。 Y-up 中藤蔓位于模型原点上方 24.5px≈1.53 格，
		// 因此本类替身需额外抬升 24px(=1.5 格) 才能回到身体缠绕位置；直立替身由 lift 承担高度，
		// 此处 lift 兼作该 24px 修正，保持 Layer 行为。
		{
			StandBase s = entity.getStand();
			if (s != null && isHermitPurple(s)) {
				lift = 1.5F;
			}
		}
		matrices.translate(0.0f, lift, 0.0f);
		//   2) rotationYaw 使模型朝向 yaw（ 180-yaw 写法）
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - renderYaw));
		//   3) scale(-1,-1,1) 把 Y 向下的模型坐标翻转为正立
		matrices.scale(-1f, -1f, 1f);

		// 第一人称飞行态：渲染层换 translucent 并强制覆盖 alpha=0.5（ renderFirst 的
		// 幽灵半透明效果；cutout 层忽略顶点 alpha，必须走混合层才透明）。
		boolean flyFirstPerson = isFly(entity) && isFirstPersonOwner(entity);
		VertexConsumer vc = vcp.getBuffer(flyFirstPerson
				? RenderLayer.getEntityTranslucent(getTexture(entity))
				: RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
		if (flyFirstPerson) {
			vc = new AlphaOverrideVertexConsumer(vc, 0.5F);
		}
		HAModelBase model = pickModel(entity);
		if (model instanceof StandAnimatedModel anim) {
			StandBase s = entity.getStand();
			float speed = s != null ? s.getSpeed() : StandLoader.THE_WORLD.getSpeed();
			// power 语义：闲置=0（ModelTheWorldIdle 抱胸盘腿，忽略 power），
			// 攻击态=1（ModelTheWorld 张开 + 十二连挥拳 + setPunch 前冲抖动）。
			float power = idle ? 0.0F : 1.0F;
			// 第一人称攻击态拳头半透明：cutout 层忽略顶点 alpha，必须换 translucent
			// 混合层（与白金闲置残影 renderHandsFade 同源问题）；alpha 对齐 Res：
			// THE_WORLD / STAR_PLATINUM / KILLER_QUEEN 的 renderFirst 均传 0.3f。
			VertexConsumer handVc = handsOnly
					? vcp.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity)))
					: null;
			final float handAlpha = 0.3F;
			if (handsOnly && model instanceof ModelTheWorld world) {
				world.renderHandsStand(matrices, handVc, light, OverlayTexture.DEFAULT_UV, entity,
						(float) entity.age, speed, power, handAlpha);
			} else if (handsOnly && model instanceof ModelStarPlatinum starPlatinum) {
				starPlatinum.renderHandsStand(matrices, handVc, light, OverlayTexture.DEFAULT_UV, entity,
						(float) entity.age, speed, power, handAlpha);
			} else if (handsOnly && model instanceof ModelKillerQueenPunch kqPunch) {
				// KQ 攻击态 renderFirst 只渲双手：第一人称只见前方舞动的十指挥拳
				kqPunch.renderHandsStand(matrices, handVc, light, OverlayTexture.DEFAULT_UV, entity,
						(float) entity.age, speed, power, handAlpha);
			} else if (handsOnly && (model instanceof ModelHierophantGreen || model instanceof ModelHierophantGreenIdle)) {
				// 绿法皇 renderFirst 为空：第一人称攻击态不渲染本体（只有弹幕特效），
				// 避免本体贴脸遮挡视野。闲置态模型（ModelHierophantGreenIdle 无
				// renderHandsStand）同样不渲染——绿法皇第一人称一律隐藏本体，
				// 与白金之星/世界观感一致。
				// 空实现：完全不渲染任何部件。
			} else if (handsOnly && model instanceof ModelKillerQueen) {
				// KQ 待机态 renderFirst 为空：第一人称不渲染本体（待机位于玩家
				// 左前方 0.9 会直接入视野），与绿法皇同口径，仅保留攻击态十指挥拳可见。
				// 空实现：完全不渲染任何部件。
			} else if (handsOnly && model instanceof ModelOrgaRequiem) {
				// ORGA_REQUIEM  renderFirst 为空：第一人称不渲染本体（远程替身本体
				// 本就飘在玩家右后方，且第一人称攻击态不应出现贴脸本体）。
				// 空实现：完全不渲染任何部件。
			} else if (handsOnly && model instanceof ModelOrgaFly) {
				// ORGA_REQUIEM 飞行态 renderFirst 为空：第一人称不渲染本体
				// （飞行姿态替身贴身悬浮，第一人称会直接遮挡视野）。
				// 空实现：完全不渲染任何部件。
			} else if (handsOnly && model instanceof ModelCrazyDiamond crazyDiamond) {
				// 疯狂钻石攻击/治疗态 renderFirst 只渲拳头环（viewFirst 子树）：
				// 第一人称只见前方舞动的拳头、本体隐藏，与世界/白金之星式拳头一致。
				crazyDiamond.renderHandsStand(matrices, handVc, light, OverlayTexture.DEFAULT_UV, entity,
						(float) entity.age, speed, power, handAlpha);
				// 治疗态红雾罩：拳头周围粉红半透明罩（专用纯白贴图 translucent 混合层）
				if ("heal".equals(entity.getStandStateName())) {
					// no-cull：第一人称视角在罩子内部，需渲染内表面
					VertexConsumer haloVc = vcp.getBuffer(CustomRenderLayers.entityTranslucentNoCull(CRAZY_DIAMOND_HALO_TEXTURE));
					crazyDiamond.renderHalo(matrices, haloVc, light, OverlayTexture.DEFAULT_UV, 0.35F);
				}
			} else if (handsOnly && (model instanceof ModelHermitPurple || model instanceof ModelHermitPurpleOverdrive)) {
				// 隐者之紫 renderFirst 为空：第一人称不渲染本体（远程替身本体
				// 位于玩家正前方会直接遮挡视野，藤蔓攻击特效独立呈现）。
				// 空实现：完全不渲染任何部件。
			} else if (handsOnly && model instanceof ModelWhiteSnakePunch wsPunch) {
				// 白蛇连击态 viewFirst 含六拳拳头环：第一人称只渲染半透明拳头环，
				// 本体隐藏不贴脸，与疯狂钻石/白金之星同款手法。
				wsPunch.renderHandsStand(matrices, handVc, light, OverlayTexture.DEFAULT_UV, entity,
						(float) entity.age, speed, power, handAlpha);
			} else if (handsOnly && model instanceof ModelWhiteSnake) {
				// 白蛇默认态 viewFirst 仅空节点（无拳头 cube）：第一人称隐藏本体。
				// 空实现：完全不渲染任何部件。
			} else {
				// ORGA_REQUIEM 飞行态第一人称： renderFirst 额外绕 X 前倾 35 度，
				// 把躺平贴身的替身转到玩家前方视野（否则横躺在脚下看不到）。
				// 方向若与预期相反，将 -35.0F 改为 +35.0F 即可。
				if (isFly(entity) && isFirstPersonOwner(entity)) {
					matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35.0F));
				}
				anim.renderStand(matrices, vc, light, OverlayTexture.DEFAULT_UV, entity, (float) entity.age, speed, power);
				// 疯狂钻石治疗态：第三人称补 translucent 红雾罩（主渲染 vc 走 cutout
				// 层不混合 alpha，必须另开混合层，否则红雾会是不透明方块）。
				// alpha 0.30→0.60：用户反馈第三人称粉色太透明看不清，翻倍提亮。
				if (!idle && model instanceof ModelCrazyDiamond crazyDiamond3
						&& "heal".equals(entity.getStandStateName())) {
					VertexConsumer haloVc = vcp.getBuffer(CustomRenderLayers.entityTranslucentNoCull(CRAZY_DIAMOND_HALO_TEXTURE));
					crazyDiamond3.renderHalo(matrices, haloVc, light, OverlayTexture.DEFAULT_UV, 0.60F);
				}
				// 白金闲置态双拳残影：必须用 translucent 混合层单独绘制才能让 alpha 生效
				// （半透明淡入淡出）；cutout 层不混合 alpha，残影会表现为不透明常驻。
				if (idle && model instanceof ModelStarPlatinumIdle idleModel) {
					idleModel.renderHandsFade(matrices, vcp, light, OverlayTexture.DEFAULT_UV,
							(float) entity.age, STAR_PLATINUM_IDLE_TEXTURE);
				}
			}
		} else {
			model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
		}
		matrices.pop();
	}

	/**
	 * 判断该实体是否属于本机玩家，且本机玩家当前处于第一人称视角。
	 * 仅当同时满足时才隐藏替身本体、只渲染双手（避免影响其他联机玩家的视角）。
	 */
	private boolean isFirstPersonOwner(EntityStandBase entity) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null || mc.player == null) {
			return false;
		}
		LivingEntity user = entity.getUser();
		if (user == null || user != mc.player) {
			return false;
		}
		return mc.options.getPerspective().isFirstPerson();
	}

	/** 悬浮高度（格），translate 置于 scale 之前为正上移量。见 render() 注释说明。 */
	private static final float STAND_FLOAT_HEIGHT = 1.2F;
	/**
	 * 第一人称攻击态拳头上浮高度（格）：比常态悬浮高 0.3，使拳头手腕锚点
	 * （≈0.3 + 5/16 格）接近玩家准星/眼睛高度（脚底上方 1.62）。
	 */
	private static final float HAND_FIRST_PERSON_LIFT = 1.5F;

	/**
	 * 判定替身当前是否处于闲置态：从替身实体 DataTracker 读取状态名（服务端每 tick
	 * 写入并自动广播，其他玩家客户端也能拿到宿主实时状态；此前读宿主玩家 STAND_DATA
	 * attachment，该数据在他人客户端从未同步过 → 其他玩家永远看到攻击态模型，即 bug3）。
	 */
	private boolean isIdle(EntityStandBase entity) {
		return ExposedData.States.IDLE.getName().equals(entity.getStandStateName());
	}

	/**
	 * 判断是否为疯狂钻石替身：自定义替身 StandCustom 的 name 带命名空间
	 * （"huajiager:crazy_diamond"），与渲染 Map 注册的双 key 兼容判断。
	 */
	private boolean isCrazyDiamond(StandBase s) {
		if (s == null) {
			return false;
		}
		String n = s.getName();
		return "crazy_diamond".equals(n) || "huajiager:crazy_diamond".equals(n);
	}

	/**
	 * 判断是否为隐者之紫替身：自定义替身 StandCustom 的 name 带命名空间
	 * （"huajiager:hermit_purple"），与渲染 Map 注册的双 key 兼容判断。
	 */
	private boolean isHermitPurple(StandBase s) {
		if (s == null) {
			return false;
		}
		String n = s.getName();
		return "hermit_purple".equals(n) || "huajiager:hermit_purple".equals(n);
	}

	/**
	 * 判断是否为白蛇替身：自定义替身 StandCustom 的 name 带命名空间
	 * （"huajiager:white_snake"），与渲染 Map 注册的双 key 兼容判断。
	 */
	private boolean isWhiteSnake(StandBase s) {
		if (s == null) {
			return false;
		}
		String n = s.getName();
		return "white_snake".equals(n) || "huajiager:white_snake".equals(n);
	}

	/**
	 * 判定替身当前是否处于爆发态（OVERDRIVE）：从替身实体 DataTracker 读取状态名。
	 * HERMIT_PURPLE 的 OVERDRIVE 态切 ModelHermitPurpleOverdrive 藤蔓缠绕模型 + 专属贴图。
	 */
	private boolean isOverdrive(EntityStandBase entity) {
		return "overdrive".equals(entity.getStandStateName());
	}

	/**
	 * 判定替身当前是否处于攻击态（PUNCH）：从替身实体 DataTracker 读取状态名。
	 * KILLER_QUEEN 的 PUNCH 态切 ModelKillerQueenPunch 十指挥拳模型 + 专属贴图。
	 */
	private boolean isPunch(EntityStandBase entity) {
		return ExposedData.States.PUNCH.getName().equals(entity.getStandStateName());
	}

	/**
	 * 判定替身当前是否处于飞行态（fly）：从替身实体 DataTracker 读取当前状态名，
	 * 若该状态的 StandStateBase 含 "fly" extraData（StateOrgaRequiemFly 注册时
	 * addExtraData("fly")）即视为飞行态。ORGA_REQUIEM 飞行态切 ModelOrgaFly
	 * 躺平飞行模型 + 专属贴图（entity_orga_requiem_fly.png）。
	 */
	private boolean isFly(EntityStandBase entity) {
		StandBase s = entity.getStand();
		if (s == null) {
			return false;
		}
		Object state = StandStates.getStandState(s.getName(), entity.getStandStateName());
		return state instanceof StandStateBase stateBase && stateBase.hasExtraData("fly");
	}

	@Override
	public Identifier getTexture(EntityStandBase entity) {
		// 女仆替身：用车万女仆本人那张皮肤（我们自带的 maid 贴图只是 Blockbench UV 模板）
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("touhou_little_maid")) {
			StandBase s = entity.getStand();
			if (s != null && org.huajiager.compat.tlm.MaidBallHelper.MAID_STAND.equals(s.getName())) {
				org.huajiager.capability.IExposedData data =
						org.huajiager.stand.StandUtil.getStandData(entity.getUser());
				Identifier skin = org.huajiager.client.compat.tlm.MaidSkinTextures
						.resolve(data == null ? null : data.getModel());
				if (skin != null) {
					return skin;
				}
			}
		}
		// ORGA_REQUIEM 飞行态（fly）用 ModelOrgaFly 专属贴图（64x64  UV）
		if (isFly(entity)) {
			StandBase s = entity.getStand();
			if (s != null && StandLoader.ORGA_REQUIEM.getName().equals(s.getName())) {
				return ORGA_REQUIEM_FLY_TEXTURE;
			}
		}
		// KILLER_QUEEN 攻击态（PUNCH）用十指挥拳模型专属贴图（128x128 Blockbench UV）
		if (isPunch(entity)) {
			StandBase s = entity.getStand();
			if (s != null && StandLoader.KILLER_QUEEN.getName().equals(s.getName())) {
				return KILLER_QUEEN_PUNCH_TEXTURE;
			}
		}
		// THE_WORLD / STAR_PLATINUM 闲置态用各自独立待机贴图（抱拳盘腿模型专属 UV）
		if (isIdle(entity)) {
			StandBase s = entity.getStand();
			if (s != null && StandLoader.THE_WORLD.getName().equals(s.getName())) {
				return THE_WORLD_IDLE_TEXTURE;
			}
			if (s != null && StandLoader.STAR_PLATINUM.getName().equals(s.getName())) {
				return STAR_PLATINUM_IDLE_TEXTURE;
			}
			if (s != null && StandLoader.HIEROPHANT_GREEN.getName().equals(s.getName())) {
				return HIEROPHANT_GREEN_IDLE_TEXTURE;
			}
			if (s != null && isCrazyDiamond(s)) {
				return CRAZY_DIAMOND_IDLE_TEXTURE;
			}
		}
		// CRAZY_DIAMOND（自定义替身）固定用 128x128 贴图。
		// StandCustom 未设置 texPath（getTexPath 返回 null），若不在此返回专属贴图，
		// 会一路兜底到 FALLBACK_TEXTURE（THE_WORLD 贴图）→ 贴图错乱。
		StandBase stand = entity.getStand();
		if (stand != null && ("crazy_diamond".equals(stand.getName())
				|| "huajiager:crazy_diamond".equals(stand.getName()))) {
			return CRAZY_DIAMOND_TEXTURE;
		}
		// HERMIT_PURPLE（自定义替身）固定贴图：default 态 hermit_purple.png，
		// 爆发态（OVERDRIVE）用 hermit_purple_overdrive.png（64x64 Blockbench UV）。
		if (stand != null && isHermitPurple(stand)) {
			return isOverdrive(entity) ? HERMIT_PURPLE_OVERDRIVE_TEXTURE : HERMIT_PURPLE_TEXTURE;
		}
		// WHITE_SNAKE（自定义替身）固定贴图：default 态 white_snake.png，
		// 攻击态（PUNCH）用 white_snake_punch.png（128x128 Blockbench UV）。
		if (stand != null && isWhiteSnake(stand)) {
			return isPunch(entity) ? WHITE_SNAKE_PUNCH_TEXTURE : WHITE_SNAKE_TEXTURE;
		}
		if (stand != null && stand.getTexPath() != null && !stand.getTexPath().isEmpty()) {
			return Identifier.of("huajiager", stand.getTexPath());
		}
		// 自定义替身（没有 texPath）：按状态声明的 modelId 取贴图——
		// 命中内置模型时同步借用该模型的贴图；否则用状态推导的路径
		// （StandStateCustom.getTex()：textures/entity/<modelId 的 path>.png，去掉 _default 后缀），
		// 于是资源包可以给自定义替身放自己的贴图。
		StandStateBase stateBase = currentState(entity);
		if (stateBase != null) {
			String modelKey = resolveModelKey(stateBase.getModelID());
			Identifier borrowed = modelKey == null ? null : BORROW_TEXTURES.get(modelKey);
			if (borrowed != null) {
				return borrowed;
			}
			Identifier own = stateBase.getTex();
			Identifier vanillaTex = org.huajiager.client.model.custom.VanillaStandModels
					.texture(stateBase.getModelID());
			if (vanillaTex != null) {
				return vanillaTex;
			}
			if (own != null) {
				return own;
			}
		}
		return FALLBACK_TEXTURE;
	}
}
