package org.huajiager.client.event;

import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemExglutenbur;
import org.huajiager.network.messages.MessageExglutenburMode;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallbackI;

/**
 * EX面筋棒潜行滚轮切换风味， ItemExglutenbur.onMouseDwheelInput
 * 。Fabric 1.20.1 无对等鼠标滚轮事件，改用 GLFW
 * glfwSetScrollCallback 安装回调（保存并转发旧回调；潜行+主手为面筋棒时消费滚动
 * 不转发，对齐 evt.setCanceled(true) 的"不切物品栏"语义）。
 * 客户端本地播放目标风味音效与提示（对齐），并发送 MessageExglutenburMode(next)
 * 由服务端完成 flavor 循环切换。
 */
public final class EventExglutenburModeChange {

	private static GLFWScrollCallbackI previous;
	private static boolean installed = false;

	private EventExglutenburModeChange() {
	}

	public static void register() {
		// 首次客户端 tick 时安装滚轮回调（窗口句柄此时才可用）
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.getWindow() == null || installed) {
				return;
			}
			previous = GLFW.glfwSetScrollCallback(client.getWindow().getHandle(),
					EventExglutenburModeChange::onScroll);
			installed = true;
		});
	}

	private static void onScroll(long window, double xoffset, double yoffset) {
		MinecraftClient client = MinecraftClient.getInstance();
		boolean handled = false;
		if (client != null && client.player != null && client.currentScreen == null
				&& yoffset != 0) {
			PlayerEntity player = client.player;
			if (player.isSneaking()) {
				ItemStack main = player.getMainHandStack();
				if (main.getItem() instanceof ItemExglutenbur) {
					boolean down = yoffset < 0;
					playSwitchFeedback(player, main, down);
					ClientPlayNetworking.send(new MessageExglutenburMode(down));
					handled = true;
				}
			}
		}
		if (!handled && previous != null) {
			previous.invoke(window, xoffset, yoffset);
		}
	}

	/** 客户端本地反馈：按当前（旧）flavor 播"即将切入"的风味音效与提示（对齐）。 */
	private static void playSwitchFeedback(PlayerEntity player, ItemStack stack, boolean down) {
		switch (ItemExglutenbur.flavor(stack)) {
			case 0 -> {
				if (down) {
					player.playSound(SoundLoader.EXGLUTENBUR_1, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.1"), false);
				} else {
					player.playSound(SoundLoader.EXGLUTENBUR_3, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.3"), false);
				}
			}
			case 1 -> {
				if (down) {
					player.playSound(SoundLoader.EXGLUTENBUR_2, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.2"), false);
				}
			}
			case 2 -> {
				if (down) {
					player.playSound(SoundLoader.EXGLUTENBUR_3, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.3"), false);
				} else {
					player.playSound(SoundLoader.EXGLUTENBUR_1, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.1"), false);
				}
			}
			case 3 -> {
				if (!down) {
					player.playSound(SoundLoader.EXGLUTENBUR_2, 1.0F, 1.0F);
					player.sendMessage(Text.translatable("message.huajiager.exglutenbur.flavor.2"), false);
				}
			}
			default -> {
			}
		}
	}
}
