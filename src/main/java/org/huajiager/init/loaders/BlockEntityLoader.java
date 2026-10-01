package org.huajiager.init.loaders;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.tileentity.TileEntityHuajiBlender;
import org.huajiager.tileentity.TileEntityHuajiPolyfurnace;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * BlockEntity 注册器（），新建。
 *
 *  huaji_blender / huaji_poly_furnace 均携带 TileEntity。 * Fabric 1.20.1 侧在方块注册后通过本类注册 BlockEntityType，供 Block 的
 * createBlockEntity 引用（HuajiBlender / HuajiPolyfurnace 实现 BlockEntityProvider）。
 */
public class BlockEntityLoader {

	public static BlockEntityType<TileEntityHuajiBlender> HUAJI_BLENDER;
	public static BlockEntityType<TileEntityHuajiPolyfurnace> HUAJI_POLYFURNACE;

	public static void register() {
		String modId = HuajiAgeRemastered.MOD_ID;

		HUAJI_BLENDER = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(modId, "huaji_blender"),
				BlockEntityType.Builder.create(TileEntityHuajiBlender::new, BlockLoader.huajiBlender).build(null));
		HUAJI_POLYFURNACE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(modId, "huaji_poly_furnace"),
				BlockEntityType.Builder.create(TileEntityHuajiPolyfurnace::new, BlockLoader.huajiPolyFurnace).build(null));
	}
}
