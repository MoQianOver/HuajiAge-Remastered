package org.huajiager.client.event;

import org.huajiager.HuajiAgeRemastered;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

/**
 * 登录进游戏时的 Patchouli 缺失提示，
 * 。
 *
 * <p>逻辑：{@code PlayerEvent.PlayerLoggedInEvent} 中判断
 * {@code !Loader.isModLoaded("patchouli")}，未加载 Patchouli 时把本地化消息
 * {@code message.huajiage.patchouli_missing}（JSON 文本）解析为 Text 组件发送给玩家，
 * 并用静态 {@code notFirst} 保证每局游戏只提示一次；已加载则什么都不做（手册可通过
 * 配方 jo_book.json 获得）。</p>
 *
 * <p>差异：Fabric 用 {@link ClientPlayConnectionEvents#JOIN} 等价 的
 * {@code PlayerLoggedInEvent}；补丁加载判断使用
 * {@link FabricLoader#isModLoaded(String)}；客户端本地化用
 * {@link I18n translate(String)}，随后走 {@link Text.Serializer#fromJson} 还原富文本
 * (颜色/链接/悬浮)，最后通过
 * {@code player.sendMessage(text, false)} 写入聊天栏。同样以静态 notFirst 去重。</p>
 */
@Environment(EnvType.CLIENT)
public final class EventPlayerLoggedIn {

	private static boolean notFirst;

	private EventPlayerLoggedIn() {
	}

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// 与 一致：整个游戏进程只提示一次
			if (notFirst) {
				return;
			}
			notFirst = true;
			if (FabricLoader.getInstance().isModLoaded("patchouli")) {
				return;
			}
			HuajiAgeRemastered.LOGGER.info("[HuajiAgeRemastered] Patchouli 未装载，发送手册缺失提示");
			// 还原 I18n -> JSON 组件解析链路；lang 缺失/损坏时静默降级，不阻断登录
			try {
				String json = I18n.translate("message.huajiager.patchouli_missing");
				Text text = Text.Serializer.fromJson(json);
				if (text != null && client.player != null) {
					client.player.sendMessage(text, false);
				}
			} catch (Exception e) {
				HuajiAgeRemastered.LOGGER.error("[HuajiAgeRemastered] 手册缺失提示解析失败", e);
			}
		});
	}
}
