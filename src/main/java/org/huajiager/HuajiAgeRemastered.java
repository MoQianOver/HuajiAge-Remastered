package org.huajiager;

import javax.script.ScriptEngine;

import org.huajiager.api.HuajiAgeAPI;
import org.huajiager.api.HuajiAgeAPIImpl;
import org.huajiager.util.JsEngineHelper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuajiAgeRemastered implements ModInitializer {
	public static final String MOD_ID = "huajiager";
	public static final String NAME = "HUAJI Age Remastered";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public HuajiAgeRemastered() {
		// 原 HuajiAge 构造函数：注册 API 实现
		HuajiAgeAPI.setInstance(new HuajiAgeAPIImpl());
	}

	@Override
	public void onInitialize() {
		String version = FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");

		// Nashorn -> GraalJS 引擎验证（替身脚本运行时依赖）
		ScriptEngine engine = JsEngineHelper.ENGINE;

		// 替身注册链路（StandLoader 构造即登记 5 站并 reloadStands）
		new org.huajiager.init.loaders.StandLoader();

		// 音效注册（显式 init，避开 <clinit> 在注册表冻结后注册的崩溃）
		org.huajiager.init.sound.SoundLoader.init();

		// 实体附件注册——显式触碰 Attachments 类以触发静态注册。
		// AttachmentRegistry.buildAndRegister 都在类加载（<clinit>）时执行；若仅靠各事件
		// 回调（lambda）里引用，类加载会被推迟到首个实体加载/首个网络包，晚于区块 NBT
		// 反序列化，导致旧档中持久化附件（time_stop_frozen 等）报 "Unknown attachment
		// type ... skipping" 并被跳过，同时时停冻结残留清理（ENTITY_LOAD）失效。
		// 读取静态字段（getstatic）即强制类加载，无运行时副作用。
		org.huajiager.attachment.Attachments.STAND_DATA.getClass();

		// 实体注册（首子批 7 实体 + 物品批 2 实体 + 替身实体。		// 替身实体 attributes 已在 EntityLoader.register 内注册马类默认属性容器）
		org.huajiager.init.loaders.EntityLoader.register();

		// WING：Lord.Lu 翅膀实体生成驱动（END_SERVER_TICK 轮询满足 lord+open
		// 且戴平衡头盔的玩家生成翅膀实体；条件不满足由实体自身 tick 自毁）
		org.huajiager.entity.EventLordLuWing.register();

		// 物品注册（命令飞盘/第二卷轴/虚空镜片 + 创造标签页）
		org.huajiager.init.loaders.ItemLoader.register();

		// 方块注册（ore_huaji / huaji_star_block + BlockItem）
		org.huajiager.init.loaders.BlockLoader.register();

		// 自定义粒子类型注册（huaji_splash 滑稽粒子，useHuajiSplash 配置用）
		org.huajiager.init.loaders.ParticleLoader.register();

		// 搅拌机/终极熔炉配方类型与序列化器注册
		org.huajiager.recipe.RecipeLoader.register();

		// 机器 ScreenHandlerType 注册
		org.huajiager.screen.MenuLoader.register();

		// 主世界 ore_huaji 矿脉生成 + 萤石伴生
		org.huajiager.common.world.gen.OreGenEventHandler.register();

		// 替身状态效果注册（PotionStand 等，MessageStandUp 召唤依赖）
		org.huajiager.init.loaders.PotionLoader.register();

		// 自定义伤害类型由数据包注册（data/huajiager/damage_type/*.json），
		// DamageLoader 仅提供 RegistryKey 与 DamageSource 工厂（见类头注释）

		// 网络注册（C2S 能力请求 + S2C 技能客户端广播）
		org.huajiager.network.StandNetWorkHandler.register();

		// THE_WORLD 时停消费链（发动倒计时递减 + 范围冻结 + DIO 标记结算），
		// 由 EventTimeStop 事件驱动。
		org.huajiager.stand.events.EventTimeStop.register();

		// 替身能量回充驱动（END_SERVER_TICK 对拥有替身玩家 MPCharge，
		// 修复合上召唤报「能量不足」：StandHandler 初始 0 且原无任何 tick 回充点）
		org.huajiager.stand.events.EventStandCharge.register();

		// 替身状态机每 tick 驱动（END_SERVER_TICK 对触发中玩家执行
		// doStandPower→doTask，让召唤后的替身默认态攻击/弹幕真正生效）
		org.huajiager.stand.events.EventStandPower.register();
		// 特异点压缩结算：SINGULARITY 标记递减、
		// 压缩伤害、t==3 阶段提升 0→1、t==1 仪式死亡。此前标记无消费逻辑，特异点无效果。
		org.huajiager.stand.events.EventStandUpgrade.register();

		// 杀手皇后"点赞"标记事件（攻击生物自动获得 killerQueenTrigger 并记录锁定 UUID）
		org.huajiager.stand.events.EventKillerQueen.register();

		// 疯狂钻石治愈态方块移动（EventCrazyDiamond：
		// 右击沿点击面推方块、左击沿反方向拉方块，block_move 标签状态生效）
		org.huajiager.stand.events.EventCrazyDiamond.register();

		// HIT：奥尔加镇魂曲技能期间空手命中音效
		// （AttackEntityCallback，potionRequiem 激活 + 主手为空 → 播 orga_requiem_hit）
		org.huajiager.stand.events.EventOrgaRequiemHit.register();

		// REQUIEM：奥尔加镇魂曲不死被动（EventOrga
		// handleOrgaEntityDeath/handleOrgaEntityUpdate 花效果段，ALLOW_DEATH + 花 tick）
		org.huajiager.stand.events.EventOrgaRequiem.register();

		// HURT：奥尔加镇魂曲受击反馈（EventOrga.onOrgaPlayerHurt：
		// 持 requiem 反击 2 倍伤害 + 低血 ORGA_SHOT 音效/粒子 + potionOrgaTarget 标记，
		// 以及 onOrgaLivingUpdate 尾部的 potionOrgaTarget 30tick 追踪 30 伤）
		org.huajiager.stand.events.EventOrgaRequiemHurt.register();

		// 替身飞行能力授予/撤销（EventPlayerFlying：fly 态玩家
		// 每 tick 补 allowFlying，退出 fly 态撤销——修复生存模式"飞不起来"）
		org.huajiager.stand.events.EventPlayerFlying.register();

		// SNAKE：白蛇抽碟事件：
		// 攻击命中带 disc_deprive 状态标签的目标时抽取心智/替身 DISC，被夺生物持续凋零掉血。
		org.huajiager.stand.events.EventWhiteSnake.register();

		// 命令注册走 Fabric Brigadier，在服务器启动时回调注册 /reloadStand。
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				org.huajiager.command.CommandStandReload.register(dispatcher));
		LOGGER.info("[HuajiAge] {} v{} initialized", NAME, version);
	}
}
