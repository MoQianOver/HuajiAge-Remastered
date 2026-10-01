package org.huajiager.mixin;

import org.huajiager.stand.events.EventWhiteSnake;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 白蛇抽碟掉落物拾取拦截。
 *
 * <p>被白蛇夺走的碟（ItemStack 带 {@link EventWhiteSnake#TAG_DEPRIVE_OWNER} 标记）：
 * <ul>
 *   <li>被夺者本人尝试拾取 → 取消（无法捡回本次被夺走的碟）；</li>
 *   <li>非白蛇使用者的其他玩家尝试拾取 → 取消（仅白蛇使用者可拾取）；</li>
 *   <li>白蛇使用者拾取 → 放行并移除本次掉落限制，转手后即恢复正常拾取。</li>
 * </ul>
 *
 * <p>拾取路径核查结论：1.20.1（yarn build.10）中 {@code ItemEntity} 的玩家拾取
 * 唯一入口是 {@code onPlayerCollision(PlayerEntity)}（tick 循环每 tick 对包围盒内
 * 玩家调用，intermediary method_5694）；不存在 canBePickedUp 等其它玩家判定入口，
 * 因此 HEAD 拦截 + 掉落前预写 NBT 标记（见 EventWhiteSnake#markDepriveStack）
 * 即可覆盖全部拾取路径：掉落物 40 tick 拾取延迟期间标记必然已写入实体 stack。
 *
 * <p>向后兼容：旧版本掉落的碟只有被夺者标记、没有白蛇使用者标记（taker 为空）时，
 * 维持"仅被夺者本人不可拾取"的旧行为，避免旧碟永久无法处理。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityDeprivePickupMixin {

	@Inject(method = "onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V",
			at = @At("HEAD"), cancellable = true)
	private void huajiager$blockDeprivedOwnerPickup(PlayerEntity player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		ItemStack stack = self.getStack();
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains(EventWhiteSnake.TAG_DEPRIVE_OWNER)) {
			return;
		}
		String ownerUuid = nbt.getString(EventWhiteSnake.TAG_DEPRIVE_OWNER);
		String takerUuid = nbt.getString(EventWhiteSnake.TAG_DEPRIVE_TAKER);
		if (ownerUuid.equals(player.getUuid().toString())) {
			// 被夺者本人：不可拾取本次掉落的碟
			ci.cancel();
		} else if (!takerUuid.isEmpty() && !takerUuid.equals(player.getUuid().toString())) {
			// 非白蛇使用者的其他玩家：不可拾取（仅白蛇使用者可拾取）
			ci.cancel();
		} else {
			// 白蛇使用者（或旧标记无 taker 时的其他玩家）捡走：解除本次掉落限制，
			// 并清零拾取延迟（掉落物默认 40 tick 拾取延迟已流逝，此处防御残留值）
			nbt.remove(EventWhiteSnake.TAG_DEPRIVE_OWNER);
			nbt.remove(EventWhiteSnake.TAG_DEPRIVE_TAKER);
			self.setPickupDelay(0);
		}
	}
}
