package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * S2C：替身外露数据同步消息，
 * 。
 * 以 NBT 承载全部字段，此处按字段拆分传输（语义等价、避免手写 NBT 序列化），
 * 载荷为替身名 / 阶段 / 触发 / 手部显示 / 状态机 / 模型 / 显示名 / 目标玩家名 / 是否本机玩家。
 */
public record SyncExposedStandDataMessage(String stand, int stage, boolean trigger, boolean hand, String state,
		String model, String displayName, String user, boolean isUser) implements FabricPacket {

	public static final PacketType<SyncExposedStandDataMessage> TYPE = PacketType.create(
			new Identifier("huajiager", "sync_exposed_stand_data"), SyncExposedStandDataMessage::new);

	public SyncExposedStandDataMessage(PacketByteBuf buf) {
		this(buf.readString(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readString(),
				buf.readString(), buf.readString(), buf.readString(), buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeString(stand);
		buf.writeInt(stage);
		buf.writeBoolean(trigger);
		buf.writeBoolean(hand);
		buf.writeString(state);
		buf.writeString(model);
		buf.writeString(displayName);
		buf.writeString(user);
		buf.writeBoolean(isUser);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
