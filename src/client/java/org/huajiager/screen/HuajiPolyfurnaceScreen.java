package org.huajiager.screen;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.tileentity.TileEntityHuajiPolyfurnace;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * 滑稽终极熔炉界面， astral HuaJiPolyfurnaceScreen。
 * 背景 192x156；烹饪条 (62,33) 87x1；燃料火焰 (10,6) 16x58。 * 聚合池 (147,19) 28x19；FE 能量条 (27,4) 3x62；悬停 tooltip 显示燃料/池/能量。
 */
public class HuajiPolyfurnaceScreen extends HandledScreen<HuajiPolyfurnaceMenu> {

	private static final Identifier TEXTURE = Identifier.of("huajiager", "textures/gui/container/gui_huaji_polyfurnace.png");

	private static final int COOK_BAR_X = 62;
	private static final int COOK_BAR_Y = 33;
	private static final int COOK_BAR_U = 0;
	private static final int COOK_BAR_V = 158;
	private static final int COOK_BAR_WIDTH = 87;
	private static final int COOK_BAR_HEIGHT = 1;

	private static final int POOL_X = 147;
	private static final int POOL_Y = 19;
	private static final int POOL_U = 192;
	private static final int POOL_V = 2;
	private static final int POOL_WIDTH = 28;
	private static final int POOL_HEIGHT = 19;

	private static final int ENERGY_X = 27;
	private static final int ENERGY_Y = 4;
	private static final int ENERGY_U = 246;
	private static final int ENERGY_V = 0;
	private static final int ENERGY_WIDTH = 3;
	private static final int ENERGY_HEIGHT = 62;

	private static final int FLAME_X = 10;
	private static final int FLAME_Y = 6;
	private static final int FLAME_U = 224;
	private static final int FLAME_V = 0;
	private static final int FLAME_WIDTH = 16;
	private static final int FLAME_HEIGHT = 58;

	public HuajiPolyfurnaceScreen(HuajiPolyfurnaceMenu menu, PlayerInventory playerInventory, Text title) {
		super(menu, playerInventory, title);
		this.backgroundWidth = 192;
		this.backgroundHeight = 156;
		this.playerInventoryTitleX = 152;
		this.playerInventoryTitleY = 58;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		// 无条件按鼠标所在区域绘制悬停提示（不依赖 focusedSlot）
		List<Text> tooltipLines = new ArrayList<>();
		if (isPointWithinBounds(FLAME_X, FLAME_Y, FLAME_WIDTH, FLAME_HEIGHT, mouseX, mouseY)) {
			int energy = handler.getEnergy();
			tooltipLines.add(Text.translatable("gui.huajiager.poly.fuel"));
			tooltipLines.add(Text.literal(String.valueOf(energy > 1000 ? (energy / 1000.0) + "k" : energy)).formatted(Formatting.AQUA));
		}
		if (isPointWithinBounds(POOL_X, POOL_Y, POOL_WIDTH, POOL_HEIGHT, mouseX, mouseY)) {
			tooltipLines.add(Text.translatable("gui.huajiager.poly.pool"));
			tooltipLines.add(Text.literal(handler.getPool() + "/" + TileEntityHuajiPolyfurnace.TOTAL_POINT).formatted(Formatting.YELLOW, Formatting.BOLD));
		}
		if (isPointWithinBounds(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
			tooltipLines.add(Text.translatable("gui.huajiager.poly.energy"));
			tooltipLines.add(Text.literal(handler.getFE() + "/" + TileEntityHuajiPolyfurnace.FE_CAPACITY));
		}
		if (!tooltipLines.isEmpty()) {
			context.drawTooltip(this.textRenderer, tooltipLines, mouseX, mouseY);
		}
	}

	/**
	 * 前景绘制：标题居中 + 中下方进程百分比。
	 * 标题 x = (xSize-27-3)/2 + 27 + 3 - width/2 - 9，y=10，黑色粗体。	 * 百分比 x = (xSize-27-3)/2 + 27 + 3 - width/2，y=42，darkGray。
	 */
	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		int xSize = this.backgroundWidth;
		int centerX = (xSize - 27 - 3) / 2 + 27 + 3;

		String title = this.title.getString();
		int titleWidth = this.textRenderer.getWidth(title);
		context.drawTextWithShadow(this.textRenderer, title, centerX - titleWidth / 2 - 9, 10, 0xFF000000);

		int percent = (int) (handler.getProgress() * 100);
		Text percentText = Text.translatable("gui.huajiager.poly.cook")
				.append(" ")
				.append(Text.literal(percent + "%").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
		int percentWidth = this.textRenderer.getWidth(percentText);
		context.drawTextWithShadow(this.textRenderer, percentText, centerX - percentWidth / 2, 42, 0xFF404040);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = this.x;
		int y = this.y;
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

		double cookProgress = handler.getProgress();
		context.drawTexture(TEXTURE,
				x + COOK_BAR_X, y + COOK_BAR_Y,
				COOK_BAR_U, COOK_BAR_V,
				(int) (COOK_BAR_WIDTH * cookProgress), COOK_BAR_HEIGHT);

		double burnRemaining = handler.getFuel();
		int burnOffsetY = (int) ((1.0 - burnRemaining) * FLAME_HEIGHT);
		context.drawTexture(TEXTURE,
				x + FLAME_X, y + FLAME_Y + burnOffsetY,
				FLAME_U, FLAME_V + burnOffsetY,
				FLAME_WIDTH, FLAME_HEIGHT - burnOffsetY);

		double pool = (double) handler.getPool() / TileEntityHuajiPolyfurnace.TOTAL_POINT;
		int poolOffsetY = (int) ((1.0 - pool) * POOL_HEIGHT);
		context.drawTexture(TEXTURE,
				x + POOL_X, y + POOL_Y + poolOffsetY,
				POOL_U, POOL_V + poolOffsetY,
				POOL_WIDTH, POOL_HEIGHT - poolOffsetY);

		double energy = (double) handler.getFE() / TileEntityHuajiPolyfurnace.FE_CAPACITY;
		int energyOffsetY = (int) ((1.0 - energy) * ENERGY_HEIGHT);
		context.drawTexture(TEXTURE,
				x + ENERGY_X, y + ENERGY_Y + energyOffsetY,
				ENERGY_U, ENERGY_V + energyOffsetY,
				ENERGY_WIDTH, ENERGY_HEIGHT - energyOffsetY);
	}
}
