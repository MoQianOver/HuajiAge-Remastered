package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * S2C：DIO 时停命中表现消息，
 * 。
 *
 * 载荷为命中坐标 + 是否开启时停特效（flag）。客户端 handler 位于 client source set
 * （+ 随渲染/客户端批补入），main 侧仅承载 payload 定义与序列化。
 */
public record MessageDioHitClient(Vec3d pos, boolean flag) implements FabricPacket {

	public static final PacketType<MessageDioHitClient> TYPE = PacketType.create(
			new Identifier("huajiager", "dio_hit_client"), MessageDioHitClient::new);

	public MessageDioHitClient(PacketByteBuf buf) {
		this(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeDouble(pos.x);
		buf.writeDouble(pos.y);
		buf.writeDouble(pos.z);
		buf.writeBoolean(flag);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
