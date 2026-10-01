package org.huajiager.util;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.network.StandNetWorkHandler;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * 服务端网络广播工具， 。
 *
 * 依赖 SimpleNetworkWrapper + MinecraftServer.getPlayerList。 * Fabric 版改经 ServerPlayNetworking 向服务端在线玩家广播 CustomPayload。
 * 原 HuajiAgeNetWorkHandler / StandNetWorkHandler 两套 HANDLER 在本工程合并用一套
 * payload 注册表（StandNetWorkHandler），sendPacketTo* 与 sendPacketTo*Stand 行为等价。
 */
public class ServerUtil {

	private ServerUtil() {
	}

	public static void sendPacketToPlayers(LivingEntity player, FabricPacket msg) {
		sendPacketToPlayersStand(player, msg);
	}

	public static void sendPacketToPlayersStand(LivingEntity player, FabricPacket msg) {
		if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}
		try {
			for (ServerPlayerEntity p : serverWorld.getServer().getPlayerManager().getPlayerList()) {
				if (p.getWorld() == serverWorld) {
					ServerPlayNetworking.send(p, msg);
				}
			}
		} catch (Exception e) {
			HuajiAgeRemastered.LOGGER.error("[HuajiAge] sendPacketToPlayersStand failed", e);
		}
	}

	public static void sendPacketToNearbyPlayers(LivingEntity entity, FabricPacket msg) {
		sendPacketToNearbyPlayersStand(entity, msg);
	}

	public static void sendPacketToNearbyPlayersStand(LivingEntity entity, FabricPacket msg) {
		if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}
		try {
			for (ServerPlayerEntity p : serverWorld.getServer().getPlayerManager().getPlayerList()) {
				if (p.getWorld() == serverWorld && p.squaredDistanceTo(entity) < 64.0 * 64.0) {
					ServerPlayNetworking.send(p, msg);
				}
			}
		} catch (Exception e) {
			HuajiAgeRemastered.LOGGER.error("[HuajiAge] sendPacketToNearbyPlayersStand failed", e);
		}
	}

	public static SoundEvent getRegisteredSoundEvent(String id) {
		Identifier soundId = Identifier.tryParse(id);
		SoundEvent soundevent = soundId == null ? null : Registries.SOUND_EVENT.get(soundId);
		if (soundevent == null) {
			throw new IllegalStateException("Invalid Sound requested: " + id);
		}
		return soundevent;
	}
}
