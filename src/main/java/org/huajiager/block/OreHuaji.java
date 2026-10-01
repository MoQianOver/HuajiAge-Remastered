package org.huajiager.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

/** 滑稽矿石（ore_huaji）
 */


public class OreHuaji extends ExperienceDroppingBlock {

	public OreHuaji() {
		super(AbstractBlock.Settings.create()
				.mapColor(MapColor.STONE_GRAY)
				.requiresTool()
				.strength(15.0F)
				.luminance(state -> 1)
				.sounds(BlockSoundGroup.STONE),
				UniformIntProvider.create(3, 7));
	}

	@Override
	protected void dropExperienceWhenMined(ServerWorld world, BlockPos pos, ItemStack tool, IntProvider experience) {
		int fortune = EnchantmentHelper.getLevel(Enchantments.FORTUNE, tool);
		if (fortune > 0) {
			this.dropExperience(world, pos, world.random.nextBetween(3, 7) * fortune);
		}
	}
}
