package org.huajiager.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

/**
 * ScreenHandlerType 注册入口。
 */
public class MenuLoader {

	public static final String MOD_ID = "huajiager";

	public static final ScreenHandlerType<HuajiBlenderMenu> HUAJI_BLENDER = new ScreenHandlerType<HuajiBlenderMenu>((syncId, inv) -> new HuajiBlenderMenu(syncId, inv), FeatureSet.empty());
	public static final ScreenHandlerType<HuajiPolyfurnaceMenu> HUAJI_POLYFURNACE = new ScreenHandlerType<HuajiPolyfurnaceMenu>((syncId, inv) -> new HuajiPolyfurnaceMenu(syncId, inv), FeatureSet.empty());

	public static void register() {
		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "huaji_blender"), HUAJI_BLENDER);
		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "huaji_polyfurnace"), HUAJI_POLYFURNACE);
	}
}
