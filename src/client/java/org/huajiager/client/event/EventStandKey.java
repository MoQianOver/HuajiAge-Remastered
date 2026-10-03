package org.huajiager.client.event;

import org.huajiager.capability.IExposedData;
import org.huajiager.client.KeyLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.messages.MessageDoStandCapabilityServer;
import org.huajiager.stand.messages.MessageStandModeSwitch;
import org.huajiager.stand.messages.MessageStandUp;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 替身按键事件：处理替身召唤/技能/模式切换的按键输入。
 * <p>Fabric 用 ClientTickEvents.END_CLIENT_TICK 轮询 KeyBinding.wasPressed()。
 * 键位已独立为「单键绑定」：不再要求 Ctrl 修饰，玩家可在 设置→控制→按键绑定
 * 中任意绑定单个按键（如 G）即可触发，HUD 提示显示的是实际绑定键名。
 *  standUp 中的替身音效/粒子（HuajiSoundPlayer）依赖未就绪的音频系统，
 * 裁剪后仅保留替身召唤消息链路（MessageStandUp）。</p>
 */
public final class EventStandKey {

	private EventStandKey() {
	}

	// 奥尔加镇魂曲 BGM 触发（并入技能键，替代原客户端药水效果检测）：
	// 释放后延迟 20 tick（1 秒）播放 ORGA_REQUIEM_GOLD（单次播放，播完自然结束）。
	// 触发入口幂等：服务端确认包（MessageDoStandPowerClient）与客户端 potionRequiem
	// 效果检测（EventOrgaRequiemClient）都会调 triggerRequiemBgm()，多次调用只重置
	// 倒计时，归零时先 stopMusic 旧实例再 playMusic 新实例——单次释放只播一遍，
	// 不再出现效果检测 + 确认包双路径各播一次导致的"全部木大 Ride On"两遍/BGM 掐断重播。
	// 两句歌词由服务端 EventOrgaRequiem.updateRequiemLyrics（requiem duration==599）
	// 统一发送，此处不再发，避免歌词重复。
	// 用户需求：技能释放的 BGM 只播放一次（此前循环 BGM 持续播放，反馈"音乐停不下来"）。
	// 播放实例已改为 repeat=false 单次播放（见 HuajiSoundPlayerClient.SingleShotMusicSound），
	// 播完自然结束，不再依赖收回/切换替身才停止；stopBgm() 仅用于在 BGM 尚未播完时
	// 收回替身/切换模式的提前停止。倒计时未归零前收回替身同样会取消未到点的播放。
	private static int bgmCountdown = -1;
	private static boolean bgmActive = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PlayerEntity player = client.player;
			if (player == null) {
				return;
			}
			// 键位为独立单键绑定，直接轮询 KeyBinding.wasPressed()（受 设置→按键绑定 配置控制）
			if (KeyLoader.STAND_UP.wasPressed()) {
				standUp(player);
			}
			if (KeyLoader.STAND_SKILL.wasPressed()) {
				performSkill(client, player);
			}
			if (KeyLoader.STAND_SWITCH.wasPressed()) {
				switchMode(player);
			}
			// BGM 倒计时：延迟 20 tick（1 秒）触发单次 BGM。
			// 触发时只停旧 BGM 实例（stopMusic），不用 stopAllSounds 全清，避免误杀环境音/其他音效。
			// 单次 BGM 播完自然结束（repeat=false），stopBgm() 仅用于提前停止未播完的实例。
			if (bgmCountdown > 0) {
				bgmCountdown--;
				if (bgmCountdown == 0) {
					HuajiSoundPlayer.stopMusic(SoundLoader.ORGA_REQUIEM_GOLD);
					HuajiSoundPlayer.playMusic(SoundLoader.ORGA_REQUIEM_GOLD);
					bgmActive = true;
				}
			}
		});
	}

	/**
	 *  standUp：按绑定键召唤/收回替身，发送 MessageStandUp(isMovingMusic)。
	 * <p>替身存在性与能量校验全部交由服务端 MessageStandUp.handle（data/stand/charge
	 * 空守卫 + canBeCost），客户端只负责下发包。原实现依赖 local player 的 STAND_DATA
	 * 前置判定，但该 persistent attachment 在收到同步包前为 null 会提前 return，
	 * 导致按召唤键完全无反应（即使服务端已觉醒成功）。</p>
	 */
	private static void standUp(PlayerEntity player) {
		ClientPlayNetworking.send(new MessageStandUp(true));
		// 收回/召唤替身时停止未播完的镇魂曲 BGM：收回即停止，召唤后再按技能键会重新触发
		stopBgm();
	}

	/**
	 *  performSkill：按绑定键释放替身技能，发送能力触发消息。
	 * <p>阶段限制：仅/2/3（进化后）可释放技能， 替身（初始/未进化）
	 * 直接拦截不发技能包——按 {@code data.getStage() <= 0} 判定；同时保留
	 * 「已拥有替身且已召唤（isTriggered）」前置判定，未召唤直接拦截不发包。	 * 能量校验、扣费与能力触发统一收归服务端 MessageDoStandCapabilityServer.handle
	 * （载荷携带 cost，单包完成，不足时提示精神力不足）。</p>
	 */
	private static void performSkill(MinecraftClient client, PlayerEntity player) {
		StandBase stand = StandUtil.getType(player);
		// 未召唤（isTriggered=false）时禁用技能：不发任何技能包、不扣费、不提示。
		// 用户需求：「没把替身召唤出来就不能用技能」。取代此前"只要拥有替身即发包、
		// 由服务端判定是否可用"的逻辑（未召唤也会扣费/出提示，用户反馈"只有提示没效果"）。
		IExposedData data = StandUtil.getStandData(player);
		// （初始/未进化替身）不允许放技能：仅/2/3 才允许释放。
		if (data == null || !data.isTriggered() || data.getStage() <= 0) {
			return;
		}
		if (stand == null) {
			return;
		}
		// 单包完成「能量校验 -> 扣费 -> doStandCapability」。此前拆成
		// MessagePerfromSkill（扣费）+ MessageDoStandCapabilityServer（触发）双包，
		// 存在顺序竞态：扣费包先到会扣光能量，触发包后到校验失败被拦截，导致
		// 「烤鸡要没了」本地提示已出但小车从不生成（KQ 小车不生成即此问题）。
		ClientPlayNetworking.send(new MessageDoStandCapabilityServer(stand.getCost()));
		// 注1：技能触发消息（stand.huajiager.skill.<stand>.start）已移出本方法——
		// 客户端本地无条件发送会在服务端精神力校验失败（不足提示、能力未触发）时
		// 仍显示"触发技能"，与精神力不足表现矛盾（用户反馈 bug）。现改为服务端
		// 能量校验通过并 doStandCapability 后统一发送（见 MessageDoStandCapabilityServer.handle）。
		// 注2：镇魂曲 BGM/歌词触发同理——按技能键本地即设 bgmCountdown 会在服务端
		// 精神力校验失败（不足提示、能力未触发）时仍播歌词（用户反馈 bug）。
		// 现改为服务端能量校验通过并 doStandCapability 后单播 MessageDoStandPowerClient
		// 确认包，客户端收到后调 triggerRequiemBgm() 启动歌词倒计时（见 ClientPacketHandlers）。
	}

	/**
	 * 镇魂曲替身技能成功触发确认入口。
	 * <p>调用方：服务端确认包（ClientPacketHandlers 收到 MessageDoStandPowerClient 且
	 * standName=ORGA_REQUIEM、实体为本机玩家）与客户端 potionRequiem 效果检测
	 * （EventOrgaRequiemClient，覆盖物品死亡/不死被动等无确认包路径）。
	 * 幂等：多次调用只重置倒计时，BGM/歌词只在倒计时归零时播/发一次——歌词/BGM 只在
	 * 服务端能量校验通过、能力真正触发后才启动：精神力不足时服务端不发确认包，歌词不再误出。</p>
	 */
	public static void triggerRequiemBgm() {
		bgmCountdown = 20;
	}

	/**
	 *  switchMode：按绑定键切换替身状态，发送 MessageStandModeSwitch。
	 */
	private static void switchMode(PlayerEntity player) {
		IExposedData data = StandUtil.getStandData(player);
		if (data == null || !data.isTriggered()) {
			return;
		}
		StandBase stand = StandUtil.getType(player);
		if (stand != null) {
			ClientPlayNetworking.send(new MessageStandModeSwitch());
			// 切换替身模式时停止未播完的镇魂曲 BGM（模式变化后由下一次技能重新触发）
			stopBgm();
		}
	}

	/** 停止当前未播完的镇魂曲 BGM（幂等；无 BGM 在播时静默）。同时清空未到点的倒计时，
	 *  防止收回/切换替身时残留倒计时到点后 BGM 照播。 */
	private static void stopBgm() {
		bgmCountdown = -1;
		if (bgmActive) {
			HuajiSoundPlayer.stopMusic(SoundLoader.ORGA_REQUIEM_GOLD);
			bgmActive = false;
		}
	}
}
