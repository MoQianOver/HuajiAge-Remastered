package org.huajiager.screen;

import org.huajiager.recipe.HuajiPolyfurnaceRecipe;
import org.huajiager.recipe.RecipeLoader;
import org.huajiager.tileentity.TileEntityHuajiPolyfurnace;
import org.huajiager.util.HuajiUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

/**
 * 滑稽终极熔炉容器， astral HuaJiPolyfurnaceMenu。
 * 槽位：输入 44,25 / 燃料 44,48 / 输出 151,25；玩家背包 16,71 / 热栏 16,129。
 * PropertyDelegate(5)：energy / progress / totalTime / pool / feEnergy。
 */
public class HuajiPolyfurnaceMenu extends ScreenHandler {

	public static final int HOTBAR_SLOT_COUNT = 9;
	public static final int PLAYER_INVENTORY_ROW_COUNT = 3;
	public static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
	public static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;
	public static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

	public static final int INPUT_SLOT_INDEX = 0;
	public static final int FUEL_SLOT_INDEX = 1;
	public static final int OUTPUT_SLOT_INDEX = 2;
	public static final int FURNACE_SLOTS_COUNT = 3;

	private final Inventory container;
	private final PropertyDelegate propertyDelegate;
	protected final World world;

	/** 客户端/网络构造：无 TileEntity 数据，槽位内容由服务端同步。 */
	public HuajiPolyfurnaceMenu(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(TileEntityHuajiPolyfurnace.NUM_SLOTS), new ArrayPropertyDelegate(TileEntityHuajiPolyfurnace.NUM_DATA_VALUES));
	}

	public HuajiPolyfurnaceMenu(int syncId, PlayerInventory playerInventory, Inventory container, PropertyDelegate propertyDelegate) {
		super(MenuLoader.HUAJI_POLYFURNACE, syncId);
		this.container = container;
		this.propertyDelegate = propertyDelegate;
		this.world = playerInventory.player.getWorld();

		this.addSlot(new Slot(container, INPUT_SLOT_INDEX, 44, 25));
		this.addSlot(new HuajiFuelSlot(container, FUEL_SLOT_INDEX, 44, 48, TileEntityHuajiPolyfurnace.HUAJI_POLYFURNACE_TIME_PREFIX, TileEntityHuajiPolyfurnace.HUAJI_POLYFURNACE_TIME_INDEX));
		this.addSlot(new HuajiResultSlot(container, OUTPUT_SLOT_INDEX, 151, 25));

		for (int i = 0; i < PLAYER_INVENTORY_ROW_COUNT; i++) {
			for (int j = 0; j < PLAYER_INVENTORY_COLUMN_COUNT; j++) {
				this.addSlot(new Slot(playerInventory, HOTBAR_SLOT_COUNT + i * PLAYER_INVENTORY_COLUMN_COUNT + j, 16 + j * 18, 71 + i * 18));
			}
		}
		for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
			this.addSlot(new Slot(playerInventory, x, 16 + x * 18, 129));
		}

		this.addProperties(propertyDelegate);
	}

	public double getProgress() {
		int i = propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_PROCESSING_PROGRESS);
		int j = propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_PROCESSING_TOTAL_TIME);
		return j != 0 && i != 0 ? (double) i / j : 0;
	}

	public int getEnergy() {
		return propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_LIT_TIME);
	}

	public int getPool() {
		return propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_POLYFURNACE_POOL);
	}

	public int getFE() {
		return propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_FE_ENERGY);
	}

	public double getFuel() {
		return (double) propertyDelegate.get(TileEntityHuajiPolyfurnace.DATA_LIT_TIME) / TileEntityHuajiPolyfurnace.MAX_ENERGY;
	}

	protected boolean isFuel(ItemStack stack) {
		return HuajiUtils.isTagFuel(stack, TileEntityHuajiPolyfurnace.HUAJI_POLYFURNACE_TIME_PREFIX, TileEntityHuajiPolyfurnace.HUAJI_POLYFURNACE_TIME_INDEX);
	}

	protected boolean canProcess(ItemStack stack) {
		return world.getRecipeManager().getFirstMatch(RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE, new SimpleInventory(stack), world).isPresent();
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return container.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack itemStack1 = slot.getStack();
			itemStack = itemStack1.copy();
			if (index == OUTPUT_SLOT_INDEX) {
				if (!this.insertItem(itemStack1, FURNACE_SLOTS_COUNT, FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index != INPUT_SLOT_INDEX && index != FUEL_SLOT_INDEX) {
				if (this.canProcess(itemStack1)) {
					if (!this.insertItem(itemStack1, INPUT_SLOT_INDEX, FUEL_SLOT_INDEX, false)) {
						return ItemStack.EMPTY;
					}
				} else if (this.isFuel(itemStack1)) {
					if (!this.insertItem(itemStack1, FUEL_SLOT_INDEX, OUTPUT_SLOT_INDEX, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= FURNACE_SLOTS_COUNT && index < FURNACE_SLOTS_COUNT + PLAYER_INVENTORY_SLOT_COUNT - HOTBAR_SLOT_COUNT) {
					if (!this.insertItem(itemStack1, FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT - HOTBAR_SLOT_COUNT, FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT - HOTBAR_SLOT_COUNT && index < FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT
						&& !this.insertItem(itemStack1, FURNACE_SLOTS_COUNT, FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT - HOTBAR_SLOT_COUNT, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(itemStack1, FURNACE_SLOTS_COUNT, FURNACE_SLOTS_COUNT + VANILLA_SLOT_COUNT, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack1.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}

			if (itemStack1.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTakeItem(player, itemStack1);
		}
		return itemStack;
	}
}
