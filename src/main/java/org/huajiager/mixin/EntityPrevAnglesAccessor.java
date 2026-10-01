package org.huajiager.mixin;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Entity 朝向插值字段（prevYaw/prevPitch）访问器。
 *
 * 这两个字段声明在 Entity（而非 PersistentProjectileEntity 自身），用 @Shadow 在
 * PersistentProjectileEntity 上按继承字段解析会报 "not located"，这里改为以 Entity
 * 为目标的 Accessor，供时停冻结期投影物朝向保持逻辑读写。
 */
@Mixin(Entity.class)
public interface EntityPrevAnglesAccessor {

	@Accessor("prevYaw")
	float huajiager$getPrevYaw();

	@Accessor("prevPitch")
	float huajiager$getPrevPitch();

	@Accessor("prevYaw")
	void huajiager$setPrevYaw(float value);

	@Accessor("prevPitch")
	void huajiager$setPrevPitch(float value);
}
