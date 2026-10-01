package org.huajiager.stand.events;

import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.item.ItemKillerQueenTrigger;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * 杀手皇后标记事件， 。
 *
 * - onTargetEntity（AttackEntityCallback）：持有杀手皇后且替身激活（potionStand）时攻击生物，
 *   自动发放/更新"点赞"物品的锁定实体 UUID。
 * - onTargetBlock（AttackBlockCallback，原 PlayerEvent.BreakSpeed）：持 KQ 激活时左键点方块，
 *   记录方块坐标为 BLOCK 类型。
 * - onTargetBlock（PlayerBlockBreakEvents.AFTER，原 BlockEvent.BreakEvent）：创造模式破坏方块时
 *   同样记录方块坐标。
 */
public final class EventKillerQueen {

	private EventKillerQueen() {
	}

	public static void register() {
		AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			// 注意：Fabric 1.20.1 的 AttackEntityCallback 传入的 hitResult 经常为 null
			// （历史 issue 已知行为），此处不能因 hitResult==null 直接 return，
			// 否则玩家左键攻击永远走不到发放逻辑（此前"只有连击模式给点赞"的根因）。
			if (target == null || isStandEntity(target)) {
				return ActionResult.PASS;
			}
			if (!isKillerQueenActive(player)) {
				return ActionResult.PASS;
			}
			giveOrUpdateTrigger(player, ItemKillerQueenTrigger.TYPE.ENTITY, target.getUuid().toString(), null);
			return ActionResult.PASS;
		});

		//  PlayerEvent.BreakSpeed：挖掘方块时记录坐标
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			if (!isKillerQueenActive(player)) {
				return ActionResult.PASS;
			}
			giveOrUpdateTrigger(player, ItemKillerQueenTrigger.TYPE.BLOCK, "empty", pos);
			return ActionResult.PASS;
		});

		//  BlockEvent.BreakEvent：仅创造模式破坏方块时记录坐标
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient) {
				return;
			}
			if (!player.isCreative()) {
				return;
			}
			if (!isKillerQueenActive(player)) {
				return;
			}
			giveOrUpdateTrigger(player, ItemKillerQueenTrigger.TYPE.BLOCK, "empty", pos);
		});
	}

	private static boolean isKillerQueenActive(PlayerEntity player) {
		StandBase stand = StandUtil.getType(player);
		return stand != null && StandLoader.KILLER_QUEEN.equals(stand)
				&& player.getStatusEffect(PotionLoader.potionStand) != null;
	}

	/**
	 * 替身自动攻击命中钩子（补充）。
	 *
	 * "点赞"由玩家攻击事件（AttackEntityEvent）发放，但本工程替身自动攻击
	 * （doStandPower → StandPowerHelper.rangePunchAttack）不经过玩家攻击事件，
	 * 导致 KQ 替身自己打生物时永远拿不到"点赞"。本方法供 rangePunchAttack 命中
	 * LivingEntity 时调用：持有 KQ 且激活（potionStand）的玩家攻击命中即发放/更新
	 * 点赞并锁定目标 UUID，与观感一致。
	 */
	public static void onStandHit(LivingEntity user, LivingEntity target) {
		if (user == target || !(user instanceof PlayerEntity player) || worldIsClient(user)
				|| isStandEntity(target)) {
			return;
		}
		if (!isKillerQueenActive(player)) {
			return;
		}
		giveOrUpdateTrigger(player, ItemKillerQueenTrigger.TYPE.ENTITY, target.getUuid().toString(), null);
	}

	/** 替身类实体（KQ 女王炸弹 / 常规替身）不可作为"点赞"锁定目标，避免右键引爆时在玩家脸上爆炸。 */
	private static boolean isStandEntity(Entity entity) {
		return entity instanceof EntitySheerHeartAttack || entity instanceof EntityStandBase;
	}

	private static boolean worldIsClient(LivingEntity entity) {
		return entity.getWorld().isClient;
	}

	private static void giveOrUpdateTrigger(PlayerEntity player, ItemKillerQueenTrigger.TYPE type, String uuid, BlockPos pos) {
		ItemStack found = null;
		for (ItemStack stack : player.getInventory().main) {
			if (!stack.isEmpty() && stack.getItem() == ItemLoader.killerQueenTrigger) {
				found = stack;
				break;
			}
		}
		double x = 0, y = 0, z = 0;
		if (pos != null) {
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();
		}
		if (found == null) {
			ItemStack stack = new ItemStack(ItemLoader.killerQueenTrigger);
			ItemKillerQueenTrigger.setData(stack, type.getName(), uuid, x, y, z,
					player.getWorld().getRegistryKey().getValue().toString());
			player.getInventory().offerOrDrop(stack);
		} else {
			ItemKillerQueenTrigger.setData(found, type.getName(), uuid, x, y, z,
					player.getWorld().getRegistryKey().getValue().toString());
		}
	}
}
