package org.huajiager.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemArrowRequiem;
import org.huajiager.item.ItemArrowStand;
import org.huajiager.item.ItemBlancedHelmet;
import org.huajiager.item.ItemDioBread;
import org.huajiager.item.ItemExpensiveCamera;
import org.huajiager.item.ItemHeroBow;
import org.huajiager.item.ItemOrgaRequiem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;

/**
 * 觉醒之箭 / 虫箭 / 昂贵相机 / Dio 面包的「按住 Shift 查看详细说明」tooltip 交互。
 *
 * 背景：/引擎场景下物品 tooltip 可在服务端共享代码里用
 * Screen.hasShiftDown() 判断 Shift——但本工程启用了 Fabric splitEnvironmentSourceSets，
 * src/main 公共源集不含 client 屏幕类，直接引用 Screen 会编译失败。因此此交互统一下沉到
 * client 源集，通过 Fabric 的 ItemTooltipCallback 客户端渲染事件实现：
 *  - 未按 Shift：仅显示一行短提示（§7按住Shift以查看更多信息）。 *  - 按住 Shift：追加 {Item} 类 createDetailedTooltip() 提供的完整说明文本。
 *
 * ItemTooltipCallback 在物品渲染时于客户端被调用（Screen.hasShiftDown 合法），
 * 与 的 addInformation + GuiUtils 传参语义一致。
 */
public final class ItemTooltipHandlers {

	private static final String KEY_TOOLTIP_HINT = "item.huajiager.tooltips.hint";

	/** 特异点警告行彩虹循环色（对齐 EventToolTip 的 6 色序列）。 */
	private static final Formatting[] SINGULARITY_COLORS = {
			Formatting.RED, Formatting.GOLD, Formatting.YELLOW,
			Formatting.GREEN, Formatting.BLUE, Formatting.AQUA
	};

	private static int singularityTick = 0;
	private static int singularityColor = 0;

	private ItemTooltipHandlers() {
	}

