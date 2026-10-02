package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * S2C：Dio 面包 / THE_WORLD / 白金之星时停的开场音效消息（广播给发动者附近玩家）。
 *
 * 服务端随机选中音效后，把 SoundEvent 的 registry id 连同发动者坐标一起下发给
 * 附近玩家（64 格内，含发动者本人）；客户端 handler（位于 client source set，见
 * org.huajiager.client.ClientPacketHandlers）解析出 SoundEvent 后以声源坐标播放，
 * 距离自然衰减。
 * 目的：服务端 player.playSound 的世界广播存在到达时机/衰减不确定性（历史实测本地听不到），
 * 改由 S2C 广播 + 客户端按坐标播放，保证发动者必然可闻，且范围内其他玩家同样能听到
 * 时停开场音（声源为发动者位置，越远越轻）。
 */
public record MessageDioBreadTimeStop(String source, String soundId, double x, double y, double z) implements FabricPacket {

	public static final PacketType<MessageDioBreadTimeStop> TYPE = PacketType.create(
			new Identifier("huajiager", "dio_bread_time_stop"), MessageDioBreadTimeStop::new);

	public MessageDioBreadTimeStop(PacketByteBuf buf) {
		this(buf.readString(), buf.readString(), buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeString(source);
		buf.writeString(soundId);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
