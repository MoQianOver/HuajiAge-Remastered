package org.huajiager.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.huajiager.compat.jei.HuajiJeiPlugin;
import org.huajiager.init.loaders.BlockLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.recipe.HuajiPolyfurnaceRecipe;

/**
 * 终极熔炼炉 JEI 分类：输入 + 燃料(huaji 滑稽) + 输出三槽，含燃料条火焰动画、进度箭头动画与滑稽之星池液位动画。
 * 槽位 INPUT(18,19) / FUEL(18,41) / OUTPUT(78,19)（三槽保持原样，仅整体右移1、下移1）。 * 火焰绘于(7,8)裁自(233,5,6,31)，StartDirection.TOP + inverted=true（与混合仪 GUI 燃料条一致，从上往下减）。 * 进度箭头绘于(39,27)裁自(2,62,37,1)，StartDirection.LEFT。 * 滑稽之星池液位绘于(77,13)裁自(202,6,23,19)，StartDirection.BOTTOM。
 * 配方无直接 result，池满 2008 点产出滑稽之星终极版，输出槽统一展示 huaji_star_universe。
 */
public class HuajiPolyfurnaceRecipeCategory implements IRecipeCategory<HuajiPolyfurnaceRecipe> {

	private static final Identifier TEXTURE =
			Identifier.of("huajiager", "textures/jei/gui_huaji_poly_jei.png");

	private final IDrawable background;
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated pool;
	private final IDrawable icon;

	public HuajiPolyfurnaceRecipeCategory(IGuiHelper helper) {
		this.background = helper.createDrawable(TEXTURE, 0, 0, 109, 55);
		this.flame = helper.createAnimatedDrawable(
				helper.createDrawable(TEXTURE, 233, 5, 6, 31), 300,
				IDrawableAnimated.StartDirection.TOP, true);
		this.arrow = helper.createAnimatedDrawable(
				helper.createDrawable(TEXTURE, 2, 62, 37, 1), 200,
				IDrawableAnimated.StartDirection.LEFT, false);
		this.pool = helper.createAnimatedDrawable(
				helper.createDrawable(TEXTURE, 202, 6, 23, 19), 500,
				IDrawableAnimated.StartDirection.BOTTOM, false);
		this.icon = helper.createDrawableIngredient(
				VanillaTypes.ITEM_STACK, new ItemStack(BlockLoader.huajiPolyFurnace));
	}

	@Override
	public RecipeType<HuajiPolyfurnaceRecipe> getRecipeType() {
		return HuajiJeiPlugin.HUaji_POLYFURNACE;
	}

	@Override
	public Text getTitle() {
		return Text.translatable("block.huajiager.huaji_poly_furnace");
	}

	@Override
	public int getWidth() {
		return 109;
	}

	@Override
	public int getHeight() {
		return 55;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, HuajiPolyfurnaceRecipe recipe, IFocusGroup focuses) {
		builder.addDrawable(background, 0, 0);
		// 燃料条火焰（左上，7,8）
		builder.addDrawable(flame, 7, 8);
		// 合成进度箭头（中，39,27）
		builder.addDrawable(arrow, 39, 27);
		// 滑稽之星池液位（右，77,13）
		builder.addDrawable(pool, 77, 13);
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, HuajiPolyfurnaceRecipe recipe, IFocusGroup focuses) {
		// 输入格（左窗口，18,19）
		builder.addSlot(RecipeIngredientRole.INPUT, 18, 19).addIngredients(recipe.ingredient());
		// 输出格（右窗口，78,19）：池满 2008 点产出滑稽之星终极版
		builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 19)
				.addItemStack(new ItemStack(ItemLoader.huajiStarUniverse));
	}
}
