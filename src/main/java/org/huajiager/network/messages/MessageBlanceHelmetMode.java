package org.huajiager.network.messages;

import org.huajiager.item.ItemBlancedHelmet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S：按 MODE_SWITCH 键切换平衡头盔 open 状态，
 * 。
 * 客户端仅下发按键意图，服务端依据 NBT 三态调度 ItemBlancedHelmet.modeChange
 * 并回显 open / failed 消息（参考模板 network.messages 包现有 FabricPacket 记录类范式）。
 */
public record MessageBlanceHelmetMode() implements FabricPacket {

	public static final PacketType<MessageBlanceHelmetMode> TYPE = PacketType.create(
			new Identifier("huajiager", "blance_helmet_mode"), MessageBlanceHelmetMode::new);

	public MessageBlanceHelmetMode(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageBlanceHelmetMode payload, ServerPlayerEntity player,
			PacketSender responseSender) {
		ItemStack stack = player.getEquippedStack(EquipmentSlot.HEAD);
		if (stack.getItem() instanceof ItemBlancedHelmet) {
			ItemBlancedHelmet.modeChange(stack, player);
		}
	}
}
