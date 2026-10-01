package org.huajiager.mixin;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemSecondFoil;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 二向箔丢出拦截（等价 ItemTossEvent）：玩家主动丢出二向箔时设为 10 秒不可捡起，
 * 并打上丢出标记 / 倒计时，供 {@code ItemSecondFoil} 的落地倒计时链路消费。
 *
 * 仅拦截玩家主动丢出（{@code retainOwnership == false}）；死亡掉落（retainOwnership=true）
 * 不触发二段生效，保持语义（死亡掉落可立即捡回）。
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDropMixin {

	@Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
			at = @At("RETURN"), cancellable = true)
	private void huajiager$onDropSecondFoil(ItemStack stack, boolean throwRandomly, boolean retainOwnership,
			CallbackInfoReturnable<ItemEntity> cir) {
		ItemEntity entity = cir.getReturnValue();
		if (entity == null || entity.getWorld().isClient()) {
			return;
		}
		// 死亡掉落不触发丢出二段生效
		if (retainOwnership) {
			return;
		}
		if (!stack.isOf(ItemLoader.secondFoil)) {
			return;
		}
		entity.setPickupDelay(ItemSecondFoil.BURST_TIME);
		NbtCompound nbt = NBTHelper.getTagCompoundSafe(entity.getStack());
		nbt.putBoolean("foil_dropped", true);
		nbt.putInt("foil_countdown", ItemSecondFoil.BURST_TIME);
		PlayerEntity self = (PlayerEntity) (Object) this;
		self.sendMessage(Text.translatable("huajiager.secondhit"), true);
	}
}
