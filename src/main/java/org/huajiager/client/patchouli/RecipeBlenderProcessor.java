package org.huajiager.client.patchouli;

import org.huajiager.recipe.HuajiBlenderRecipe;
import org.huajiager.recipe.RecipeLoader;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

/**
 * 滑稽搅拌机配方处理器。
 * 查询 HuajiRecipeList 硬编码熔炼表；改为查询 RecipeManager 中 huajiager:huaji_blender 类型数据包配方。
 */
public class RecipeBlenderProcessor implements IComponentProcessor {

	private ItemStack inItem = ItemStack.EMPTY;
	private ItemStack outItem = ItemStack.EMPTY;

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
		if (level == null || level.getRecipeManager() == null) {
			return;
		}
		for (HuajiBlenderRecipe recipe : level.getRecipeManager().listAllOfType(RecipeLoader.HUAJI_BLENDER_RECIPE_TYPE)) {
			if (recipe.ingredient().test(inItem)) {
				outItem = recipe.result();
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
			case "iname":
				return IVariable.wrap("$(bold)" + outItem.getName().getString());
			default:
				return null;
		}
	}

}
