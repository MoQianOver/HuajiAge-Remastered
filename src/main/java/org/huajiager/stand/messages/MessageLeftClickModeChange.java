package org.huajiager.stand.messages;

import org.huajiager.item.ItemHeroBow;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S：Shift+左键空击切换大英雄之弓 Burst（解放）状态，
 * 。
 *
 * 客户端已在本端 toggleMode 更新渲染谓词（即时 UI 反馈），物品堆 NBT 的直接修改
 * 不触发槽位同步，故本消息携带目标 open 值，让服务端物品堆 NBT 与客户端保持一致，
 * 后续服务端 onStoppedUsing 判断 Burst 时依据的就是这份对齐后的状态。
 */
public record MessageLeftClickModeChange(boolean open) implements FabricPacket {

	public static final PacketType<MessageLeftClickModeChange> TYPE = PacketType.create(
			new Identifier("huajiager", "hero_bow_mode_change"), MessageLeftClickModeChange::new);

	public MessageLeftClickModeChange(PacketByteBuf buf) {
		this(buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBoolean(open);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageLeftClickModeChange payload, ServerPlayerEntity player,
			PacketSender responseSender) {
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() instanceof ItemHeroBow) {
			NbtCompound nbt = stack.getOrCreateNbt();
			if (payload.open) {
				nbt.putBoolean("open", true);
			} else {
				nbt.remove("open");
			}
		}
	}
}
