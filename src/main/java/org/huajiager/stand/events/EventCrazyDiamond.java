package org.huajiager.stand.events;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.stand.EnumStandTag;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * 疯狂钻石方块移动事件， 。
 *
 * 通过 PlayerInteractEvent（RightClickBlock / LeftClickBlock）实现：
 * 替身激活（potionStand）且当前状态带 block_move 标签时，
 * - 右击方块：把方块沿点击面方向推开一格。 * - 左击方块：把方块沿点击面反方向拉回一格。 * 不能移动带数据（TileEntity/BlockEntity）的方块。
 *
 * Fabric 1.20.1 对应：UseBlockCallback（右击）/ AttackBlockCallback（左键）。
 */
public final class EventCrazyDiamond {

	private EventCrazyDiamond() {
	}

	public static void register() {
		// 右击方块：沿点击面方向推动
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			if (!canBlockMove(player)) {
				return ActionResult.PASS;
			}
			tryMoveBlock(world, hitResult.getBlockPos(), hitResult.getSide(), true);
			return ActionResult.PASS;
		});

		// 左键方块：沿点击面反方向拉动
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			if (!canBlockMove(player)) {
				return ActionResult.PASS;
			}
			tryMoveBlock(world, pos, direction, false);
			return ActionResult.PASS;
		});
	}

	/**
	 * 判定当前替身状态是否具备方块移动能力（：替身非空 + potionStand 激活 +
	 * 当前状态 hasExtraData(BLOCK_MOVE)）。
	 */
	private static boolean canBlockMove(PlayerEntity player) {
		StandBase stand = StandUtil.getType(player);
		if (stand == null || player.getStatusEffect(PotionLoader.potionStand) == null) {
			return false;
		}
		String state = StandUtil.getStandState(player);
		if (StandStates.getStandState(stand.getName(), state) instanceof StandStateBase stateBase) {
			return stateBase.hasExtraData(EnumStandTag.StateTags.BLOCK_MOVE.getName());
		}
		return false;
	}

	/**
	 * 执行方块移动：右击把方块沿点击面推出一格，左击沿反方向拉回一格。
	 * 带 BlockEntity 的方块（箱子/命令方块等数据方块）不可移动。
	 */
	private static void tryMoveBlock(World world, BlockPos pos, Direction face, boolean isRightClick) {
		if (!ConfigHuaji.Stands.allowCrazyDiamondBlock) {
			return;
		}
		BlockState blockState = world.getBlockState(pos);
		//  world.getTileEntity(pos) == null：不能移动带数据方块
		if (world.getBlockEntity(pos) != null) {
			return;
		}
		if (blockState.isAir()) {
			return;
		}
		BlockPos newPos = pos;
		if (face != null) {
			// 右击推（沿 face 方向），左击拉（沿 face 反方向）
			newPos = pos.offset(isRightClick ? face : face.getOpposite());
		}
		//  world.getBlockState(newPos).getBlock().canPlaceBlockAt(world, newPos)
		if (world.canPlace(blockState, newPos, ShapeContext.absent())) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			world.setBlockState(newPos, blockState);
			world.updateNeighbors(pos, Blocks.AIR);
			//  world.playEvent(2001, pos, Block.getStateId(blockState))：破坏音效 + 粒子
			world.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
		}
	}
}
