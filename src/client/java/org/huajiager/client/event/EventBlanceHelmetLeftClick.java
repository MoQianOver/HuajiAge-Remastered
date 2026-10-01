package org.huajiager.client.event;

import org.huajiager.item.ItemBlancedHelmet;
import org.huajiager.network.messages.MessageFiveBulletShoot;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;

import org.lwjgl.glfw.GLFW;

/**
 * 五五开 BUFF 左键空挥攻击钩子。
 *
 *  EventKeyInput.leftClick：
 * 头戴五五开头盔且 NBT 含 lord+open 时左键空挥 → 发送 MessageFiveBulletShoot，
 * 服务端生成随机黑白字弹（EntityFivePower）。
 * Fabric 1.20.1 无 KeyInputEvent，采用 EventHuajiStarSwordLeftClick 同款
 * GLFW 左键边沿检测（松开→按下算一次点击）在 END_CLIENT_TICK 轮询，
 * 并以 crosshairTarget 判定"空挥"（对齐 LeftClickEmpty 语义）。
 */
public final class EventBlanceHelmetLeftClick {

	private static boolean prevLeftDown = false;

	private EventBlanceHelmetLeftClick() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventBlanceHelmetLeftClick::onTick);
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
		ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
		if (!(head.getItem() instanceof ItemBlancedHelmet)) {
			return;
		}
		// 对齐 EventKeyInput.leftClick：hasKey("lord") && hasKey("open") && open==true。
		// 只要求 NBT 含 lord 键（hasKey 语义，不校验布尔值）+ open 开启；服务端不二次拦截。
		if (!head.getOrCreateNbt().contains("lord") || !ItemBlancedHelmet.isOpen(head)) {
			return;
		}
		ClientPlayNetworking.send(new MessageFiveBulletShoot());
	}
}
