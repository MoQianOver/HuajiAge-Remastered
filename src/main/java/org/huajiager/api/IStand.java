package org.huajiager.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/**
 * 替身(Stand)接口
 */
public interface IStand {
	void doStandPower(LivingEntity user);

	void doStandCapability(LivingEntity user);

	void doStandCapabilityClient(World world, LivingEntity user);
}
