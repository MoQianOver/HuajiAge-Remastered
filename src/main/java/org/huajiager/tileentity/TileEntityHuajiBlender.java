package org.huajiager.tileentity;

import org.huajiager.init.loaders.BlockEntityLoader;
import org.huajiager.recipe.HuajiBlenderRecipe;
import org.huajiager.recipe.RecipeLoader;
import org.huajiager.util.HuajiUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 滑稽搅拌机 TileEntity（Fabric 完整版）， astral HuaJiBlenderBlockEntity。
 * 3 槽 Inventory（0 输入 / 1 燃料 / 2 输出）+ serverTick 烧炼状态机：
 * 燃料 tag（huaji_blender/time_*）点燃 → 进度累计 → 产物输出；BURNING 状态同步方块。
 */
public class TileEntityHuajiBlender extends BlockEntity implements Inventory {

	public static final int SLOT_INPUT = 0;
	public static final int SLOT_FUEL = 1;
	public static final int SLOT_RESULT = 2;
	public static final int NUM_SLOTS = 3;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_COOKING_PROGRESS = 2;
	public static final int DATA_COOKING_TOTAL_TIME = 3;
	public static final int NUM_DATA_VALUES = 4;

	/** 无燃料数据时的兜底总时长。 */
	public static final int COOK_TIME_FOR_COMPLETION = 100;
	/** 燃料 tag 前缀，如 huaji_blender/time_100 = 100 tick。 */
	public static final String HUAJI_BLENDER_TIME_PREFIX = "huaji_blender/time_";
	/** 燃料解析起始下标（"huaji_blender/time_" 长度）。 */
	public static final int HUAJI_BLENDER_TIME_INDEX = 19;

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(NUM_SLOTS, ItemStack.EMPTY);

	public int litTime;
	public int litDuration;
	public int processingProgress;
	public int processingTotalTime;

