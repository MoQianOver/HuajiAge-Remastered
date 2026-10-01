package org.huajiager.client.event;

import org.huajiager.init.loaders.PotionLoader;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 停不下来的奥尔加——客户端 BGM 触发（EventOrga 的客户端音乐段）。
 *
 * 语义（client tick）：
 *  - potionRequiem duration == 599：stopSounds + playMusic(ORGA_REQUIEM_GOLD)，
 *    并发送 message.huajiager.orga.requiem.bgm.1 / bgm.2；效果消失后停音乐。 *  - potionFlowerHope duration == 2720：stopSounds + playMusic(ORGA_FLOWER)。 *    效果消失后停音乐。
 *
 * Fabric 侧统一由 EventStandKey 管理 ORGA_REQUIEM_GOLD 循环 BGM 的播放/停止：
 *  - 服务端校验通过并 doStandCapability 后单播确认包 → triggerRequiemBgm()。 *  - 本类作为「potionRequiem 首次出现」的兜底入口（覆盖镇魂曲物品死亡/不死被动等
 *    无确认包路径），同样调 triggerRequiemBgm()。
 *  triggerRequiemBgm() 幂等：多次调用只重置 20 tick 倒计时，倒计时归零时先 stopMusic
 *  旧实例再 playMusic 新实例，单次释放只播一遍（修复此前效果检测 + 确认包双路径
 *  各播一次导致"全部木大 Ride On"两遍、BGM 被掐断重播）。歌词由服务端
 *  EventOrgaRequiem.updateRequiemLyrics（duration==599）统一发送，本类不再发。
 *  效果消失不 stopAllSounds：镇魂曲 BGM 为单次播放（repeat=false），播完自然结束，
 *  不需要回收替身/切换模式才停止。
 */
public final class EventOrgaRequiemClient {

	private EventOrgaRequiemClient() {
	}

	/** 上一 tick 是否已存在 potionRequiem（边沿触发用）。 */
	private static boolean hasRequiem = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventOrgaRequiemClient::tick);
	}

	private static void tick(MinecraftClient client) {
		PlayerEntity player = client.player;
		if (player == null) {
			return;
		}
		StatusEffectInstance requiem = player.getStatusEffect(PotionLoader.potionRequiem);
		// 语义：duration == 599 时播 BGM。客户端收到的药水剩余时长受网络同步/时序
		// 偏移影响，精确 == 599 判定在联机环境几乎必然错过，改「效果存在即触发」。
		// 必须用「从无到有」边沿触发，只在效果首次出现时调一次 triggerRequiemBgm()：
		// 该方法是「重置 20 tick 倒计时」语义，若每 tick 效果存在都调用，倒计时会被
		// 持续重置、永远无法归零，playMusic 永不执行——实测 BGM 完全不响的根因。
		if (requiem != null) {
			if (!hasRequiem) {
				hasRequiem = true;
				EventStandKey.triggerRequiemBgm();
			}
		} else {
			hasRequiem = false;
		}

		// ORGA_FLOWER 由服务端在 flower duration==2710 时广播播放（对齐 playSound 语义），
		// 客户端不再做 == 精确判定（网络同步跳变不可靠）与 playMusic 循环播放。
	}
}
