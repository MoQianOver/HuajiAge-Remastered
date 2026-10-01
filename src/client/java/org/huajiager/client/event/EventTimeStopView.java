package org.huajiager.client.event;

import org.huajiager.capability.StandHandler;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * THE_WORLD 时停视觉（客户端），
 *  的时停核心表现。
 *
 * 数据源：服务端 {@code EventTimeStop} 每 tick 通过 SyncStandChargeMessage 下发
 * buffTag=TIME_STOP + 剩余 tick（buffer），实体镜像写入本地玩家 StandHandler 后由
 * {@code isTimeStopActive()} 判定。
 *
 * 表现：去掉全屏灰膜遮罩（time_stop_view），保留左上/右下齿轮装饰（gear_1/gear_2，
 * 原 64px 放大 50% 并按屏幕尺寸兜底、保证不超出屏幕边缘），并在时停最后一秒播放
 * THE_WORLD_RE 回响音效。开场"砸瓦鲁多"音效与延迟时停由服务端 ItemDioBread 链路负责
 * （先响音效、到点再冻结世界），本类不再做本地兜底播放。
 */
public final class EventTimeStopView {

	private static final Identifier GEAR_1 = new Identifier("huajiager", "textures/misc/gear_1.png");
	private static final Identifier GEAR_2 = new Identifier("huajiager", "textures/misc/gear_2.png");

	private EventTimeStopView() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(EventTimeStopView::onClientTick);
		HudRenderCallback.EVENT.register(EventTimeStopView::onHudRender);
	}

	private static void onClientTick(MinecraftClient mc) {
		if (!isTimeStopActive(mc)) {
			return;
		}
		PlayerEntity player = mc.player;
		if (player == null) {
			return;
		}
		// 时停最后一秒（剩余约 20 tick）播放回响声。
		// 结束音按替身名分发：白金之星用专属时停结束音（star_platinum_the_world_re），
		// THE_WORLD 及其它替身维持共用 THE_WORLD_RE（取消白金与世界的共用）。
		if (getRemaining(player) == 20) {
			StandBase stand = StandUtil.getType(player);
			boolean isStarPlatinum = stand != null
					&& StandLoader.STAR_PLATINUM.getName().equals(stand.getName());
			player.playSound(isStarPlatinum ? SoundLoader.STAR_PLATINUM_THE_WORLD_RE : SoundLoader.THE_WORLD_RE, 1f, 1f);
		}
	}

	private static void onHudRender(DrawContext context, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!isTimeStopActive(mc)) {
			return;
		}
		int width = mc.getWindow().getScaledWidth();
		int height = mc.getWindow().getScaledHeight();

		double effect = ConfigHuaji.Stands.timeStopEffect;
		float alpha = (float) Math.max(0.3, Math.min(1.0, 0.55 * effect));

		// 齿轮原 64px，放大 50% 并按屏幕最小尺寸兜底、保证不超出屏幕边缘
		double scale = ConfigHuaji.Stands.timeStopScale * 1.5;
		int gs = (int) (64 * scale);
		int limit = Math.min(width, height) - 8;
		if (gs > limit) {
			gs = Math.max(limit, 8);
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		context.drawTexture(GEAR_1, 0, 0, 0.0f, 0.0f, gs, gs, gs, gs);
		context.drawTexture(GEAR_2, width - gs, height - gs, 0.0f, 0.0f, gs, gs, gs, gs);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}

	private static boolean isTimeStopActive(MinecraftClient mc) {
		PlayerEntity player = mc.player;
		if (player == null) {
			return false;
		}
		StandHandler handler = StandUtil.getStandHandler(player);
		return handler != null
				&& HuajiConstant.BuffTags.TIME_STOP.equals(handler.getBuffTag())
				&& handler.getBuffer() > 0;
	}

	private static int getRemaining(PlayerEntity player) {
		StandHandler handler = StandUtil.getStandHandler(player);
		return handler == null ? 0 : handler.getBuffer();
	}
}
