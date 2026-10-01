package org.huajiager.tileentity;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.BlockEntityLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.recipe.HuajiPolyfurnaceRecipe;
import org.huajiager.recipe.RecipeLoader;
import org.huajiager.util.HuajiUtils;
import org.huajiager.util.NBTHelper;

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
 * 滑稽终极熔炉 TileEntity（Fabric 完整版）， astral HuaJiPolyfurnaceBlockEntity。
 * 3 槽 Inventory（0 输入 / 1 燃料 / 2 输出）+ serverTick 状态机：
 * 燃料 tag（huaji_polyfurnace/time_*）按 FE 能量条（37000000 RF）烧炼，聚合池
 * itemPool 满 {@link #TOTAL_POINT} 后产出滑稽星（huaji_star_universe）。 * BURNING 由 energy > 0 驱动。
 */
public class TileEntityHuajiPolyfurnace extends BlockEntity implements Inventory {

	public static final int SLOT_INPUT = 0;
	public static final int SLOT_FUEL = 1;
	public static final int SLOT_RESULT = 2;
	public static final int NUM_SLOTS = 3;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_PROCESSING_PROGRESS = 1;
	public static final int DATA_PROCESSING_TOTAL_TIME = 2;
	public static final int DATA_POLYFURNACE_POOL = 3;
	public static final int DATA_FE_ENERGY = 4;
	public static final int NUM_DATA_VALUES = 5;

	/** 聚合池满阈值。 */
	public static final int TOTAL_POINT = ConfigHuaji.Huaji.point_star;
	/** BURN 能量上限（astral MAX_ENERGY=5000）。 */
	public static final int MAX_ENERGY = 5000;
	/** RF 能量容量（astral FE_CAPACITY）。 */
	public static final int FE_CAPACITY = 37000000;
	/** 每 tick 从 RF 转换的 BURN 能量（astral FE_CONVERT=100）。 */
	public static final int FE_CONVERT = 100;
	/** 燃料 tag 前缀，如 huaji_polyfurnace/time_10 = 10 BURN 能量。 */
	public static final String HUAJI_POLYFURNACE_TIME_PREFIX = "huaji_polyfurnace/time_";
	/** 燃料解析起始下标（"huaji_polyfurnace/time_" 长度）。 */
	public static final int HUAJI_POLYFURNACE_TIME_INDEX = 23;

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(NUM_SLOTS, ItemStack.EMPTY);

	public int energy;
	public int processingProgress;
	public int processingTotalTime;
	public int itemPool;
	public int feEnergy;

	private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_LIT_TIME -> energy;
				case DATA_PROCESSING_PROGRESS -> processingProgress;
				case DATA_PROCESSING_TOTAL_TIME -> processingTotalTime;
				case DATA_POLYFURNACE_POOL -> itemPool;
				case DATA_FE_ENERGY -> feEnergy;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_LIT_TIME -> energy = value;
				case DATA_PROCESSING_PROGRESS -> processingProgress = value;
				case DATA_PROCESSING_TOTAL_TIME -> processingTotalTime = value;
				case DATA_POLYFURNACE_POOL -> itemPool = value;
				case DATA_FE_ENERGY -> feEnergy = value;
			}
		}

		@Override
		public int size() {
			return NUM_DATA_VALUES;
		}
	};

	public TileEntityHuajiPolyfurnace(BlockPos pos, BlockState state) {
		super(BlockEntityLoader.HUAJI_POLYFURNACE, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, TileEntityHuajiPolyfurnace be) {
		boolean dirty = false;
		boolean wasBurning = be.energy > 0;
		HuajiPolyfurnaceRecipe recipe = world.getRecipeManager().getFirstMatch(RecipeLoader.HUAJI_POLYFURNACE_RECIPE_TYPE, be, world).orElse(null);

		boolean canBurn = be.canBurn(recipe);

		if (be.feEnergy < FE_CAPACITY && be.energy > 0) {
			be.feEnergy = Math.min(FE_CAPACITY, be.feEnergy + FE_CONVERT);
			dirty = true;
		}

		if (canBurn) {
			// 新放置/未持久化的 TileEntity 首次 tick 时 processingTotalTime 为 0，
			// 若不先初始化会导致 processingProgress(1) >= 0 立即误烧一次（聚合池异常跳变）
			if (be.processingTotalTime <= 0) {
				be.processingTotalTime = be.getProcessingTotalTime(recipe);
			}
			// 行为：燃料槽整组一次性吞掉，能量按 数量 * 单燃料值 累加（封顶 MAX_ENERGY）。
			// 只要燃料槽有滑稽燃料即整组吞（放入即吞），一次性增加能量，杜绝逐 tick 逐个消耗。
			ItemStack fuel = be.inventory.get(SLOT_FUEL);
			if (!fuel.isEmpty()) {
				int fuelValue = HuajiUtils.getTagFuel(fuel, HUAJI_POLYFURNACE_TIME_PREFIX, HUAJI_POLYFURNACE_TIME_INDEX);
				if (fuelValue > 0) {
					int count = fuel.getCount();
					be.energy = Math.min(MAX_ENERGY, be.energy + fuelValue * count);
					fuel.decrement(count);
					if (fuel.isEmpty()) {
						be.inventory.set(SLOT_FUEL, ItemStack.EMPTY);
					}
					dirty = true;
				}
			}
			if (be.energy > 0) {
				// 行为：黄金精神仅在烧炼（canBurn && energy>0）时逐 tick 消耗，
				// 未烧炼时不得扣减
				be.energy--;
				be.processingProgress++;
				dirty = true;
				if (be.processingProgress >= be.processingTotalTime) {
					be.processingTotalTime = be.getProcessingTotalTime(recipe);
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

		// 工作状态变化时同步 BURNING 到 BlockState，驱动 working 模型切换
		if (wasBurning != (be.energy > 0)) {
			world.setBlockState(pos, state.with(org.huajiager.block.HuajiPolyfurnace.BURNING, be.energy > 0), Block.NOTIFY_ALL);
			dirty = true;
		}

		if (dirty) {
			be.markDirty();
		}
	}

	private int getProcessingTotalTime(HuajiPolyfurnaceRecipe recipe) {
		return recipe != null ? recipe.processTime() : 100;
	}

	private boolean canBurn(HuajiPolyfurnaceRecipe recipe) {
		ItemStack input = inventory.get(SLOT_INPUT);
		if (input.isEmpty() || recipe == null) {
			return false;
		}
		ItemStack output = inventory.get(SLOT_RESULT);
		if (output.isEmpty()) {
			return true;
		}
		if (!output.isOf(ItemLoader.huajiStarUniverse)) {
			return false;
		}
		return output.getCount() < output.getMaxCount();
	}

	private void burn(HuajiPolyfurnaceRecipe recipe) {
		ItemStack input = inventory.get(SLOT_INPUT);
		//  getPoolfuel = 配方固定 point + NBT poly（多重叠加态滑稽之星按叠加值计入聚合池）
		int gained = recipe.point() + getPolyPoolBonus(input);
		input.decrement(1);
		if (input.isEmpty()) {
			inventory.set(SLOT_INPUT, ItemStack.EMPTY);
		}
		itemPool += gained;
		while (itemPool >= TOTAL_POINT) {
			itemPool -= TOTAL_POINT;
			ItemStack output = inventory.get(SLOT_RESULT);
			if (output.isEmpty()) {
				inventory.set(SLOT_RESULT, new ItemStack(ItemLoader.huajiStarUniverse));
			} else if (output.isOf(ItemLoader.huajiStarUniverse) && output.getCount() < output.getMaxCount()) {
				output.increment(1);
			}
		}
	}

	/**
	 *  HuajiPolyRecipeList.getPool：多重叠加态滑稽之星烧炼时，聚合值附加其 NBT poly 叠加值（>0 才计）。
	 */
	private static int getPolyPoolBonus(ItemStack stack) {
		if (stack.isEmpty() || !stack.isOf(ItemLoader.huajiStarPoly)) {
			return 0;
		}
		return Math.max(NBTHelper.getTagCompoundSafe(stack).getInt("poly"), 0);
	}

	/** 实例 tick 包装，供 Block.getTicker 直接调用。 */
	public void tick() {
		if (world != null && !world.isClient) {
			serverTick(world, pos, world.getBlockState(pos), this);
		}
	}

	/** 供外部判定的 burning 状态（BURNING blockstate 同步用）。 */
	public boolean isBurning() {
		return energy > 0;
	}

	/** 兼容原 onBlockBreakStart 显示：当前 BURN 能量。 */
	public int getEnergyCurrent() {
		return energy;
	}

	public int getEnergyMax() {
		return MAX_ENERGY;
	}

	public int getPool() {
		return itemPool;
	}

	public void setPool(int pool) {
		this.itemPool = pool;
	}

	/** 兼容原 getField(3)：BURNING 映射。 */
	public int getField(int id) {
		if (id == 3) {
			return isBurning() ? 1 : 0;
		}
		return 0;
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
			return HuajiUtils.isTagFuel(stack, HUAJI_POLYFURNACE_TIME_PREFIX, HUAJI_POLYFURNACE_TIME_INDEX);
		}
		return true;
	}

	// ---------- NBT ----------

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putInt("Energy", energy);
		nbt.putInt("ProcessingProgress", processingProgress);
		nbt.putInt("ProcessingTotalTime", processingTotalTime);
		nbt.putInt("ItemPool", itemPool);
		nbt.putInt("FeEnergy", feEnergy);
		net.minecraft.inventory.Inventories.writeNbt(nbt, inventory);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		energy = nbt.getInt("Energy");
		processingProgress = nbt.getInt("ProcessingProgress");
		processingTotalTime = nbt.getInt("ProcessingTotalTime");
		itemPool = nbt.getInt("ItemPool");
		feEnergy = nbt.getInt("FeEnergy");
		net.minecraft.inventory.Inventories.readNbt(nbt, inventory);
	}
}
