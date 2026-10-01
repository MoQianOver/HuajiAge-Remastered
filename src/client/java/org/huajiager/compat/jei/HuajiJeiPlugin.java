package org.huajiager.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import org.huajiager.compat.jei.category.HuajiBlenderRecipeCategory;
import org.huajiager.compat.jei.category.HuajiPolyfurnaceRecipeCategory;
import org.huajiager.init.loaders.BlockLoader;
import org.huajiager.recipe.HuajiBlenderRecipe;
import org.huajiager.recipe.HuajiPolyfurnaceRecipe;
import org.huajiager.recipe.RecipeLoader;

import java.util.List;

/**
 * JEI 联动入口：将搅拌机 / 终极熔炼炉的自定义配方暴露到 JEI 中，供玩家查看。
 */
@JeiPlugin
public class HuajiJeiPlugin implements IModPlugin {

	public static final RecipeType<HuajiBlenderRecipe> HUaji_BLENDER =
			RecipeType.create("huajiager", "huaji_blender", HuajiBlenderRecipe.class);
	public static final RecipeType<HuajiPolyfurnaceRecipe> HUaji_POLYFURNACE =
			RecipeType.create("huajiager", "huaji_polyfurnace", HuajiPolyfurnaceRecipe.class);

	private IJeiRuntime jeiRuntime;
	private boolean recipesRegistered = false;
	private boolean joined = false;
	private int waitTicks = 0;

	public HuajiJeiPlugin() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			joined = true;
			waitTicks = 0;
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			joined = false;
			recipesRegistered = false;
			waitTicks = 0;
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::tryRegisterRuntimeRecipes);
	}

	@Override
	public Identifier getPluginUid() {
		return Identifier.of("huajiager", "jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(
				new HuajiBlenderRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
				new HuajiPolyfurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		// JEI 不会自动同步自定义 RecipeType，必须手动从 RecipeManager 拉取配方并注册。
		// JEI 初始化时 world 可能尚未就绪，但网络连接后 recipeManager 同样可用，二者取其一。
		MinecraftClient client = MinecraftClient.getInstance();
		RecipeManager recipeManager = null;
		if (client.world != null) {
			recipeManager = client.world.getRecipeManager();
		} else if (client.getNetworkHandler() != null) {
			recipeManager = client.getNetworkHandler().getRecipeManager();
		}
		if (recipeManager == null) {
			return;
		}

		List<HuajiBlenderRecipe> blenderRecipes =
				recipeManager.listAllOfType(RecipeLoader.HUAJI_BLENDER_RECIPE_TYPE);
		registration.addRecipes(HUaji_BLENDER, blenderRecipes);

		List<HuajiPolyfurnaceRecipe> polyRecipes =
				recipeManager.listAllOfType(RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE);
		registration.addRecipes(HUaji_POLYFURNACE, polyRecipes);

		recipesRegistered = true;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		this.jeiRuntime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		this.jeiRuntime = null;
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(BlockLoader.huajiBlender), HUaji_BLENDER);
		registration.addRecipeCatalyst(new ItemStack(BlockLoader.huajiPolyFurnace), HUaji_POLYFURNACE);
	}

	private void tryRegisterRuntimeRecipes(MinecraftClient client) {
		if (!joined || recipesRegistered || jeiRuntime == null || client.world == null) return;
		RecipeManager recipeManager = client.world.getRecipeManager();
		if (recipeManager == null) return;
		List<HuajiBlenderRecipe> blenderRecipes =
				recipeManager.listAllOfType(RecipeLoader.HUAJI_BLENDER_RECIPE_TYPE);
		List<HuajiPolyfurnaceRecipe> polyRecipes =
				recipeManager.listAllOfType(RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE);
		if (blenderRecipes.isEmpty() && polyRecipes.isEmpty()) return;
		jeiRuntime.getRecipeManager().addRecipes(HUaji_BLENDER, blenderRecipes);
		jeiRuntime.getRecipeManager().addRecipes(HUaji_POLYFURNACE, polyRecipes);
		recipesRegistered = true;
	}
}
