package org.huajiager.client.event;

import org.huajiager.item.ItemHeroBow;
import org.huajiager.stand.messages.MessageLeftClickModeChange;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import org.lwjgl.glfw.GLFW;

/**
 * 大英雄之弓 Burst（解放）切换钩子：Shift（蹲下）+ 左键时切换。
 *
 *  EventLeftClickEmpty语义：
 * 潜行 + 主手为英雄之弓 → 本地先 toggleMode（立即刷新 Burst 谓词/名称，UI 即时反馈），
 * 再发送 MessageLeftClickModeChange(open) 让服务端物品堆 NBT 与客户端对齐。
 * Fabric 1.20.1 无 LeftClickEmpty 事件，且 KeyBinding.wasPressed() 会被游戏本 tick
 * 的正常攻击/挖矿处理提前消费导致漏检，故改用 glfwGetMouseButton 对左键实时状态做
 * 边沿检测（松开→按下算一次点击），在 END_CLIENT_TICK 轮询。判定不限制"空击"：
 * 蹲下+左键的意图场景本身就专属于切 Burst，对准方块也照常切换，避免手感困惑。
 */
public final class EventHeroBowModeChange {

	private static boolean prevLeftDown = false;

	private EventHeroBowModeChange() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventHeroBowModeChange::onTick);
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
		PlayerEntity player = client.player;
		if (!player.isSneaking()) {
			return;
		}
		ItemStack main = player.getMainHandStack();
		if (!(main.getItem() instanceof ItemHeroBow)) {
			return;
		}
		boolean open = !ItemHeroBow.isOpen(main);
		ItemHeroBow.toggleMode(main);
		ClientPlayNetworking.send(new MessageLeftClickModeChange(open));
	}
}
