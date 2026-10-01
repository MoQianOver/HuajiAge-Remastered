package org.huajiager.client.event;

import org.huajiager.client.KeyLoader;
import org.huajiager.item.ItemBlancedHelmet;
import org.huajiager.network.messages.MessageBlanceHelmetMode;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * 五五开头盔模式切换键（KeyLoader.MODE_SWITCH）， EventKeyInput 的
 * modeSwitch 分支：按 MODE_SWITCH 键且头戴五五开头盔时发送 MessageBlanceHelmetMode，
 * 由服务端依据 NBT 三态调度 ModeChange。
 * Fabric 用 ClientTickEvents.END_CLIENT_TICK 轮询 KeyBinding.wasPressed()
 * （参考 EventStandKey 轮询范式）。
 */
public final class EventBlanceHelmetKey {

	private EventBlanceHelmetKey() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			if (!KeyLoader.MODE_SWITCH.wasPressed()) {
				return;
			}
			PlayerEntity player = client.player;
			ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
			if (head.getItem() instanceof ItemBlancedHelmet) {
				ClientPlayNetworking.send(new MessageBlanceHelmetMode());
			}
		});
	}
}
