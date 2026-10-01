package org.huajiager.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

/**
 * 合成配方序列化器。
 * JSON 字段与 crafting_shapeless 一致：ingredients / result / group / category，
 * 但 type 使用 huajiager:multi_knife_shiny，由 MultiKnifeShinyRecipe.craft() 写入 light:true。
 */
public class MultiKnifeShinyRecipeSerializer implements RecipeSerializer<MultiKnifeShinyRecipe> {

	@Override
	public MultiKnifeShinyRecipe read(Identifier id, JsonObject json) {
		String group = JsonHelper.getString(json, "group", "");
		CraftingRecipeCategory category = parseCategory(JsonHelper.getString(json, "category", "misc"));
		DefaultedList<Ingredient> input = readIngredients(JsonHelper.getArray(json, "ingredients"));
		// 1.20.1 官方解析器本身不读 result 的 nbt，result 只需取普通 multi_knife
		ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
		return new MultiKnifeShinyRecipe(id, group, category, result, input);
	}

	private static DefaultedList<Ingredient> readIngredients(JsonArray json) {
		DefaultedList<Ingredient> defaultedList = DefaultedList.of();
		for (int i = 0; i < json.size(); ++i) {
			Ingredient ingredient = Ingredient.fromJson(json.get(i));
			if (!ingredient.isEmpty()) {
				defaultedList.add(ingredient);
			}
		}
		return defaultedList;
	}

	private static CraftingRecipeCategory parseCategory(String id) {
		for (CraftingRecipeCategory category : CraftingRecipeCategory.values()) {
			if (category.asString().equals(id)) {
				return category;
			}
		}
		return CraftingRecipeCategory.MISC;
	}

	@Override
	public MultiKnifeShinyRecipe read(Identifier id, PacketByteBuf buf) {
		String group = buf.readString();
		CraftingRecipeCategory category = buf.readEnumConstant(CraftingRecipeCategory.class);
		int i = buf.readVarInt();
		DefaultedList<Ingredient> input = DefaultedList.ofSize(i, Ingredient.EMPTY);
		for (int j = 0; j < i; ++j) {
			input.set(j, Ingredient.fromPacket(buf));
		}
		ItemStack result = buf.readItemStack();
		return new MultiKnifeShinyRecipe(id, group, category, result, input);
	}

	@Override
	public void write(PacketByteBuf buf, MultiKnifeShinyRecipe recipe) {
		buf.writeString(recipe.getGroup());
		buf.writeEnumConstant(recipe.getCategory());
		buf.writeVarInt(recipe.getIngredients().size());
		for (Ingredient ingredient : recipe.getIngredients()) {
			ingredient.write(buf);
		}
		buf.writeItemStack(recipe.getResultStack());
	}
}
