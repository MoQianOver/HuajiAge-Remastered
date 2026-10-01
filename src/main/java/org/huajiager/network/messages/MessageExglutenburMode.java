package org.huajiager.network.messages;

import org.huajiager.item.ItemExglutenbur;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S：潜行滚轮切换 EX面筋棒 flavor，
 * 。
 * 载荷 next=true 表示向前（滚轮下滚），服务端在主手为面筋棒时循环切换 flavor(0~3)。
 */
public record MessageExglutenburMode(boolean next) implements FabricPacket {

	public static final PacketType<MessageExglutenburMode> TYPE = PacketType.create(
			new Identifier("huajiager", "exglutenbur_mode"), MessageExglutenburMode::new);

	public MessageExglutenburMode(PacketByteBuf buf) {
		this(buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBoolean(next);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageExglutenburMode payload, ServerPlayerEntity player,
			PacketSender responseSender) {
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() instanceof ItemExglutenbur) {
			int f = ItemExglutenbur.flavor(stack);
			ItemExglutenbur.setFlavor(stack, (f + (payload.next ? 1 : -1) + 4) % 4);
		}
	}
}
