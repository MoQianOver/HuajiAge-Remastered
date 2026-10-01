package org.huajiager.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.huajiager.compat.jei.HuajiJeiPlugin;
import org.huajiager.init.loaders.BlockLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.recipe.HuajiBlenderRecipe;

/**
 * 搅拌机 JEI 分类：输入(左格) → 输出(右格)，含燃料条火焰动画与合成进度箭头动画。
 * 背景使用 textures/jei/gui_huaji_blader_jei.png（内容区 106x55），
 * 布置：INPUT(18,19) / OUTPUT(72,19)。 * 火焰绘于(13,18)裁自(37,156,2,18)，StartDirection.TOP + inverted=true
 * （内部反转为 BOTTOM 方向 + 递增：底端固定、黄色从上往下收缩，燃料消耗时顶部先减少）。 * 进度箭头绘于(38,18)裁自(0,156,30,17)，StartDirection.LEFT。
 */
public class HuajiBlenderRecipeCategory implements IRecipeCategory<HuajiBlenderRecipe> {

	private static final Identifier TEXTURE =
			Identifier.of("huajiager", "textures/jei/gui_huaji_blader_jei.png");

	private final IDrawable background;
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;
	private final IDrawable icon;

	public HuajiBlenderRecipeCategory(IGuiHelper helper) {
		this.background = helper.createDrawable(TEXTURE, 0, 0, 106, 55);
		this.flame = helper.createAnimatedDrawable(
				helper.createDrawable(TEXTURE, 37, 156, 2, 18), 300,
				IDrawableAnimated.StartDirection.TOP, true);
		this.arrow = helper.createAnimatedDrawable(
				helper.createDrawable(TEXTURE, 0, 156, 30, 17), 200,
				IDrawableAnimated.StartDirection.LEFT, false);
		this.icon = helper.createDrawableIngredient(
				VanillaTypes.ITEM_STACK, new ItemStack(BlockLoader.huajiBlender));
	}

	@Override
	public RecipeType<HuajiBlenderRecipe> getRecipeType() {
		return HuajiJeiPlugin.HUaji_BLENDER;
	}

	@Override
	public Text getTitle() {
		return Text.translatable("block.huajiager.huaji_blender");
	}

	@Override
	public int getWidth() {
		return 106;
	}

	@Override
	public int getHeight() {
		return 54;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, HuajiBlenderRecipe recipe, IFocusGroup focuses) {
		builder.addDrawable(background, 0, 0);
		// 燃料条火焰（左，13,18）
		builder.addDrawable(flame, 13, 18);
		// 合成进度箭头（中，38,18）
		builder.addDrawable(arrow, 38, 18);
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, HuajiBlenderRecipe recipe, IFocusGroup focuses) {
		// 输入格（左，18,19）
		builder.addSlot(RecipeIngredientRole.INPUT, 18, 19).addIngredients(recipe.ingredient());
		// 输出格（右，72,19）
		builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 19).addItemStack(recipe.result());
	}
}
