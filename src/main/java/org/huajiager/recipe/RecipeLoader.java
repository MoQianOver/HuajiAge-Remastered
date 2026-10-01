package org.huajiager.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 配方注册入口：搅拌机 / 终极熔炉的 RecipeType 与 RecipeSerializer。
 */
public class RecipeLoader {

	public static final String MOD_ID = "huajiager";

	public static final RecipeType<HuajiBlenderRecipe> HUAJI_BLENDER_RECIPE_TYPE = new RecipeType<>() {};
	public static final RecipeSerializer<HuajiBlenderRecipe> HUAJI_BLENDER_RECIPE_SERIALIZER = new HuajiBlenderRecipeSerializer();

	public static final RecipeType<HuajiPolyfurnaceRecipe> HUAJI_POLYFURNACE_RECIPE_TYPE = new RecipeType<>() {};
	public static final RecipeSerializer<HuajiPolyfurnaceRecipe> HUAJI_POLYFURNACE_RECIPE_SERIALIZER = new HuajiPolyfurnaceRecipeSerializer();

	/** 神圣多刀工作台合成：1.20.1  result 不写 NBT，用自定义 Serializer 在 craft() 写入 light:true。 */
	public static final RecipeSerializer<MultiKnifeShinyRecipe> HUAJI_MULTI_KNIFE_SHINY_SERIALIZER = new MultiKnifeShinyRecipeSerializer();

	public static void register() {
		Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, "huaji_blender"), HUAJI_BLENDER_RECIPE_TYPE);
		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "huaji_blender"), HUAJI_BLENDER_RECIPE_SERIALIZER);
		Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, "huaji_polyfurnace"), HUAJI_POLYFURNACE_RECIPE_TYPE);
		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "huaji_polyfurnace"), HUAJI_POLYFURNACE_RECIPE_SERIALIZER);
		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "multi_knife_shiny"), HUAJI_MULTI_KNIFE_SHINY_SERIALIZER);

		// 1.20.1 会全量同步数据包配方到客户端，自定义 RecipeType/Serializer 正常注册即可。
	}
}
