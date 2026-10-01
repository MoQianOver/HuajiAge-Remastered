package org.huajiager.entity;

import java.util.List;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemBlancedHelmet;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/**
 * Lord.Lu 翅膀实体生成驱动。
 *
 * 原 ArmorRenderer 背部渲染随玩家盔甲渲染流程隐式出现/消失；改为独立实体后，
 * 需要服务端显式生成：END_SERVER_TICK 每 tick 轮询在线玩家，满足"戴平衡头盔且
 * lord+open"且尚无对应翅膀实体时生成；不满足时的销毁由 EntityLordLuWing.tick
 * 自愈完成（覆盖切回模式/摘盔/死亡/退出/换维度，双保险）。
 */
public final class EventLordLuWing {
	private EventLordLuWing() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (shouldShow(player) && findWing(player) == null) {
					EntityLordLuWing wing = new EntityLordLuWing(EntityLordLuWing.TYPE, player.getServerWorld());
					wing.setUser(player.getUuid().toString());
					wing.setUserName(player.getName().getString());
					wing.setPosition(player.getX(), player.getY(), player.getZ());
					wing.setYaw(player.getYaw());
					player.getServerWorld().spawnEntity(wing);
				}
			}
		});
	}

	private static boolean shouldShow(PlayerEntity player) {
		ItemStack helm = player.getEquippedStack(EquipmentSlot.HEAD);
		return helm.getItem() == ItemLoader.blanceHelmet
				&& ItemBlancedHelmet.isLord(helm) && ItemBlancedHelmet.isOpen(helm);
	}

	private static EntityLordLuWing findWing(ServerPlayerEntity player) {
		List<EntityLordLuWing> wings = player.getServerWorld().getEntitiesByClass(
				EntityLordLuWing.class,
				new Box(player.getBlockPos()).expand(64),
				e -> e.getOwner() == player);
		return wings.isEmpty() ? null : wings.get(0);
	}
}
