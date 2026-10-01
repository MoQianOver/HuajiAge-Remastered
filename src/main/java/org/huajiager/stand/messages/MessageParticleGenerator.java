package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * S2C：通用粒子生成消息，
 * 。
 *
 * 载荷为出生坐标 + 粒子注册名 + 数量 + 速度 + 距离。粒子类型以字符串传输
 * （ParticleTypes 静态字段在 main 侧可直接引用其 Identifier），客户端 handler
 * 位于 client source set（+ 补入）。
 */
public record MessageParticleGenerator(Vec3d pos, String particle, int count, int speed, int distance)
		implements FabricPacket {

	public static final PacketType<MessageParticleGenerator> TYPE = PacketType.create(
			new Identifier("huajiager", "particle_generator"), MessageParticleGenerator::new);

	public MessageParticleGenerator(PacketByteBuf buf) {
		this(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readString(),
				buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeDouble(pos.x);
		buf.writeDouble(pos.y);
		buf.writeDouble(pos.z);
		buf.writeString(particle);
		buf.writeInt(count);
		buf.writeInt(speed);
		buf.writeInt(distance);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
