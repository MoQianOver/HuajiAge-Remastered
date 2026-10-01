package org.huajiager.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * 熔炉配方序列化器，JSON 字段：ingredient / experience / processTime / point。
 */
public class HuajiPolyfurnaceRecipeSerializer implements RecipeSerializer<HuajiPolyfurnaceRecipe> {

	@Override
	public HuajiPolyfurnaceRecipe read(Identifier id, JsonObject json) {
		Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
		float experience = JsonHelper.getFloat(json, "experience", 0.0F);
		int processTime = JsonHelper.getInt(json, "processTime", 100);
		int point = JsonHelper.getInt(json, "point", 1);
		return new HuajiPolyfurnaceRecipe(id, ingredient, experience, processTime, point);
	}

	@Override
	public HuajiPolyfurnaceRecipe read(Identifier id, PacketByteBuf buf) {
		Ingredient ingredient = Ingredient.fromPacket(buf);
		float experience = buf.readFloat();
		int processTime = buf.readVarInt();
		int point = buf.readVarInt();
		return new HuajiPolyfurnaceRecipe(id, ingredient, experience, processTime, point);
	}

	@Override
	public void write(PacketByteBuf buf, HuajiPolyfurnaceRecipe recipe) {
		recipe.ingredient().write(buf);
		buf.writeFloat(recipe.experience());
		buf.writeVarInt(recipe.processTime());
		buf.writeVarInt(recipe.point());
	}
}
