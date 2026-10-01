package org.huajiager.stand.events;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.util.NBTHelper;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 奥尔加镇魂曲攻击链（ ）：
 * <ol>
 *   <li>{@code onRequiemHit}（AttackEntityCallback）：镇魂曲技能期间（potionRequiem 激活）
 *       且空手命中生物时，播 ORGA_REQUIEM_HIT 音效 + 5 点 requiem_hit 伤害（
 *       EntityDamageSource("huajiage.requiem.hit", 玩家)，死亡提示
 *       death.attack.huajiager.requiem.hit）+ 打 REQUIEM=60 / PLAYER_NAME 标记，并补
 *       一击沿玩家→目标方向的击退（对齐 空手 f=1.0 手感；1.20.1 默认仅 0.4，
 *       故补 0.6 使总量近似）；</li>
 *   <li>{@code requiemTarget}（服务端每 tick 遍历）：消费 REQUIEM 标记——非玩家目标每
 *       5 tick 受发动者 maxHealth/3 伤害，被标记玩家每 10 tick 受 12+maxHealth 伤害，
 *       即用户体感的"持续攻击"；</li>
 *   <li>{@code requiemHit}（服务端每 tick 遍历）：消费 potionRequiemTarget 药水标记——
 *       非玩家且无花/无镇魂曲时每 5 tick 受 12+maxHealth（发动者在线）或 32 点无来源
 *       魔法伤害（发动者离线）。</li>
 * </ol>
 * Fabric 1.20.1 无全局 LivingUpdateEvent，与 EventTimeStop 一致，以
 * ServerTickEvents.END_SERVER_TICK 遍历全实体等价承接三段逻辑。
 */
public final class EventOrgaRequiemHit {

	/** 镇魂曲额外攻击音效（orga_requiem_hit）时长 2.624s ≈ 53 tick：防重叠冷却。
	 *  同一玩家两次命中间隔不足该值时沿用当前音效实例不重播，音效放完后再攻击才播新音效。 */
	private static final int REQUIEM_HIT_SOUND_COOLDOWN = 53;

	/** 各玩家最近一次播放额外攻击音效的世界 tick（仅服务端主线程访问，普通 HashMap 足够）。 */
	private static final Map<UUID, Long> LAST_HIT_SOUND_TICK = new HashMap<>();

	private EventOrgaRequiemHit() {
	}

	public static void register() {
		AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			if (!(target instanceof LivingEntity living)) {
				return ActionResult.PASS;
			}
			// 仅技能期间（potionRequiem 激活）且空手（主手无武器）命中才结算
			if (!player.hasStatusEffect(PotionLoader.potionRequiem)
					|| !player.getMainHandStack().isEmpty()) {
				return ActionResult.PASS;
			}
			onRequiemHit(player, living);
			return ActionResult.PASS;
		});

		// 服务端权威持续结算（等价 LivingUpdateEvent）：每 tick 遍历全实体消费
		// REQUIEM / potionRequiemTarget 两种标记
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (Entity e : world.iterateEntities()) {
					if (!(e instanceof LivingEntity living)) {
						continue;
					}
					requiemTarget(living);
					requiemHit(living);
				}
			}
		});
	}

	/** 对应 EventRequiem.onRequiemHit：音效 + 5 点 requiem_hit 伤害 + REQUIEM/PLAYER_NAME 标记 + 击退。 */
	private static void onRequiemHit(PlayerEntity player, LivingEntity target) {
		// 额外攻击音效防重叠：若该玩家上一次命中播放的 orga_requiem_hit 尚未放完
		// （间隔 < 53 tick）则沿用当前播放实例不再新播；音效放完后再次命中才播新音效。
		long now = player.getWorld().getTime();
		Long last = LAST_HIT_SOUND_TICK.get(player.getUuid());
		if (last == null || now - last >= REQUIEM_HIT_SOUND_COOLDOWN) {
			HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.ORGA_REQUIEM_HIT, 1.0f);
			LAST_HIT_SOUND_TICK.put(player.getUuid(), now);
		}
		//  EntityDamageSource("huajiage.requiem.hit", 玩家)；Fabric 注册 requiem_hit 伤害类型，
		// 来源=玩家，死亡提示 death.attack.huajiager.requiem.hit（"%s被替身木大木大木大木大木大"）
		target.damage(DamageLoader.requiemHit(target, player), 5.0f);
		NBTHelper.setEntityInteger(target, HuajiConstant.Tags.REQUIEM, 60);
		NBTHelper.setEntityString(target, HuajiConstant.Tags.PLAYER_NAME, player.getName().getString());
		// 对齐 空手攻击 f=1.0 的击退手感（1.20.1 默认仅 0.4，补 0.6 近似总量），方向沿玩家→目标
		target.takeKnockback(0.6, target.getX() - player.getX(), target.getZ() - player.getZ());
	}

	/** 对应 EventRequiem.requiemTarget：REQUIEM 标记递减 + 持续伤害结算。 */
	private static void requiemTarget(LivingEntity target) {
		int requiem = NBTHelper.getEntityInteger(target, HuajiConstant.Tags.REQUIEM);
		if (requiem <= 0) {
			return;
		}
		String name = NBTHelper.getEntityString(target, HuajiConstant.Tags.PLAYER_NAME);
		PlayerEntity player = target.getWorld().getServer().getPlayerManager().getPlayer(name);
		if (player == null) {
			return;
		}
		NBTHelper.setEntityInteger(target, HuajiConstant.Tags.REQUIEM, requiem - 1);
		if (!(target instanceof PlayerEntity)) {
			if (target.age % 5 == 0) {
				target.damage(target.getDamageSources().indirectMagic(player, player),
						player.getMaxHealth() / 3.0f);
			}
		} else if (target.age % 10 == 0) {
			target.damage(target.getDamageSources().indirectMagic(player, player),
					12.0f + player.getMaxHealth());
		}
	}

	/** 对应 EventRequiem.RequiemHit：potionRequiemTarget 药水标记的持续伤害。 */
	private static void requiemHit(LivingEntity target) {
		if (!target.hasStatusEffect(PotionLoader.potionRequiemTarget)) {
			return;
		}
		if (target instanceof PlayerEntity
				|| target.hasStatusEffect(PotionLoader.potionFlowerHope)
				|| target.hasStatusEffect(PotionLoader.potionRequiem)) {
			return;
		}
		if (target.age % 5 != 0) {
			return;
		}
		String name = NBTHelper.getEntityString(target, HuajiConstant.Tags.PLAYER_NAME);
		PlayerEntity player = target.getWorld().getServer().getPlayerManager().getPlayer(name);
		if (player != null) {
			target.damage(DamageLoader.requiemHit(target, player),
					12.0f + player.getMaxHealth());
		} else {
			target.damage(DamageLoader.requiemHitNoSource(target), 32.0f);
		}
	}
}
