package org.huajiager.item;

import org.huajiager.api.HuajiAgeAPI;
import org.huajiager.api.IMultiBlock;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

/**
 * 滑稽。
 * 主手右键适合的多方块结构方块以搭建多方块结构（走 Fabric 已的 HuajiAgeAPI/IMultiBlock）。
 */
public class ItemHuaji extends Item {

	public ItemHuaji() {
		super(new Item.Settings().maxCount(64));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		List<IMultiBlock> multiBlockList = HuajiAgeAPI.getMultiBlockList();
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState blockState = world.getBlockState(pos);
		Direction facing = context.getSide();
		Hand hand = context.getHand();
		for (IMultiBlock multiBlock : multiBlockList) {
			boolean baseConditionIsOkay = hand == Hand.MAIN_HAND;
			boolean multiBlockIsOkay = multiBlock.blockIsSuitable(blockState) && multiBlock.facingIsSuitable(facing);
			if (!baseConditionIsOkay || !multiBlockIsOkay) {
				continue;
			}
			BlockPos posStart = pos.add(multiBlock.getCenterPos(facing));
			StructureTemplate template = multiBlock.getTemplate(world, facing);
			if (!world.isClient() && multiBlock.isMatch(world, posStart, facing, template)) {
				multiBlock.build(world, posStart, facing, template);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
}
