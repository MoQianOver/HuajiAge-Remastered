package org.huajiager.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * 搅拌机配方序列化器，JSON 字段：ingredient / result / experience / processTime。
 */
public class HuajiBlenderRecipeSerializer implements RecipeSerializer<HuajiBlenderRecipe> {

	@Override
	public HuajiBlenderRecipe read(Identifier id, JsonObject json) {
		Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
		float experience = JsonHelper.getFloat(json, "experience", 0.0F);
		int processTime = JsonHelper.getInt(json, "processTime", 100);
		String resultStr = JsonHelper.getString(json, "result");
		Identifier resultId = Identifier.tryParse(resultStr);
		Item resultItem = Registries.ITEM.get(resultId);
		return new HuajiBlenderRecipe(id, ingredient, new ItemStack(resultItem), experience, processTime);
	}

	@Override
	public HuajiBlenderRecipe read(Identifier id, PacketByteBuf buf) {
		Ingredient ingredient = Ingredient.fromPacket(buf);
		ItemStack result = buf.readItemStack();
		float experience = buf.readFloat();
		int processTime = buf.readVarInt();
		return new HuajiBlenderRecipe(id, ingredient, result, experience, processTime);
	}

	@Override
	public void write(PacketByteBuf buf, HuajiBlenderRecipe recipe) {
		recipe.ingredient().write(buf);
		buf.writeItemStack(recipe.result());
		buf.writeFloat(recipe.experience());
		buf.writeVarInt(recipe.processTime());
	}
}