	private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_LIT_TIME -> litTime;
				case DATA_LIT_DURATION -> litDuration;
				case DATA_COOKING_PROGRESS -> processingProgress;
				case DATA_COOKING_TOTAL_TIME -> processingTotalTime;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_LIT_TIME -> litTime = value;
				case DATA_LIT_DURATION -> litDuration = value;
				case DATA_COOKING_PROGRESS -> processingProgress = value;
				case DATA_COOKING_TOTAL_TIME -> processingTotalTime = value;
			}
		}

		@Override
		public int size() {
			return NUM_DATA_VALUES;
		}
	};

	public TileEntityHuajiBlender(BlockPos pos, BlockState state) {
		super(BlockEntityLoader.HUAJI_BLENDER, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, TileEntityHuajiBlender be) {
		boolean dirty = false;
		boolean wasBurning = be.litTime > 0;
		HuajiBlenderRecipe recipe = world.getRecipeManager().getFirstMatch(RecipeLoader.HUAJI_BLENDER_RECIPE_TYPE, be, world).orElse(null);

		if (be.litTime > 0) {
			be.litTime--;
			dirty = true;
		}

		if (!be.inventory.get(SLOT_INPUT).isEmpty() && recipe != null && be.canBurn(recipe)) {
			if (be.litTime <= 0) {
				ItemStack fuel = be.inventory.get(SLOT_FUEL);
				int fuelValue = HuajiUtils.getTagFuel(fuel, HUAJI_BLENDER_TIME_PREFIX, HUAJI_BLENDER_TIME_INDEX);
				if (fuelValue > 0) {
					be.litTime = fuelValue;
					be.litDuration = fuelValue;
					fuel.decrement(1);
					if (fuel.isEmpty()) {
						be.inventory.set(SLOT_FUEL, ItemStack.EMPTY);
					}
					dirty = true;
				}
			}
			if (be.litTime > 0) {
				// 新放置/未持久化的 TileEntity 首次 tick 时 processingTotalTime 为 0，
				// 若不先初始化会导致 processingProgress(1) >= 0 立即误烧一次（第一次混合瞬间完成）
				if (be.processingTotalTime <= 0) {
					be.processingTotalTime = be.getCookTotalTime(recipe);
				}
				be.processingProgress++;
				dirty = true;
				if (be.processingProgress >= be.processingTotalTime) {
					be.processingTotalTime = be.getCookTotalTime(recipe);
					be.processingProgress = 0;
					be.burn(recipe);
					dirty = true;
				}
			} else {
				be.processingProgress = 0;
			}
		} else {
			be.processingProgress = 0;
		}

		if (wasBurning != (be.litTime > 0)) {
			// 工作状态变化时同步 BURNING 到 BlockState，驱动 working 模型切换
			world.setBlockState(pos, state.with(org.huajiager.block.HuajiBlender.BURNING, be.litTime > 0), Block.NOTIFY_ALL);
			dirty = true;
		}

		if (dirty) {
			be.markDirty();
		}
	}

	private int getCookTotalTime(HuajiBlenderRecipe recipe) {
		return recipe != null ? recipe.processTime() : COOK_TIME_FOR_COMPLETION;
	}

	private boolean canBurn(HuajiBlenderRecipe recipe) {
		ItemStack input = inventory.get(SLOT_INPUT);
		if (input.isEmpty() || recipe == null) {
			return false;
		}
		ItemStack output = inventory.get(SLOT_RESULT);
		if (output.isEmpty()) {
			return true;
		}
		if (!ItemStack.areEqual(output, recipe.getOutput(world.getRegistryManager()))) {
			return false;
		}
		int resultCount = output.getCount() + recipe.getOutput(world.getRegistryManager()).getCount();
		return resultCount <= getMaxCountPerStack() && resultCount <= output.getMaxCount();
	}

	private void burn(HuajiBlenderRecipe recipe) {
		ItemStack input = inventory.get(SLOT_INPUT);
		ItemStack output = inventory.get(SLOT_RESULT);
		ItemStack result = recipe.craft(this, world.getRegistryManager());
		if (output.isEmpty()) {
			inventory.set(SLOT_RESULT, result.copy());
		} else if (ItemStack.areEqual(output, result)) {
			output.increment(result.getCount());
		}
		input.decrement(1);
		if (input.isEmpty()) {
			inventory.set(SLOT_INPUT, ItemStack.EMPTY);
		}
	}

	/** 实例 tick 包装，供 Block.getTicker 直接调用。 */
	public void tick() {
		if (world != null && !world.isClient) {
			serverTick(world, pos, world.getBlockState(pos), this);
		}
	}

	/** 供外部判定的 burning 状态（BURNING blockstate 同步用）。 */
	public boolean isBurning() {
		return litTime > 0;
	}

	public PropertyDelegate getPropertyDelegate() {
		return propertyDelegate;
	}

	// ---------- Inventory ----------

	@Override
	public int size() {
		return NUM_SLOTS;
	}

	@Override
	public boolean isEmpty() {
		return inventory.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack stack = inventory.get(slot);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (amount >= stack.getCount()) {
			inventory.set(slot, ItemStack.EMPTY);
			return stack;
		}
		ItemStack split = stack.split(amount);
		markDirty();
		return split;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack stack = inventory.get(slot);
		inventory.set(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}
		markDirty();
	}

	@Override
	public void clear() {
		inventory.clear();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		if (world == null || world.getBlockEntity(pos) != this) {
			return false;
		}
		return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (slot == SLOT_FUEL) {
			return HuajiUtils.isTagFuel(stack, HUAJI_BLENDER_TIME_PREFIX, HUAJI_BLENDER_TIME_INDEX);
		}
		return true;
	}

	// ---------- NBT ----------

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putInt("LitTime", litTime);
		nbt.putInt("LitDuration", litDuration);
		nbt.putInt("ProcessingProgress", processingProgress);
		nbt.putInt("ProcessingTotalTime", processingTotalTime);
		net.minecraft.inventory.Inventories.writeNbt(nbt, inventory);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		litTime = nbt.getInt("LitTime");
		litDuration = nbt.getInt("LitDuration");
		processingProgress = nbt.getInt("ProcessingProgress");
		processingTotalTime = nbt.getInt("ProcessingTotalTime");
		net.minecraft.inventory.Inventories.readNbt(nbt, inventory);
	}
}
