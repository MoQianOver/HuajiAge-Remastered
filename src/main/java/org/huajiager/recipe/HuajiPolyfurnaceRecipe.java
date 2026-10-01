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
 * 滑稽终极熔炉配方， astral HuaJiPolyfurnaceRecipe。
 * 字段：ingredient / experience / processTime / point（聚合池贡献值）；无 result，
 * 池满后由 TileEntity 直接产出滑稽星（huaji_star_universe）。
 */
public record HuajiPolyfurnaceRecipe(Identifier id, Ingredient ingredient, float experience, int processTime, int point) implements Recipe<Inventory> {

	@Override
	public boolean matches(Inventory inventory, World world) {
		return ingredient.test(inventory.getStack(0));
	}

	@Override
	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeLoader.HUAJI_POLYFURNACE_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE;
	}
}