	public static void register() {
		// 特异点警告行色相滚动：每 2 tick 平移一色
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (++singularityTick >= 2) {
				singularityTick = 0;
				if (--singularityColor < 0) {
					singularityColor = SINGULARITY_COLORS.length - 1;
				}
			}
		});
		// 1.20.1 的 ItemTooltipCallback 签名为 (ItemStack, Item.TooltipContext, List<Text>)
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
			if (stack.isOf(ItemLoader.arrowStand)) {
				appendDetailTooltip(lines, ItemArrowStand.createDetailedTooltip());
			} else if (stack.isOf(ItemLoader.arrowRequiem)) {
				appendDetailTooltip(lines, ItemArrowRequiem.createDetailedTooltip());
			} else if (stack.isOf(ItemLoader.expensiveCamera)) {
				appendDetailTooltip(lines, ItemExpensiveCamera.createDetailedTooltip());
			} else if (stack.isOf(ItemLoader.dioBread)) {
				appendDetailTooltip(lines, ItemDioBread.createDetailedTooltip());
			} else if (stack.isOf(ItemLoader.orgaRequiem)) {
				// 奥尔加镇魂曲：无 Shift 时也显示绑定持有者行 + 摘要；Shift 时追加详细说明。
				lines.add(ItemOrgaRequiem.createOwnerLine(stack));
				if (Screen.hasShiftDown()) {
					lines.addAll(ItemOrgaRequiem.createDetailedTooltip());
				} else {
					lines.add(ItemOrgaRequiem.createSummaryLine());
				}
			} else if (stack.isOf(ItemLoader.heroBow)) {
				// 大英雄之弓：Shift 显示能力表 1.1~1.4，否则显示提示 2.desc；底部恒显宝具 3.desc
				if (Screen.hasShiftDown()) {
					lines.addAll(ItemHeroBow.createDetailedTooltip());
				} else {
					lines.add(Text.translatable("item.hero_bow:unicode_tooltips.2.desc"));
				}
				lines.add(Text.translatable("item.hero_bow:unicode_tooltips.3.desc"));
			} else if (stack.isOf(ItemLoader.blanceHelmet)) {
				// 五五开头盔：非 Shift 显示"按住Shift以查看更多"；Shift 时已激活（active）显示
				// 切换键提示，未激活显示挂者核心提示（3.desc 已由主类 appendTooltip 恒定追加）
				if (Screen.hasShiftDown()) {
					if (ItemBlancedHelmet.isActive(stack)) {
						// 模板 KeyLoader.MODE_SWITCH 默认 K
						lines.add(Text.translatable("item.blance_helmet:changeMode", "K"));
					} else {
						lines.add(Text.translatable("item.blance_helmet:unicode_tooltips.2.desc"));
					}
				} else {
					lines.add(Text.translatable("item.blance_helmet:unicode_tooltips.1.desc"));
				}
			} else if (isOrgaSuitStack(stack)) {
				// 奥尔加四件套：庆贺吧台词 + 套装列表四行；已装备的对应行黄色高亮，
				// 未装备灰色。高亮需要客户端玩家装备槽信息，故统一在 client 源集追加。
				appendOrgaSuitTooltip(lines, stack);
			} else if (stack.isOf(ItemLoader.singularity)) {
				// 特异点：警告行逐字彩虹色循环滚动（对齐 EventToolTip，
				// 主类 appendTooltip 不再输出，避免白色静态行与彩虹行重复）
				lines.add(createRainbowLine(
						Text.translatable("item.singularity.tooltip.warning").getString()));
			} else {
				return;
			}
			// 自动换行收尾：上述物品的详细说明多为超长单行翻译串，item tooltip 按最长行
			// 决定整体宽度，长行会把 tooltip 顶出屏幕右缘。这里把每个 Text 按其本地化后
			// 的渲染宽度做软换行，保证 tooltip 不超出屏幕宽度（左右各留 12px 边距）。
			wrapToScreenWidth(lines);
		});
	}

	/**
	 * 将工具提示行按屏幕宽度自动换行：单行超出可用宽度时按字符拆分并保留原样式。
	 * 仅对本模组自加的长说明行生效（普通 Minecraft 行通常足够短，不会被拆）。
	 *
	 * 关键点：不能再用 line.getString() 拆——getString 只返回纯文本，会把字面上
	 * 的颜色/样式（sibling 的 formatted 颜色、遗留 § 码）全部剥离，导致"没换行的
	 * 部分还是橙色，换行后变回白色"。这里用 visit 逐段取出 (Style, 字符串)，再经
	 * TextVisitFactory.visitFormatted 把字符串内嵌的 § 码解析到每个字符的 Style 上，
	 * 按渲染宽度断行后每段仍携带自身样式，换行后颜色不丢失。
	 */
	private static void wrapToScreenWidth(List<Text> lines) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;
		int maxWidth = mc.getWindow().getScaledWidth() - 24;
		if (maxWidth <= 0) {
			return;
		}
		List<Text> wrapped = new ArrayList<>();
		for (Text line : lines) {
			// 整行渲染宽度未超限则原样保留（含原样式结构）
			if (renderer.getWidth(line) <= maxWidth) {
				wrapped.add(line);
				continue;
			}
			// 先按 Text 结构逐段取出 (style, 字符串)，再用 TextVisitFactory 解析字符串
			// 内嵌的 § 格式码（如 §6），展开成 (style, char) 序列。这样颜色/格式被绑定
			// 到每个字符自身的 Style 上，断行后新行仍携带样式，不会再变回白色。
			List<StyledChar> chars = new ArrayList<>();
			line.visit((style, str) -> {
				if (!str.isEmpty()) {
					TextVisitFactory.visitFormatted(str, 0, style, (index, s, c) -> {
						chars.add(new StyledChar(s, (char) c));
						return true;
					});
				}
				// 必须返回 Optional.empty() 而非 null：StringVisitable.styled().visit()
				// 会原样透传 visitor 的返回值，TranslatableTextContent.visit 内部对其
				// 调 isPresent()，返回 null 会直接 NPE（打开物品栏重建搜索索引时触发）。
				return Optional.empty();
			}, Style.EMPTY);
			// 逐字符贪心断行，保持每段文本携带自己的样式
			// row = 当前行已放入的样式化片段；seg/segStyle = 正在累积的连续同样式片段
			List<Text> row = new ArrayList<>();
			StringBuilder seg = new StringBuilder();
			Style segStyle = Style.EMPTY;
			int width = 0;
			for (StyledChar sc : chars) {
				if (sc.ch == '\n') {
					flushRow(row, seg, segStyle, wrapped);
					width = 0;
					continue;
				}
				String ch = String.valueOf(sc.ch);
				int w = renderer.getWidth(ch);
				if (width > 0 && width + w > maxWidth) {
					flushRow(row, seg, segStyle, wrapped);
					width = 0;
				}
				// 样式变化时把上一段收尾，开启新样式片段
				if (!segStyle.equals(sc.style)) {
					if (seg.length() > 0) {
						row.add(Text.literal(seg.toString()).setStyle(segStyle));
						seg.setLength(0);
					}
					segStyle = sc.style;
				}
				seg.append(sc.ch);
				width += w;
			}
			flushRow(row, seg, segStyle, wrapped);
		}
		lines.clear();
		lines.addAll(wrapped);
	}

	/** 把当前行的片段（row + 收尾中的 seg）合并为一个 Text flush 进 wrapped。 */
	private static void flushRow(List<Text> row, StringBuilder seg, Style segStyle, List<Text> wrapped) {
		if (seg.length() > 0) {
			row.add(Text.literal(seg.toString()).setStyle(segStyle));
			seg.setLength(0);
		}
		if (row.isEmpty()) {
			return;
		}
		if (row.size() == 1) {
			wrapped.add(row.get(0));
		} else {
			MutableText merged = Text.literal("");
			for (Text part : row) {
				merged.append(part);
			}
			wrapped.add(merged);
		}
		row.clear();
	}

	/** visitFormatted 展开后的单个样式化字符。 */
	private record StyledChar(Style style, char ch) {
	}

	/** 将字符串逐字渲染为彩虹色 Text：第 i 个字符取色相偏移 (singularityColor + i)，
	 *  颜色随客户端 tick 滚动形成循环动画（对齐 EventToolTip.onSingularityTooltip）。 */
	private static Text createRainbowLine(String str) {
		MutableText result = Text.literal("");
		for (int i = 0; i < str.length(); i++) {
			result.append(Text.literal(String.valueOf(str.charAt(i)))
					.formatted(SINGULARITY_COLORS[(singularityColor + i) % SINGULARITY_COLORS.length]));
		}
		return result;
	}

	private static void appendDetailTooltip(List<Text> lines, List<Text> detail) {
		if (Screen.hasShiftDown()) {
			lines.addAll(detail);
		} else {
			lines.add(Text.translatable(KEY_TOOLTIP_HINT));
		}
	}

	/** 是否为奥尔加四件套（发型 / 希望之衣 / 希望之裤 / 黑色高档皮鞋）。 */
	private static boolean isOrgaSuitStack(ItemStack stack) {
		return stack.isOf(ItemLoader.orgaHair)
				|| stack.isOf(ItemLoader.orgaChestplate)
				|| stack.isOf(ItemLoader.orgaLeggings)
				|| stack.isOf(ItemLoader.orgaBoots);
	}

	/**
	 * 奥尔加四件套统一 tooltip：庆贺吧台词（金色）+ 套装列表四行。
	 * 已装备的对应行黄色高亮（与套装 addInformation 的
	 * hasArmorSetItem 行为），未装备灰色。高亮依赖客户端玩家装备槽，
	 * 故在 client 源集实现（ItemTooltipCallback 渲染时可取 MinecraftClient.player）。
	 */
	private static void appendOrgaSuitTooltip(List<Text> lines, ItemStack stack) {
		PlayerEntity player = MinecraftClient.getInstance().player;
		lines.add(Text.translatable("huajiager.orga.1").formatted(Formatting.GOLD));
		lines.add(Text.translatable("item.huajiager.orgaHair.name")
				.formatted(isOrgaEquipped(player, EquipmentSlot.HEAD, ItemLoader.orgaHair)
						? Formatting.YELLOW : Formatting.GRAY));
		lines.add(Text.translatable("item.huajiager.orgaChestplate.name")
				.formatted(isOrgaEquipped(player, EquipmentSlot.CHEST, ItemLoader.orgaChestplate)
						? Formatting.YELLOW : Formatting.GRAY));
		lines.add(Text.translatable("item.huajiager.orgaLeggings.name")
				.formatted(isOrgaEquipped(player, EquipmentSlot.LEGS, ItemLoader.orgaLeggings)
						? Formatting.YELLOW : Formatting.GRAY));
		lines.add(Text.translatable("item.huajiager.orgaBoots.name")
				.formatted(isOrgaEquipped(player, EquipmentSlot.FEET, ItemLoader.orgaBoots)
						? Formatting.YELLOW : Formatting.GRAY));
	}

	private static boolean isOrgaEquipped(PlayerEntity player, EquipmentSlot slot, Item item) {
		return player != null && player.getEquippedStack(slot).isOf(item);
	}
}
