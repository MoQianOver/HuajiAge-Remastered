package org.huajiager.block;

import org.huajiager.init.loaders.BlockEntityLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.screen.HuajiPolyfurnaceMenu;
import org.huajiager.tileentity.TileEntityHuajiPolyfurnace;
import org.huajiager.util.NBTHelper;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HuajiPolyfurnace extends Block implements BlockEntityProvider {

	public static final BooleanProperty BURNING = BooleanProperty.of("burning");
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public HuajiPolyfurnace() {
		//  getLightValue 读 getField(3)=cookTime>0 映射 BURNING，BURNING 时 15 级光照
		super(AbstractBlock.Settings.create().strength(3.5F, 6.0F)
				.luminance(state -> state.get(BURNING) ? 15 : 0));
		this.setDefaultState(this.stateManager.getDefaultState().with(BURNING, false).with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BURNING, FACING);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityHuajiPolyfurnace(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : (w, p, s, be) -> {
			if (be instanceof TileEntityHuajiPolyfurnace poly) {
				poly.tick();
			}
		};
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof TileEntityHuajiPolyfurnace poly) {
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
						(syncId, inv, p) -> new HuajiPolyfurnaceMenu(syncId, inv, poly, poly.getPropertyDelegate()),
						state.getBlock().getName()));
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundLoader.NOISE_FURNACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof Inventory inventory) {
				ItemScatterer.spawn(world, pos, inventory);
			}
			// 行为：聚合值 > 0 时挖掉炉子，掉落 1 个多重叠加态滑稽之星，
			// 其 poly 叠加值 = 炉子当前聚合值（ItemHuajiStarPoly tooltip 显示该值）
			if (!world.isClient && be instanceof TileEntityHuajiPolyfurnace poly && poly.getPool() > 0) {
				ItemStack stack = new ItemStack(ItemLoader.huajiStarPoly);
				NBTHelper.getTagCompoundSafe(stack).putInt("poly", poly.getPool());
				ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
