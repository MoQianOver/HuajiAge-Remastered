package org.huajiager.client.event;

import org.huajiager.api.IStandState;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.client.KeyLoader;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.custom.StandStateCustom;
import org.huajiager.stand.custom.StandStateInfo;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * 替身信息/能量 HUD， 
 * 的 onRenderOverlay（RenderGameOverlayEvent.ElementType.HOTBAR）分支。
 *
 * <p>展示内容保持一致：替身名 / 阶段 / 模式（状态机状态）/ 精神力（能量 charge/max，
 * 能量足够召唤时置青绿色），并在左上角给出替身按键提示（放出收回 / 技能 / 切换状态）。
 * 的 RenderGameOverlayEvent 在 Fabric 侧等价为 HudRenderCallback。</p>
 *
 * <p>差异（裁剪）：
 * <ul>
 *   <li>在信息块左上绘制替身 Disc 图标（StandUtil.getDiscTex + drawModalRectWithCustomSizedTexture），
 *       该方法依赖未的自定义替身资源加载且当前资源目录无 disc 贴图，本实现暂跳过图标绘制；</li>
 *   <li>替身显示名：经 I18n 本地化（stand.huajiage.&lt;name&gt;），StandUtil.getLocalName
 *       当前为裁剪版直接返回注册名，HUD 沿用该实现；</li>
 *   <li>提示文案使用 Ctrl+ 组合修饰符（键名由 Keyboard.getKeyName 拼接）；后按键
 *       已改为独立单键绑定（见 EventStandKey），此处直接显示实际绑定键名，不再加 “Ctrl+” 前缀。</li>
 * </ul>
 * </p>
 */
public final class EventStandHudRender {

	private static final int COLOR_WHITE = 0xFFFFFF;
	private static final int COLOR_CHARGE_OK = 0x00FFFC;

	private EventStandHudRender() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(EventStandHudRender::render);
	}

	private static void render(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return;
		}

		IExposedData data = StandUtil.getStandData(player);
		StandBase stand = StandUtil.getType(player);
		StandHandler chargeHandler = StandUtil.getStandHandler(player);
		if (data == null || stand == null || chargeHandler == null) {
			return;
		}
		// 未持有替身（EMPTY）时不绘制，保持一致
		if (StandLoader.EMPTY.equals(data.getStand())) {
			return;
		}

		int stage = data.getStage();
		String state = data.getState();
		int charge = chargeHandler.getChargeValue();
		int maxCharge = chargeHandler.getMaxValue();
		int cost = stand.getCost();

		// 信息块锚点：以 displayWidth/displayHeight 与 guiScale 换算，
		// 此处用 scaled 窗口尺寸按 standHUDx / standHUDy 比例（guiScale=2 时与等价）。
		int x = (int) (ConfigHuaji.Stands.standHUDx * client.getWindow().getScaledWidth());
		int y = (int) (ConfigHuaji.Stands.standHUDy * client.getWindow().getScaledHeight());

		TextRenderer textRenderer = client.textRenderer;
		int off = 16;

		// —— 右侧替身信息块（坐标与 onRenderOverlay 保持一致）——
		// 标签：替身
		draw(context, textRenderer, Text.translatable("stand.huajiage.name").getString(), 8 + x, 2 + off + y);
		// 替身显示名（localName 为 lang key，translatable 本地化为中文）
		draw(context, textRenderer,
				Text.translatable(StandUtil.getLocalName(stand)).getString(), 13 + x, 10 + off + y);
		// 阶段
		draw(context, textRenderer, Text.translatable("stand.huajiage.stage").getString() + "  " + stage, 8 + x, 20 + off + y);
		// 模式（状态机状态）：自定义替身状态用 JS 声明的 stateKey（如
		// stand.huajiager.crazy_diamond.heal，与 lang 键一致），避免拼出 lang 中
		// 不存在的 "stand.state.huajiage.heal" 显示原始 key；原生替身状态保持原前缀。
		String stateKey = "stand.state.huajiage." + state;
		IStandState stateObj = StandStates.getStandState(stand.getName(), state);
		if (stateObj instanceof StandStateCustom custom) {
			StandStateInfo info = custom.getStateInfo();
			if (info != null && info.getName() != null && !info.getName().isEmpty()) {
				stateKey = info.getName();
			}
		}
		draw(context, textRenderer, Text.translatable("stand.huajiage.state").getString() + "  "
				+ Text.translatable(stateKey).getString(), 8 + x, 30 + off + y);
		// 精神力：能量足够召唤时青色，否则白色（与 canBeCost 判定一致）
		boolean canCost = chargeHandler.canBeCost(cost);
		draw(context, textRenderer,
				Text.translatable("stand.huajiage.mp").getString() + "  " + charge + "/" + maxCharge,
				8 + x, 40 + off + y, canCost ? COLOR_CHARGE_OK : COLOR_WHITE);

		// —— 左上角按键提示——
		// 技能提示与切换提示统一在「替身已召唤（isTriggered）」时显示：
		// 裁剪版无 stage 推进机制（stage 恒为 0），若沿用 stage>0 门槛，
		// 技能提示将永远不显示；此处与 EventStandKey.performSkill 已放开
		// 的「召唤后即可用技能」判定保持一致。
		if (ConfigHuaji.Stands.allowStandTip) {
			String keyUp = KeyLoader.STAND_UP.getBoundKeyLocalizedText().getString();
			draw(context, textRenderer, Text.translatable("stand.huajiage.tip", keyUp).getString(), 5, 0);
			if (data.isTriggered()) {
				String keySkill = KeyLoader.STAND_SKILL.getBoundKeyLocalizedText().getString();
				draw(context, textRenderer, Text.translatable("stand.huajiage.tip.skill", keySkill).getString(), 5, 10);
				String keySwitch = KeyLoader.STAND_SWITCH.getBoundKeyLocalizedText().getString();
				draw(context, textRenderer, Text.translatable("stand.huajiage.tip.mode", keySwitch).getString(), 5, 20);
			}
		}

		// —— 已觉醒（stage>0）时展示技能消耗 ——
		if (stage > 0) {
			draw(context, textRenderer, Text.translatable("stand.huajiage.tip.cost", cost).getString(), 8 + x, 50 + off + y);
		}
	}

	private static void draw(DrawContext context, TextRenderer textRenderer, String text, int x, int y) {
		draw(context, textRenderer, text, x, y, COLOR_WHITE);
	}

	private static void draw(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color) {
		context.drawTextWithShadow(textRenderer, Text.literal(text), x, y, color);
	}
}
