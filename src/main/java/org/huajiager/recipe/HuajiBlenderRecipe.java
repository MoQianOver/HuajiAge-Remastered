package org.huajiager.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * 滑稽搅拌机配方， astral HuaJiBlenderRecipe。
 * 字段：ingredient（输入）/ result（输出）/ experience / processTime。
 */
public record HuajiBlenderRecipe(Identifier id, Ingredient ingredient, ItemStack result, float experience, int processTime) implements Recipe<Inventory> {

	@Override
	public boolean matches(Inventory inventory, World world) {
		return ingredient.test(inventory.getStack(0));
	}

	@Override
	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		return result.copy();
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return result;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeLoader.HUAJI_BLENDER_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeLoader.HUAJI_BLENDER_RECIPE_TYPE;
	}
}
