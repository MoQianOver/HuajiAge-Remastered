package org.huajiager.client.patchouli;

import org.huajiager.recipe.HuajiPolyfurnaceRecipe;
import org.huajiager.recipe.RecipeLoader;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

/**
 * 滑稽终极熔炉（Polyfurnace）配方处理器。
 * 聚合值从配方 point 字段读取。
 */
public class RecipePolyProcessor implements IComponentProcessor {

	private ItemStack inItem = ItemStack.EMPTY;
	private ItemStack outItem = ItemStack.EMPTY;
	private int point = 0;

	@Override
	public void setup(World level, IVariableProvider variables) {
		String material = variables.get("recipe").asString();
		if (material == null || material.isEmpty()) {
			return;
		}
		Identifier id = Identifier.tryParse(material);
		if (id == null) {
			return;
		}
		inItem = new ItemStack(Registries.ITEM.get(id));
		outItem = new ItemStack(Registries.ITEM.get(Identifier.of("huajiager", "huaji_star_universe")));
		if (level == null || level.getRecipeManager() == null) {
			return;
		}
		for (HuajiPolyfurnaceRecipe recipe : level.getRecipeManager().listAllOfType(RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE)) {
			if (recipe.ingredient().test(inItem)) {
				point = recipe.point();
				break;
			}
		}
	}

	@Override
	public IVariable process(World level, String key) {
		switch (key) {
			case "item_in":
				return IVariable.from(outItem.isEmpty() ? ItemStack.EMPTY : inItem);
			case "item_out":
				return IVariable.from(outItem.isEmpty() ? ItemStack.EMPTY : outItem);
			case "icount":
				return IVariable.wrap(outItem.getCount());
			case "ipool":
				return IVariable.wrap(point);
			case "iname":
				return IVariable.wrap("$(bold)" + inItem.getName().getString());
			default:
				return null;
		}
	}

}
