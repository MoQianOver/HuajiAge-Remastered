package org.huajiager.init.loaders;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.block.HuajiBlender;
import org.huajiager.block.HuajiBomb;
import org.huajiager.block.HuajiPolyfurnace;
import org.huajiager.block.HuajiStarBlock;
import org.huajiager.block.HuajiStarBlockSky;
import org.huajiager.block.OreHuaji;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 方块注册器。
 *
 * Fabric 侧按注册名注册 Block + BlockItem（ BlockLoader 为 ore_huaji /
 * huaji_star_block 均注册了 ItemBlock 进物品栏），并通过 ItemGroupEvents
 * 接入现有创造标签页 huajiager:huajiage（ItemLoader 已注册）。
 */
public class BlockLoader {

	public static Block oreHuaji;
	public static Block huajiStarBlock;
	public static Block huajiStarBlockSky;
	public static Block huajiBomb;
	public static Block huajiBlender;
	public static Block huajiPolyFurnace;

	public static Item oreHuajiItem;
	public static Item huajiStarBlockItem;
	public static Item huajiStarBlockSkyItem;
	public static Item huajiBombItem;
	public static Item huajiBlenderItem;
	public static Item huajiPolyFurnaceItem;

	public static void register() {
		String modId = HuajiAgeRemastered.MOD_ID;

		oreHuaji = new OreHuaji();
		huajiStarBlock = new HuajiStarBlock();
		huajiStarBlockSky = new HuajiStarBlockSky();
		huajiBomb = new HuajiBomb();
		huajiBlender = new HuajiBlender();
		huajiPolyFurnace = new HuajiPolyfurnace();

		Registry.register(Registries.BLOCK, Identifier.of(modId, "ore_huaji"), oreHuaji);
		Registry.register(Registries.BLOCK, Identifier.of(modId, "huaji_star_block"), huajiStarBlock);
		Registry.register(Registries.BLOCK, Identifier.of(modId, "huaji_star_block_sky"), huajiStarBlockSky);
		Registry.register(Registries.BLOCK, Identifier.of(modId, "huaji_bomb"), huajiBomb);
		Registry.register(Registries.BLOCK, Identifier.of(modId, "huaji_blender"), huajiBlender);
		Registry.register(Registries.BLOCK, Identifier.of(modId, "huaji_poly_furnace"), huajiPolyFurnace);

		oreHuajiItem = new BlockItem(oreHuaji, new Item.Settings());
		huajiStarBlockItem = new BlockItem(huajiStarBlock, new Item.Settings());
		huajiStarBlockSkyItem = new BlockItem(huajiStarBlockSky, new Item.Settings());
		huajiBombItem = new BlockItem(huajiBomb, new Item.Settings());
		huajiBlenderItem = new BlockItem(huajiBlender, new Item.Settings());
		huajiPolyFurnaceItem = new BlockItem(huajiPolyFurnace, new Item.Settings());
		Registry.register(Registries.ITEM, Identifier.of(modId, "ore_huaji"), oreHuajiItem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_block"), huajiStarBlockItem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_block_sky"), huajiStarBlockSkyItem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_bomb"), huajiBombItem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_blender"), huajiBlenderItem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_poly_furnace"), huajiPolyFurnaceItem);

		// 接入主创造标签页 tabhuaji（ItemLoader 已注册 huajiager:huaji）
		ItemGroupEvents.modifyEntriesEvent(
				RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(modId, "huaji")))
				.register(entries -> {
					entries.add(oreHuajiItem);
					entries.add(huajiStarBlockItem);
					entries.add(huajiStarBlockSkyItem);
					entries.add(huajiBombItem);
					entries.add(huajiBlenderItem);
					entries.add(huajiPolyFurnaceItem);
				});

		// BlockEntity 注册（huaji_blender / huaji_poly_furnace 均携带 TE）
		BlockEntityLoader.register();
	}
}
