package org.huajiager.common.world.gen;

import org.huajiager.HuajiAgeRemastered;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

/**
 * 世界生成： OreGenEventHandler。
 *
 * 行为：主世界（dim=0）每 chunk 2 次、Y 5~14 生成 ore_huaji 矿脉（WorldGenMinable size 4），
 * 同时伴生生成 GLOWSTONE 矿脉；并需 biome rainfall 随机判定。
 * 1.20.1 Fabric 方式：ConfiguredFeature / PlacedFeature 由数据包 worldgen JSON 定义
 * （data/huajiager/worldgen/{configured_feature,placed_feature}），代码仅用 BiomeModifications
 * 简化为全主世界生成。
 */
public class OreGenEventHandler {

	public static void register() {
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE,
						Identifier.of(HuajiAgeRemastered.MOD_ID, "ore_huaji")));

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE,
						Identifier.of(HuajiAgeRemastered.MOD_ID, "ore_huaji_glowstone")));
	}
}
