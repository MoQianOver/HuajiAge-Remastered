package org.huajiager.mixin;

import org.huajiager.init.HuajiConstant;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 时停中对冻结目标施加火焰（火焰附加等走 {@link Entity#setOnFireFor(int)} 的入口）：
 * 不实时点燃，登记进 {@link EventTimeStop} 延迟火焰表，目标时停解除时一次性施加，
 * 避免时停期间 fireTicks 被消耗殆尽、时停结束后目标不再燃烧。
 *
 * 1.20.1 yarn 映射中 setOnFireFor(int) 定义在 {@link Entity}（非 LivingEntity），
 * 故单独建 Entity mixin 承接此注入。
 */
@Mixin(Entity.class)
public abstract class EntityTimeStopMixin {

	@Inject(method = "setOnFireFor", at = @At("HEAD"), cancellable = true)
	private void huajiager$deferFireOnTimeStop(int seconds, CallbackInfo ci) {
		if (seconds <= 0) {
			return;
		}
		Entity self = (Entity) (Object) this;
		// 火焰结算逻辑仅对生物有意义，且为服务端权威，客户端镜像不做处理
		if (self.getWorld().isClient()) {
			return;
		}
		if (!(self instanceof LivingEntity living)) {
			return;
		}
		if (NBTHelper.getEntityInteger(living, HuajiConstant.Tags.TIME_STOP) <= 0) {
			return;
		}
		EventTimeStop.registerPendingFire(living, seconds);
		ci.cancel();
	}
}
