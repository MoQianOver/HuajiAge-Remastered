package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * S2C：替身能量实时同步消息，
 * 。
 * 载荷为当前能量 / 上限 / 缓冲值 / 增益标签，客户端 handler 位于 client source set。
 * <p>chargeOnly 标识本包是否仅为能量回充同步（只写 charge/max，不触碰 buffer/buffTag）：
 * 常驻回充链路 EventStandCharge 每 5 tick 广播一次，若不区分来源会把它打包的
 * buffer/buffTag 污染值周期性覆盖到客户端时停状态，导致滤镜/齿轮/反色一闪一闪。</p>
 */
public record SyncStandChargeMessage(int charge, int max, int buffer, String buffTag,
		boolean chargeOnly) implements FabricPacket {

	public static final PacketType<SyncStandChargeMessage> TYPE = PacketType.create(
			new Identifier("huajiager", "sync_stand_charge"), SyncStandChargeMessage::new);

	public SyncStandChargeMessage(PacketByteBuf buf) {
		this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(charge);
		buf.writeInt(max);
		buf.writeInt(buffer);
		buf.writeString(buffTag);
		buf.writeBoolean(chargeOnly);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
