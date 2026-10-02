package org.huajiager.client.event;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.huajiager.capability.StandHandler;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.HuajiConstant;
import org.huajiager.mixin.client.MixinGameRenderer;
import org.huajiager.stand.StandUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * THE_WORLD 时停后处理滤镜（客户端）。
 *
 * 行为说明：
 * 通过 vanilla 实体渲染器的后处理加载/卸载（EntityRenderer.loadShader / stopUseShader）
 * 在时停期间按阶段挂载 minecraft 自带的 shaders/post（invert / desaturate / pencil）三档滤镜，
 * 时停结束移除。沿用 vanilla 同一机制，等价接口为
 * GameRenderer.loadPostProcessor / disablePostProcessor。
 *
 * 挂载时机：WorldRenderEvents.LAST（世界渲染末尾）只负责按进度决策加载/切换/卸载。 * 实际滤镜画面由 vanilla GameRenderer 在 renderWorld 之后、主 framebuffer 重绑定之前的
 * 后处理阶段绘制（与游戏自带的"超隐藏设置"同一条渲染链路），保证滤镜对整屏可见且顺序正确。
 *
 * 全屏滤镜设计：fsh 已移除圆形遮罩（不再有半径扩散/收缩），反色与灰色均全屏覆盖。 * 动画只由强度/饱和度渐变驱动，避免换挡重建 FBO 造成卡顿。旧实现把半径/强度烘焙进
 * 30+ 个 post json，每次换档 loadPostProcessor 重建 FBO 并重编译 GLSL；现只加载两个
 * 基础 program（timestop_inv / timestop_gray），此后每帧只更新动态 uniform
 * （JsonEffectShaderProgram.getUniformByName(...).set(...) 写入 Uniform 缓存，
 * 渲染时统一 flush，零重编译、零 FBO 重建），过渡逐渲染帧平滑推进。
 *
 * 本文件全部实现为本工程依据语义独立编写。
 */
public final class TimeStopPostShader {

	// 基础档位：inv = 反色开场（timestop_inv.json），gray = 灰色滤镜（timestop_gray.json）。
	// 每次时停实例最多加载这 2 个 post json，之后全部靠动态 uniform 驱动。
	private static final String STAGE_INV = "inv";
	private static final String STAGE_GRAY = "gray";

	private static Identifier stageId(String stage) {
		return new Identifier("huajiager", "shaders/post/timestop_" + stage + ".json");
	}

	/** 当前已加载的滤镜档位；空字符串表示未加载（时停未激活或已卸载）。 */
	private static String currentStage = "";

	/**
	 * 本次时停实例已观测到的最大剩余 tick（首观测即峰值，≈ 时停总时长-1）。
	 * 全程阶段判定（反色开场 / 灰色 / 灰淡出）均以 remaining 相对本值的偏移锚定，
	 * 不依赖世界时钟与会话状态：世界替身 / 白金之星 / Dio 面包统一同一套确定性时序，
	 * 且不随帧率、网络抖动、边缘 tick 判定翻转而漂移。
	 */
	private static int maxRemaining = 0;

	/**
	 * 本次时停实例已渲染帧数（与 vanilla RenderTickEvent 同节奏，每渲染帧一次）。
	 * 对齐原版 EventViewRender.TimeStopRenderTest 的 static ticks：
	 * 时停激活期间每帧 ++、非激活清零。开场反色窗口以帧计数判定，
	 * 避免把原版的"帧"误当"游戏 tick"，导致默认 1.5 的 150 帧被放大成
	 * 150 tick（7.5 秒）——即反色持续过长、圆扩散变慢的根因。
	 */
	private static int ticks = 0;

	/**
	 * 开场反色过渡：总时长由配置 ConfigHuaji.Stands.timeStopEffect 决定
	 * （对齐原版 EventViewRender：t0 = (int)(timeStopEffect*100) 渲染帧，反转在
	 * ticks>10 且 ticks<t0 期间加载 invert 着色器，即反转持续 timeStopEffect*100-10 帧）。
	 * 三段（扩散 / 保持 / 收缩）按 1:2:1 比例动态划分，见 {@link #openingTicks()}：
	 */

