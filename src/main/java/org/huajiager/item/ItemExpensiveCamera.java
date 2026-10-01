package org.huajiager.item;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.text.Text;

/**
 * 昂贵相机， （独立编写）。
 *
 * 右击念写语义：隐者之紫持有者主手持相机右击 -> 判断能量 -> 服务端播放念写音效并向副手
 * 物品施加 telepathize 标记，能量不足时破碎相机。
 *
 * tooltip：main 源集（splitEnvironmentSourceSets）不含 client 屏幕类，无法用
 * Screen.hasShiftDown() 实现按住 Shift 展开第二段。详情文本交由 createDetailedTooltip()
 * 提供，由 client 源集 ItemTooltipHandlers 按 Shift 状态展开（未按 Shift 显示统一短提示）。
 */
public class ItemExpensiveCamera extends Item {

	public ItemExpensiveCamera() {
		super(new Item.Settings().maxCount(64));
	}

	/**
	 * 按住 Shift 时展示的完整说明文本（由 ItemTooltipHandlers 客户端事件调用）。
	 * 两段同时保留：第一段为物品描述，第二段为念写用途说明。
	 */
	public static List<Text> createDetailedTooltip() {
		return List.of(
				Text.translatable("item.huajiager.expensive_camera.tooltips.1"),
				Text.translatable("item.huajiager.expensive_camera.tooltips.2"));
	}
}
