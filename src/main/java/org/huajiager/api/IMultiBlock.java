package org.huajiager.api;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * 多方块结构接口
 */
public interface IMultiBlock {
	boolean blockIsSuitable(BlockState blockState);

	boolean facingIsSuitable(Direction facing);

	BlockPos getCenterPos(Direction facing);

	StructureTemplate getTemplate(World worldIn, Direction facing);

	boolean isMatch(World worldIn, BlockPos posStart, Direction facing, StructureTemplate template);

	void build(World worldIn, BlockPos posStart, Direction facing, StructureTemplate template);
}
