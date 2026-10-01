package org.huajiager.block;

import org.huajiager.init.loaders.BlockEntityLoader;
import org.huajiager.screen.HuajiBlenderMenu;
import org.huajiager.tileentity.TileEntityHuajiBlender;

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

public class HuajiBlender extends Block implements BlockEntityProvider {

	public static final BooleanProperty BURNING = BooleanProperty.of("burning");
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public HuajiBlender() {
		// 点亮（BURNING）时 15 级光照，熄灭时为 0
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
		return new TileEntityHuajiBlender(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : (w, p, s, be) -> {
			if (be instanceof TileEntityHuajiBlender blender) {
				blender.tick();
			}
		};
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof TileEntityHuajiBlender blender) {
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
						(syncId, inv, p) -> new HuajiBlenderMenu(syncId, inv, blender, blender.getPropertyDelegate()),
						state.getBlock().getName()));
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
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
