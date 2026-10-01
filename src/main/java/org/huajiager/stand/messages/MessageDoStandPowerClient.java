package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * S2C：服务端向客户端广播替身能力表现。
 *
 * 载荷仅 playerName + standName，客户端 handler（位于 client source set，
 * 见 org.huajiager.HuajiAgeRemasteredClient）取出实体后调用
 * StandBase.doStandCapabilityClient(world, user) 播放技能特效/音效。
 * main 侧只承载 payload 定义与序列化，不引入 MinecraftClient，符合 splitEnvironmentSourceSets。
 */
public record MessageDoStandPowerClient(String playerName, String standName) implements FabricPacket {
	public static final PacketType<MessageDoStandPowerClient> TYPE = PacketType.create(
			new Identifier("huajiager", "stand_power_client"), MessageDoStandPowerClient::new);

	public MessageDoStandPowerClient(PacketByteBuf buf) {
		this(buf.readString(), buf.readString());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeString(playerName);
		buf.writeString(standName);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
