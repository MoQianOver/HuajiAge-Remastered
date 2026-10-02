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
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * THE_WORLD 时停视觉（客户端），
 *  的时停核心表现。
 *
 * 数据源：服务端 {@code EventTimeStop} 每 tick 通过 SyncStandChargeMessage 下发
 * buffTag=TIME_STOP + 剩余 tick（buffer），实体镜像写入本地玩家 StandHandler 后由
 * {@code isTimeStopActive()} 判定。
 *
 * 保留左上/右下齿轮装饰（gear_1/gear_2，原 64px 放大 50% 并按屏幕尺寸兜底、保证不超出
 * 屏幕边缘），并在时停最后一秒播放 THE_WORLD_RE 回响音效。开场"砸瓦鲁多"音效与延迟时停由服务端 ItemDioBread 链路负责
 * （先响音效、到点再冻结世界），本类不再做本地兜底播放。
 */
public final class EventTimeStopView {

	private static final Identifier GEAR_1 = new Identifier("huajiager", "textures/misc/gear_1.png");
	private static final Identifier GEAR_2 = new Identifier("huajiager", "textures/misc/gear_2.png");
	private static final Identifier TIME_STOP_VIEW = new Identifier("huajiager", "textures/misc/time_stop_view.png");
	private static final float MASK_ALPHA = 0.3f;

	/** 时停来源（由 MessageDioBreadTimeStop 开场音消息在客户端记录）：the_world=面包/世界发动，star_platinum=白金之星技能。 */
	private static final String SOURCE_THE_WORLD = "the_world";
	private static final String SOURCE_STAR_PLATINUM = "star_platinum";
	private static String timeStopSource = SOURCE_THE_WORLD;

	private EventTimeStopView() {
	}

	/**
	 * 记录最近一次时停的开场来源，时停结束时据此分发结束音效：
	 * 来源为世界（Dio 面包 / THE_WORLD 技能）一律播 THE_WORLD_RE；
	 * 仅白金之星自身技能发动的时停播专属 STAR_PLATINUM_THE_WORLD_RE。
	 */
	public static void setTimeStopSource(String source) {
		timeStopSource = SOURCE_STAR_PLATINUM.equals(source) ? SOURCE_STAR_PLATINUM : SOURCE_THE_WORLD;
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
		// 结束音按时停来源分发：来源为世界（Dio 面包 / THE_WORLD 技能，timeStopSource 非
		// star_platinum）一律播 THE_WORLD_RE；仅白金之星自身技能发动的时停播专属
		// STAR_PLATINUM_THE_WORLD_RE（修复白金之星吃面包后误播白金结束音的历史 bug）。
		if (getRemaining(player) == 20) {
			player.playSound(SOURCE_STAR_PLATINUM.equals(timeStopSource)
					? SoundLoader.STAR_PLATINUM_THE_WORLD_RE : SoundLoader.THE_WORLD_RE, 1f, 1f);
		}
	}

	private static void onHudRender(DrawContext context, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		// allowMaskTimeStop 关闭时不绘制时停遮罩与齿轮（对照原版 EventViewRender 的
		// RenderGameOverlayEvent.VIGNETTE 判定：ConfigHuaji.Stands.allowMaskTimeStop && flag）。
		if (!isTimeStopActive(mc) || !ConfigHuaji.Stands.allowMaskTimeStop) {
			return;
		}
		int width = mc.getWindow().getScaledWidth();
		int height = mc.getWindow().getScaledHeight();

		// 齿轮尺寸照搬原版：贴图 512x512 按 0.25 * timeStopScale 缩放显示，
		// 左上/右上角对齐（右上齿轮原版坐标 = width*4/scale-512）。
		// 注意：timeStopEffect 只用于时停反转特效时长（见 TimeStopPostShader），
		// 不参与此处齿轮/遮罩的透明度计算。

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// 全屏灰膜遮罩：time_stop_view 单张拉伸铺满整个窗口（UV 0~1 全贴图，
		// 绘制尺寸 = scaled 窗口宽高），固定约 30% 透明度，保留时停氛围且不遮挡视野。
		// 遮罩独立使用 SRC_ALPHA 混合，与齿轮乘法混合互不影响。
		RenderSystem.setShaderColor(1f, 1f, 1f, MASK_ALPHA);
		context.drawTexture(TIME_STOP_VIEW, 0, 0, width, height, 0.0f, 0.0f, 256, 256, 256, 256);

		// 齿轮：黑底贴图 + 乘法混合（ZERO / ONE_MINUS_SRC_COLOR，颜色 0.3,0.3,0.3,1.0），
		// 完全照搬原版 EventViewRender.renderElement，黑色在乘法混合下等于透明，黑底自动透明。
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		// GL 常量：0=GL_ZERO, 1=GL_ONE, 0x0301=GL_ONE_MINUS_SRC_COLOR
		RenderSystem.blendFuncSeparate(0, 0x0301, 1, 0);
		RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1.0f);

		MatrixStack matrices = context.getMatrices();
		float gearScale = (float) (0.25 * ConfigHuaji.Stands.timeStopScale);
		// 左上齿轮（原版：x=0,y=0，绘制 512x512 区域后整体缩放）
		matrices.push();
		matrices.scale(gearScale, gearScale, 1f);
		matrices.translate(0f, 0f, 0f);
		context.drawTexture(GEAR_1, 0, 0, 0.0f, 0.0f, 512, 512, 512, 512);
		matrices.pop();

		// 右上齿轮（原版：x=(width*4/scale)-512, y=(height*4/scale)-512）
		matrices.push();
		matrices.scale(gearScale, gearScale, 1f);
		matrices.translate((float) (width * 4.0 / ConfigHuaji.Stands.timeStopScale) - 512f,
				(float) (height * 4.0 / ConfigHuaji.Stands.timeStopScale) - 512f, 0f);
		context.drawTexture(GEAR_2, 0, 0, 0.0f, 0.0f, 512, 512, 512, 512);
		matrices.pop();

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.defaultBlendFunc();
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
