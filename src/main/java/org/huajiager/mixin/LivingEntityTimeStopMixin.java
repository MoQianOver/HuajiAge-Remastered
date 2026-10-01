package org.huajiager.mixin;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * THE_WORLD 时停伤害延迟结算（服务端权威，common mixin）。
 *
 *  DIO_HIT 语义：时停期间对被冻结实体（TIME_STOP &gt; 0）造成的【任意来源】伤害
 * 不实时结算，登记进 {@link EventTimeStop} 的延迟伤害表，等目标时停解除（TIME_STOP
 * 归零）时一次性释放。
 *
 * 覆盖范围：替身拳击已由 {@code StandPowerHelper.rangePunchAttack} 单独登记（其本身
 * 不调用 target.damage，无重复）；本 mixin 补齐普攻 / 爆炸 / 箭矢 / 掉落等全部走
 * vanilla 伤害入口的打法。时停结束结算时的 target.damage 调用发生在 TIME_STOP 已归零
 * 之后，不在此拦截范围内，不会引起递归或误拦截。
 *
 * 例外：
 * <ul>
 *   <li>二向箔（ItemSecondFoil）为必杀武器，时停中手持攻击实时结算，确保击杀与
 *       额外掉落（虚空镜片）不因延迟结算被吞（postHit 依赖 damage 返回 true）；</li>
 *   <li>时停中对目标施加的火焰（setOnFireFor，如火焰附加）不实时点燃，登记进
 *       {@link EventTimeStop} 延迟火焰表，目标时停解除时一次性施加，火焰时长完整保留。</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTimeStopMixin {

	@Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			at = @At("HEAD"), cancellable = true)
	private void huajiager$deferDamageOnTimeStop(DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		// 伤害结算是服务端权威，客户端镜像不做处理
		if (self.getWorld().isClient()) {
			return;
		}
		if (amount <= 0) {
			return;
		}
		// 仅拦截时停中被冻结的目标（实时结算分支原样放行）
		if (NBTHelper.getEntityInteger(self, HuajiConstant.Tags.TIME_STOP) <= 0) {
			return;
		}
		// 二向箔必杀放行：时停中手持二向箔攻击实时结算，保证击杀 + 掉落碎片不被延迟结算吞掉
		Entity attacker = source.getAttacker();
		if (attacker instanceof PlayerEntity pe
				&& (pe.getMainHandStack().isOf(ItemLoader.secondFoil)
						|| pe.getOffHandStack().isOf(ItemLoader.secondFoil))) {
			return;
		}
		// 定位施力者（投射物取主人）；无明确施力者的环境伤害不回滚，直接按原逻辑实时结算
		Entity sourceEntity = attacker != null ? attacker : source.getSource();
		if (sourceEntity == null) {
			return;
		}
		EventTimeStop.registerPendingPunch(sourceEntity, self, amount);
		cir.setReturnValue(false);
	}

}
