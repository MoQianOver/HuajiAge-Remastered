package org.huajiager.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.block.MapColor;


//滑稽之星压缩块（huaji_star_block
public class HuajiStarBlock extends Block {
	public HuajiStarBlock() {
		super(AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY)
				.strength(5.0F)
				.luminance(state -> 1)
				.sounds(BlockSoundGroup.GLASS));
	}
}
