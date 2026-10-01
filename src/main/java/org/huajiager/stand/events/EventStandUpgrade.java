package org.huajiager.stand.events;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.messages.MessageParticleGenerator;
import org.huajiager.util.NBTHelper;
import org.huajiager.util.ServerUtil;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

/**
 * 特异点压缩结算（EventStand.standUpgrade 对 LivingUpdateEvent 的处理）。
 * <p>在每 tick 递减玩家实体的 SINGULARITY 标记（使用特异点时写入 200）：
 * 标记期间每 10 tick 受到 18 点「特异点压缩」伤害；当标记递减到 3 时，替身阶段
 * 提升到 1（UI_TOAST 音效 + 烟花粒子）；递减到 1 时受到虚空伤害强制死亡（创造
 * 模式直接移除实体）——「死者是无法抵达新世界的」，活到效果结束即可突破阶段。
 * </p>
 * <p>Fabric 1.20.1 用 {@link ServerTickEvents#END_SERVER_TICK}，仅遍历在线玩家：
 * SINGULARITY 标记只由玩家使用物品写入，对所有实体的遍历等价于玩家子集。
 * 此前该标记写入后全工程无任何消费逻辑（standUpgrade 未），导致特异点
 * 「只响音效、无实际效果」，此处补齐（独立编写）。</p>
 */
public final class EventStandUpgrade {

	private EventStandUpgrade() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				// 迭代副本：upgradeTick 内可能 discard()/移除玩家，直接迭代原列表会抛
				// ConcurrentModificationException（崩溃日志 EventStandUpgrade.java:36）
				for (ServerPlayerEntity player : new ArrayList<>(world.getPlayers())) {
					upgradeTick(player);
				}
			}
		});
	}

	private static void upgradeTick(ServerPlayerEntity player) {
		int t = NBTHelper.getEntityInteger(player, HuajiConstant.Tags.SINGULARITY);
		if (t > 0) {
			NBTHelper.setEntityInteger(player, HuajiConstant.Tags.SINGULARITY, t - 1);
			if (player.age % 10 == 0) {
				player.damage(DamageLoader.singularity(player), 18);
			}
			if (player.getHealth() <= 0) {
				return;
			}
		}
		if (t == 3) {
			StandUtil.setStandStage(player, 1);
			HuajiSoundPlayer.playToNearbyClient(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 2.0F);
			ServerUtil.sendPacketToNearbyPlayers(player,
					new MessageParticleGenerator(player.getPos(), "minecraft:fireworks_spark", 120, 3, 1));
		}
		if (t == 1) {
			player.damage(player.getDamageSources().outOfWorld(), 999999);
			if (player.isCreative()) {
				player.discard();
			}
		}
	}
}
