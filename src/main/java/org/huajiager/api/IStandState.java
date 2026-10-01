package org.huajiager.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * 替身状态接口
 */
public interface IStandState {
	String getStand();

	String getStateName();

	int getStage();

	String getModelID();

	Identifier getTex();

	boolean isSoundLoop();

	void doTask(LivingEntity user);

	void doTaskOutOfTime(LivingEntity user);
}
