package org.huajiager.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 时停冻结期投影物（普通箭 ArrowEntity）朝向保持（服务端/客户端 common）。
 *
 * vanilla PersistentProjectileEntity.tick 只在 prevPitch==0 && prevYaw==0 时按
 * 当前速度反算一次朝向并写 prev*。时停中射出的新箭会被 EventTimeStop 在首 tick
 * 前冻结（setVelocity(ZERO) 清速），此时 vanilla 用 atan2(0,0)=0 把 yaw/pitch
 * 覆盖成默认方向，冻结期渲染表现为"一律朝南"。本 mixin 在 tick 头部识别该竞态：
 * 若当前速度恰为零且朝向尚未初始化（prev 均为 0），就把 prev 置为当前 yaw/pitch
 * 的非 0 哨兵，使 vanilla 的逆算分支被跳过，保留 ItemHeroBow.initProjectileFacing
 * 已设的正确朝向；实体本身位移仍保持清零（冻结语义不变）。
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

	@Unique
	private boolean huajiager$frozenFacing = false;

	@Unique
	private float huajiager$preserveYaw;

	@Unique
	private float huajiager$preservePitch;

	@Inject(method = "tick", at = @At("HEAD"))
	private void huajiager$armPreserveFacing(CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
		boolean frozenSpeed = self.getVelocity().lengthSquared() < 1e-4;
		if (frozenSpeed) {
			if (!this.huajiager$frozenFacing) {
				// 首次检测到冻结（速度刚被清零）：此刻朝向仍是正确值，锁定为保持目标。
				this.huajiager$frozenFacing = true;
				this.huajiager$preserveYaw = self.getYaw();
				this.huajiager$preservePitch = self.getPitch();
			}
		} else {
			// 速度恢复（时停结束/正常飞行）：解除冻结态。
			this.huajiager$frozenFacing = false;
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void huajiager$restorePreserveFacing(CallbackInfo ci) {
		if (!this.huajiager$frozenFacing) {
			return;
		}
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
		// vanilla 重算已将朝向写成 atan2(0,0)=正南，这里强制回写保持目标。		// prev 角同步为一致值，避免渲染插值在 prevYaw->yaw 间再产生"逐渐向南"的视觉。
		self.setYaw(this.huajiager$preserveYaw);
		self.setPitch(this.huajiager$preservePitch);
		EntityPrevAnglesAccessor angles = (EntityPrevAnglesAccessor) (Object) self;
		angles.huajiager$setPrevYaw(this.huajiager$preserveYaw);
		angles.huajiager$setPrevPitch(this.huajiager$preservePitch);
	}
}
