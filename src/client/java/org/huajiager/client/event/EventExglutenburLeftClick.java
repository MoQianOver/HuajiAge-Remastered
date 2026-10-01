package org.huajiager.client.event;

import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemExglutenbur;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import org.lwjgl.glfw.GLFW;

/**
 * EX面筋棒空挥音效：flavor2空挥播烈焰人射击音效，
 * ItemExglutenbur.leftClick。
 * Fabric 1.20.1 无对等空挥事件，复用模板 EventHuajiStarSwordLeftClick 的
 * END_CLIENT_TICK + GLFW.glfwGetMouseButton 左键边沿检测范式。
 */
public final class EventExglutenburLeftClick {

	private static boolean lastButton = false;

	private EventExglutenburLeftClick() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PlayerEntity player = client.player;
			if (player == null || client.getWindow() == null || client.currentScreen != null) {
				lastButton = false;
				return;
			}
			boolean pressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
					== GLFW.GLFW_PRESS;
			if (pressed && !lastButton) {
				if (player.getMainHandStack().getItem() instanceof ItemExglutenbur
						&& ItemExglutenbur.flavor(player.getMainHandStack()) == 2) {
					player.playSound(SoundLoader.EXGLUTENBUR_2, 1.0F, 1.0F);
				}
			}
			lastButton = pressed;
		});
	}
}
