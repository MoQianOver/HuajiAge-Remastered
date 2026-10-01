package org.huajiager.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class HuajiStarBlockSky extends Block {
	public HuajiStarBlockSky() {
		super(AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY)
				.strength(15.0F)
				.luminance(state -> 15)
				.sounds(BlockSoundGroup.GLASS));
	}
}
