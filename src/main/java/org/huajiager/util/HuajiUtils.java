package org.huajiager.util;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;

/**
 * 通用工具， （Fabric 简化）。
 *  getTagFuel 按 mod 命名空间下 "huaji_blender/time_" / "huaji_polyfurnace/time_" 前缀的
 * 物品 tag 解析数值（如 time_100 = 100），作为燃料燃烧时长 / 能量值。
 */
public class HuajiUtils {

	/** 从物品所属 tag 中解析 mod 命名空间、指定前缀后的数字；无匹配返回 0。 */
	public static int getTagFuel(ItemStack stack, String tagPrefix, int index) {
		int burn = 0;
		for (TagKey<?> tag : stack.getRegistryEntry().streamTags().toList()) {
			if (tag.id().getNamespace().equals("huajiager")) {
				String path = tag.id().getPath();
				if (path.startsWith(tagPrefix)) {
					try {
						burn = Integer.parseInt(path.substring(index));
					} catch (NumberFormatException ignored) {
						// 非数字后缀，忽略
					}
				}
			}
		}
		return burn;
	}

	public static boolean isTagFuel(ItemStack stack, String tagPrefix, int index) {
		return getTagFuel(stack, tagPrefix, index) > 0;
	}

	/** 矩形区域判定（部分机器逻辑使用），保留接口。 */
	public static boolean isInRectangularArea(double x, double y, double z, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
	}
}
