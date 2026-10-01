package org.huajiager.stand.messages;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * S2C：时停滤镜同步（THE_WORLD / Dio 面包 / 白金之星统一）。
 *
 * <p>服务端 EventTimeStop.onTheWorld 扫描时，把时停剩余 tick 同步给范围内受影响的所有
 * 玩家——包括被冻结的玩家、以及持有 potionRequiem 免疫冻结的镇魂曲玩家。
 * 客户端 handler 只写本地玩家 StandHandler 的 buffer/buffTag（不触碰 charge/max），
 * 驱动 EventTimeStopView（齿轮 HUD）与 TimeStopPostShader（反色/灰色后处理滤镜）。</p>
 *
 * <p>与 SyncStandChargeMessage 的区别：后者携带 charge/max 且语义为"本机玩家自己的
 * 能量同步"，直接复用会把发动者的能量值污染到受影响玩家身上，因此单独走轻量包。</p>
 */
public record MessageTimeStopFilterSync(int remaining) implements FabricPacket {

	public static final PacketType<MessageTimeStopFilterSync> TYPE = PacketType.create(
			new Identifier("huajiager", "time_stop_filter_sync"), MessageTimeStopFilterSync::new);

	public MessageTimeStopFilterSync(PacketByteBuf buf) {
		this(buf.readInt());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(remaining);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
