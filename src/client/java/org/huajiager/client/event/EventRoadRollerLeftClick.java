package org.huajiager.client.event;

import org.huajiager.item.ItemRoadRoller;
import org.huajiager.stand.messages.MessageLeftClickRoadRoller;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import org.lwjgl.glfw.GLFW;

/**
 * 压路机时停连击（DIO 推挤）左键钩子：主手持压路机物品时左键点击发
 * MessageLeftClickRoadRoller，还原 ItemRoadRoller 的 LeftClickEmpty 语义。
 *
 * Fabric 1.20.1 无 LeftClickEmpty 事件，
 * 且 options.attackKey.wasPressed() 会被游戏本 tick 的正常攻击/挖矿处理提前消费导致漏检，
 * 故沿用 EventHeroBowModeChange 的方案：glfwGetMouseButton 对左键实时状态做边沿检测
 * （松开→按下算一次点击），在 ClientTickEvents.END_CLIENT_TICK 轮询。
 *
 * 服务端 MessageLeftClickRoadRoller.handle 仅在玩家带 THE_WORLD 时停标记时把范围内
 * 压路机的 huajiage.dio_push +2，普通状态发包无副作用，故客户端不做时停前置判定、
 * 也不限制"空击"，只要手持压路机左键按下即上报，时停判定交给服务端统一过滤。
 */
public final class EventRoadRollerLeftClick {

	private static boolean prevLeftDown = false;

	private EventRoadRollerLeftClick() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventRoadRollerLeftClick::onTick);
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
		ItemStack main = client.player.getMainHandStack();
		if (!(main.getItem() instanceof ItemRoadRoller)) {
			return;
		}
		ClientPlayNetworking.send(new MessageLeftClickRoadRoller());
	}
}