	/** 圆形遮罩半径（宽高比修正后的归一化距离）。FULL_R=2.0 足够覆盖任意常见宽高比
	 * （16:9 角落≈1.02、21:9≈1.27、32:9≈1.85），圆扩散到最大时整个屏幕被盖满，
	 * 超出屏幕的部分自然被裁掉——即用户要求的"圆形扩大到全屏"。 */
	private static final float MIN_R = 0.03f;
	private static final float FULL_R = 2.0f;

	/** 灰色滤镜开始时的淡入时长：前 10 渲染帧饱和度从接近原色压到最灰，避免反色→灰色跳变。 */
	private static final int GRAY_FADE_IN_TICKS = 10;

	/** 灰色滤镜结束前的淡出时长：最后 1 秒（20 tick）饱和度连续回升到接近原色，滤镜平滑消失。 */
	private static final int GRAY_FADE_OUT_TICKS = 20;

	/**
	 * 反色扩散/收缩各延长的渲染帧数（用户要求"扩大与收缩各再慢 0.5 秒"，
	 * 60fps 下 0.5 秒 = 30 帧）。只加扩散与收缩，保持段不变，总反色时长 +60 帧。
	 */
	private static final int OPENING_EXTRA_FRAMES = 30;

	private static final org.slf4j.Logger LOGGER =
			org.slf4j.LoggerFactory.getLogger(TimeStopPostShader.class.getName());

	private TimeStopPostShader() {
	}

	public static void register() {
		// 只负责每帧决策加载/切换/卸载与 uniform 推进；实际渲染由 vanilla GameRenderer 的后处理阶段完成
		WorldRenderEvents.LAST.register(TimeStopPostShader::onWorldRender);
	}

	private static void onWorldRender(WorldRenderContext context) {
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if (player == null || mc.world == null) {
			release();
			return;
		}
		if (!isTimeStopActive(player)) {
			release();
			return;
		}

		// 开场反色窗口以"本次时停实例渲染帧计数"锚定（对齐原版 EventViewRender：
		// t0 = (int)(timeStopEffect*100) 帧，默认 1.5 → 150 帧 ≈ 2.5s@60fps），
		// 灰色中段与最后 1s(20tick) 灰淡出仍由 remaining 相对 maxRemaining 锚定；
		// 帧计数与时停剩余 tick 各自独立推进，互不干扰，且不依赖世界时钟与会话状态。
		int remaining = getRemaining(player);
		if (remaining > maxRemaining) {
			maxRemaining = remaining;
		}
		int total = Math.max(maxRemaining, 1);
		ticks++; // 本次时停实例已渲染帧数（对齐原版 EventViewRender 的 static ticks）
		int openingTicks = openingTicks(); // 渲染帧（反色基础总长，来自配置）
		int openingTotal = openingTicks + OPENING_EXTRA_FRAMES * 2; // 扩散/收缩各 +30 帧后的反色总时长
		int openingExpand = Math.max(1, openingTicks / 4 + OPENING_EXTRA_FRAMES); // 扩散（+0.5s）
		int openingKeep = Math.max(1, openingTicks / 2); // 保持段不变
		if (ticks < openingTotal) {
			// 开场：全屏反色强度渐入→保持→渐落（每渲染帧平滑 uniform）
			applyFilter(mc, STAGE_INV);
			updateInv(ticks, openingTotal, openingExpand, openingKeep);
		} else if (remaining > GRAY_FADE_OUT_TICKS) {
			// 中段：全屏灰色（开头淡入后稳定在最灰，饱和度随剩余进度缓慢回升）
			applyFilter(mc, STAGE_GRAY);
			updateGrayFull(ticks, openingTotal, remaining, total);
		} else {
			// 最后 1 秒：全屏灰色饱和度连续回升到接近原色，滤镜平滑淡出消失
			applyFilter(mc, STAGE_GRAY);
			updateGrayFadeOut(total, remaining);
		}
	}

