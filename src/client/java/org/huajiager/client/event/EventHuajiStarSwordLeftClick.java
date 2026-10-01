package org.huajiager.client.event;

import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemHuajiStarSword;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;

import org.lwjgl.glfw.GLFW;

/**
 * 滑稽之星剑左键空挥音效钩子。
 *
 *  ItemHuajiStarSword.leftClick：
 * 开启（burst）状态下左键空挥播放 WAVE1 音效。
 * Fabric 1.20.1 无 LeftClickEmpty 事件，采用与 EventHeroBowModeChange 相同的
 * GLFW 左键边沿检测（松开→按下算一次点击）在 END_CLIENT_TICK 轮询，
 * 并用 crosshairTarget 判定"空挥"（未命中方块/实体），避免与攻击/挖矿混淆。
 */
public final class EventHuajiStarSwordLeftClick {

	private static boolean prevLeftDown = false;

	private EventHuajiStarSwordLeftClick() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventHuajiStarSwordLeftClick::onTick);
	}

	private static void onTick(MinecraftClient client) {
		if (client == null || client.player == null || client.currentScreen != null) {
			prevLeftDown = false;
			return;
		}
		long handle = client.getWindow().getHandle();
		boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean clicked = leftDown && !prevLeftDown;
		prevLeftDown = leftDown;
		if (!clicked) {
			return;
		}
		// 仅空挥（未命中方块/实体）触发，对齐 LeftClickEmpty 语义
		if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.MISS) {
			return;
		}
		PlayerEntity player = client.player;
		ItemStack main = player.getMainHandStack();
		if (!(main.getItem() instanceof ItemHuajiStarSword)) {
			return;
		}
		if (!ItemHuajiStarSword.isOpen(main)) {
			return;
		}
		player.playSound(SoundLoader.WAVE1, 1.0F, 1.0F);
	}
}
