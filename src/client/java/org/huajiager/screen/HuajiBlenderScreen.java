package org.huajiager.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 滑稽搅拌机界面， astral HuaJiBlenderScreen。
 * 背景 176x156；烹饪条 (76,29) 30x17；燃料火焰 (12,29) 2x18（贴图第二行 y=156）。
 */
public class HuajiBlenderScreen extends HandledScreen<HuajiBlenderMenu> {

	private static final Identifier TEXTURE = Identifier.of("huajiager", "textures/gui/container/gui_huaji_blender.png");

	private static final int COOK_BAR_X = 76;
	private static final int COOK_BAR_Y = 29;
	private static final int COOK_BAR_U = 0;
	private static final int COOK_BAR_V = 156;
	private static final int COOK_BAR_WIDTH = 30;
	private static final int COOK_BAR_HEIGHT = 17;

	private static final int FLAME_X = 12;
	private static final int FLAME_Y = 29;
	private static final int FLAME_U = 37;
	private static final int FLAME_V = 156;
	private static final int FLAME_WIDTH = 2;
	private static final int FLAME_HEIGHT = 18;

	public HuajiBlenderScreen(HuajiBlenderMenu menu, PlayerInventory playerInventory, Text title) {
		super(menu, playerInventory, title);
		this.backgroundWidth = 176;
		this.backgroundHeight = 156;
		this.playerInventoryTitleX = 5;
		this.playerInventoryTitleY = 62;
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
		int yOffset = (int) ((1.0 - burnRemaining) * FLAME_HEIGHT);
		context.drawTexture(TEXTURE,
				x + FLAME_X, y + FLAME_Y + yOffset,
				FLAME_U, FLAME_V + yOffset,
				FLAME_WIDTH, FLAME_HEIGHT - yOffset);
	}
}