	/**
	 * 开场反色总时长（渲染帧）：读取 ConfigHuaji.Stands.timeStopEffect，
	 * 按原版公式 t0 = (int)(timeStopEffect * 100) 换算，单位为渲染帧
	 * （原版 EventViewRender.TimeStopRenderTest 在 RenderTickEvent 中逐帧累计，
	 * 反转窗口为 ticks>10 且 ticks<t0，即 t0-10 帧）。
	 * 默认 1.5 → 150 帧：60fps 下开场约 2.5 秒（反转窗口 140 帧 ≈ 2.33 秒），
	 * 扩散/收缩各 37 帧 ≈ 0.6 秒，符合原版节奏；在此基础上扩散与收缩再各延长
	 * OPENING_EXTRA_FRAMES=30 帧（0.5 秒），即扩散 67 / 保持 75 / 收缩 67 帧，
	 * 总反色时长 210 帧 ≈ 3.5s；用户调整配置时时长按比例变化（保持段随配置同步增长）。
	 */
	private static int openingTicks() {
		return Math.max(1, (int) (ConfigHuaji.Stands.timeStopEffect * 100));
	}

	/**
	 * 开场反色过渡：三段连续曲线按 openingTicks 的 1:2:1 划分后，扩散/收缩再各
	 * 延长 OPENING_EXTRA_FRAMES 帧（帧）——
	 * 前段扩散：圆形遮罩从中心小圆扩散到全屏覆盖（MIN_R→FULL_R），反色强度 0.20→0.93 渐入；	 * 中段保持：圆保持全屏（FULL_R），反色峰值 0.93（时长不变）；	 * 末段收缩：圆从全屏收缩回中心小圆（FULL_R→MIN_R），反色强度 0.93→0.20 渐落，
	 * 圆外背景灰化渐现（BgGray 0→1、BgSaturation 0.40→0.15），收尾视觉与灰色滤镜自然衔接。
	 */
	private static void updateInv(int ticks, int openingTotal, int openingExpand, int openingKeep) {
		float amount, radius = FULL_R, bgGray = 0.0f, bgSat = 0.40f;
		if (ticks < openingExpand) {
			// 0..expand-1 扩散 + 强度渐入
			float t = ticks / (float) (openingExpand - 1);
			radius = lerp(MIN_R, FULL_R, t);
			amount = lerp(0.20f, 0.93f, t);
		} else if (ticks < openingExpand + openingKeep) {
			// 扩散后圆保持全屏
			amount = 0.93f;
		} else {
			// 末尾：收缩 + 强度渐落 + 背景灰化渐现
			int shrink = ticks - openingExpand - openingKeep;
			int shrinkLen = Math.max(1, openingTotal - openingExpand - openingKeep - 1);
			float t = shrink / (float) shrinkLen;
			radius = lerp(FULL_R, MIN_R, t);
			amount = lerp(0.93f, 0.20f, t);
			bgGray = t;
			bgSat = lerp(0.40f, 0.15f, t);
		}
		setUniform("InverseAmount", amount);
		setUniform("Radius", radius);
		setUniform("BgGray", bgGray);
		setUniform("BgSaturation", bgSat);
	}

	/**
	 * 灰色滤镜中段（圆保持全屏覆盖）：前 10 渲染帧饱和度从 0.50（接近原色）压到 0.15（最灰）淡入，
	 * 避免反色结尾直接跳成全灰；之后稳定在最灰，随剩余进度缓慢回升（视觉过渡更顺）。
	 * 圆始终保持在 FULL_R（全屏盖满，超出裁掉），收缩只发生在最后 1 秒淡出阶段。
	 */
	private static void updateGrayFull(int ticks, int openingTotal, int remaining, int total) {
		int grayElapsed = ticks - openingTotal; // 进入灰色阶段后的渲染帧数（对齐开场反色的帧计数）
		float saturation;
		if (grayElapsed < GRAY_FADE_IN_TICKS) {
			saturation = lerp(0.50f, 0.15f, grayElapsed / (float) (GRAY_FADE_IN_TICKS - 1));
		} else {
			float progress = (float) remaining / total;
			saturation = lerp(0.15f, 0.30f, clamp01((1.0f - progress) / 0.75f));
		}
		setUniform("Saturation", saturation);
		setUniform("Radius", FULL_R);
	}

