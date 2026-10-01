package org.huajiager.recipe;

import org.huajiager.item.ItemMultiKnife;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * multi_knife_shiny工作台合成配方。
 *
 * <p>1.20.1  crafting_shapeless / crafting_shaped 的 result 解析只认 item/count/data，
 * 不支持 nbt 字段，因此合成结果永远拿不到 light:true。
 * 这里用自定义 Recipe 在 craft() 阶段手动写入 NBT，等价于 配方行为。
 */
public class MultiKnifeShinyRecipe implements CraftingRecipe {

	private final Identifier id;
	private final String group;
	private final CraftingRecipeCategory category;
	private final ItemStack result;
	private final DefaultedList<Ingredient> input;

	public MultiKnifeShinyRecipe(Identifier id, String group, CraftingRecipeCategory category,
			ItemStack result, DefaultedList<Ingredient> input) {
		this.id = id;
		this.group = group;
		this.category = category;
		this.result = result;
		this.input = input;
	}

	@Override
	public boolean matches(RecipeInputInventory inventory, World world) {
		RecipeMatcher recipeMatcher = new RecipeMatcher();
		int i = 0;
		for (int j = 0; j < inventory.size(); ++j) {
			ItemStack itemStack = inventory.getStack(j);
			if (!itemStack.isEmpty()) {
				++i;
				recipeMatcher.addInput(itemStack, 1);
			}
		}
		return i == this.input.size() && recipeMatcher.match(this, null);
	}

	@Override
	public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
		return ItemMultiKnife.setLight(result.copy(), true);
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		// 配方书展示为发光刀
		return ItemMultiKnife.setLight(result.copy(), true);
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return input;
	}

	/** 配方 JSON 中的原始 result（普通 multi_knife），同步用。 */
	public ItemStack getResultStack() {
		return result;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeLoader.HUAJI_MULTI_KNIFE_SHINY_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public CraftingRecipeCategory getCategory() {
		return category;
	}
}
