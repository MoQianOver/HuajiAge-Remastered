package org.huajiager.stand.messages;

import org.huajiager.capability.StandHandler;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * C2S：替身技能释放扣费消息，
 * 。
 * 载荷为技能消耗量 cost，服务端校验能量是否足够并扣费。
 */
public record MessagePerfromSkill(int cost) implements FabricPacket {

	public static final PacketType<MessagePerfromSkill> TYPE = PacketType.create(
			new Identifier("huajiager", "stand_perfrom_skill"), MessagePerfromSkill::new);

	public MessagePerfromSkill(PacketByteBuf buf) {
		this(buf.readInt());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(cost);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessagePerfromSkill payload, ServerPlayerEntity player, PacketSender responseSender) {
		StandBase stand = StandUtil.getType(player);
		StandHandler charge = StandUtil.getStandHandler(player);
		if (stand == null || charge == null) {
			return;
		}
		if (charge.canBeCost(payload.cost())) {
			charge.cost(payload.cost());
		} else {
			player.sendMessage(Text.translatable("message.huajiager.stand_skill.cost_lack"), false);
		}
	}
}