	/**
	 * 灰色滤镜结束前淡出：最后 1 秒（remaining 20..1）圆形遮罩从全屏收缩回中心小圆
	 * （FULL_R→MIN_R），饱和度同时从 0.15（最灰）回升到 0.90（接近原色）。
	 * 收缩过程中圆外露出原色画面、圆内灰色逐渐变浅，滤镜随圆缩到中心自然消失。
	 */
	private static void updateGrayFadeOut(int total, int remaining) {
		float t = clamp01(1.0f - remaining / (float) GRAY_FADE_OUT_TICKS);
		setUniform("Saturation", lerp(0.15f, 0.90f, t));
		setUniform("Radius", lerp(FULL_R, MIN_R, t));
	}

	/**
	 * 向后处理链中所有 pass 下发动态 uniform。只对声明了对应 uniform 的 pass 生效
	 * （blit 等无此 uniform 的 pass 自动跳过）。值先写入 Uniform 缓存，渲染时统一 flush。
	 * PostEffectProcessor 的 passes 为私有字段且无公开 getter，经反射读取（yarn 字段名
	 * "passes" 在运行时与编译时一致）。
	 */
	private static void setUniform(String name, float value) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null || mc.gameRenderer == null) {
			return;
		}
		PostEffectProcessor processor = mc.gameRenderer.getPostProcessor();
		if (processor == null) {
			return;
		}
		for (PostEffectPass pass : getPasses(processor)) {
			JsonEffectShaderProgram program = pass.getProgram();
			if (program == null) {
				continue;
			}
			GlUniform uniform = program.getUniformByName(name);
			if (uniform != null) {
				uniform.set(value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static List<PostEffectPass> getPasses(PostEffectProcessor processor) {
		try {
			Field f = PostEffectProcessor.class.getDeclaredField("passes");
			f.setAccessible(true);
			return (List<PostEffectPass>) f.get(processor);
		} catch (ReflectiveOperationException ex) {
			// 反射失败时静默跳过 uniform 更新（滤镜仍按 json 静态值渲染，不崩溃）
			LOGGER.warn("[TimeStopPostShader] cannot access post passes: {}", ex.toString());
			return Collections.emptyList();
		}
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static float clamp01(float v) {
		return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
	}

	/** 与 EventTimeStopView 同源的时停激活判定。 */
	private static boolean isTimeStopActive(PlayerEntity player) {
		StandHandler handler = StandUtil.getStandHandler(player);
		return handler != null
				&& HuajiConstant.BuffTags.TIME_STOP.equals(handler.getBuffTag())
				&& handler.getBuffer() > 0;
	}

	private static int getRemaining(PlayerEntity player) {
		StandHandler handler = StandUtil.getStandHandler(player);
		return handler == null ? 0 : handler.getBuffer();
	}

	/**
	 * 通过 vanilla GameRenderer 的后处理加载接口应用目标档位：
	 * 加载时会先关闭旧处理器再创建新处理器并启用后处理，档位切换/首次加载都走这里。	 * 渲染由 vanilla 在后处理阶段（world 之后）统一完成。
	 */
	private static void applyFilter(MinecraftClient mc, String stage) {
		GameRenderer renderer = mc.gameRenderer;
		// vanilla 视角切换（F5）、死亡重生、骑乘/下马、切换维度等都会触发
		// GameRenderer.onCameraEntitySet：它会直接 close 并把 postProcessor 置空（玩家不重载）。
		// 若 currentStage 与目标一致但 vanilla 后处理器已被清空，必须强制重载，
		// 否则 currentStage 去重会把"已丢失的滤镜"误短路为"不再加载"，导致滤镜消失且后续忽明忽暗。
		if (stage.equals(currentStage) && renderer.getPostProcessor() != null) {
			return;
		}
		Identifier id = stageId(stage);
		try {
			((MixinGameRenderer) renderer).invokeLoadPostProcessor(id);
			currentStage = stage;
		} catch (Throwable ex) {
			// 资源缺失 / JSON 异常时静默降级，仅记录一次
			LOGGER.warn("[TimeStopPostShader] load {} failed: {}", id, ex.toString());
			currentStage = "";
			maxRemaining = 0;
			ticks = 0;
		}
	}

	/** 时停结束移除滤镜，并重置帧计数器 / 剩余峰值。 */
	private static void release() {
		ticks = 0;
		maxRemaining = 0;
		if (currentStage.isEmpty()) {
			return;
		}
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc != null && mc.gameRenderer != null) {
			mc.gameRenderer.disablePostProcessor();
		}
		currentStage = "";
	}
}
