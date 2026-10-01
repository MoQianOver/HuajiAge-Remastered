package org.huajiager.stand.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemOrgaArmor;
import org.huajiager.item.ItemOrgaRequiem;
import org.huajiager.network.StandNetWorkHandler;
import org.huajiager.stand.messages.MessageParticleGenerator;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * 停不下来的奥尔加——受击反馈（EventOrga.onOrgaPlayerHurt 段）
 * 与 potionOrgaTarget 追踪伤害。
 *
 * 受击（仅玩家，穿齐 ORGA 四件套/替身触发或物品栏持有绑定本人 UUID 的镇魂曲物品，攻击者为存活 LivingEntity 且非自己）：
 *  1) 持有效镇魂曲（背包绑定本人 orgaRequiem 或替身触发）→ 攻击者受 2 倍伤害反击
 *     （ EntityDamageSource REQUIEM_BACK 来源为玩家；Fabric 注册 requiem_back 伤害类型，
 *      来源=玩家，死亡提示 death.attack.huajiager.requiem.back）。 *  2) 玩家血量 &lt; 2（半颗心）时：
 *     - 无镇魂曲 → 附近播放 ORGA_SHOT + SPELL_INSTANT 粒子 150（扩散 2）。 *     - 有镇魂曲 → FIREWORKS_SPARK 粒子 200（扩散 2）。 *     - 攻击者未被标记 → 施加 potionOrgaTarget 100 tick。
 *
 * potionOrgaTarget 追踪（服务端 END_SERVER_TICK，对任意带标记实体）：
 *  剩余 30 tick 时，以 100 格内最近玩家（优先非自身）为来源造成 30 点
 *  ORGA_SHOT 伤害；该玩家未持有镇魂曲时发送 message.huaji.orga.shot 提示。
 */
public final class EventOrgaRequiemHurt {

	private static final double ORGA_TARGET_RANGE = 100.0;
	private static final float ORGA_SHOT_DAMAGE = 30.0F;

	/** orga_shot.ogg 实际时长约 4.88s，换算为 tick（20t/s）取整 98 */
	private static final int ORGA_SHOT_SOUND_TICKS = 98;

	/** 每个玩家的低血反馈状态：音效播放期间不重复触发，冷却结束后重新触发 */
	private static final Map<UUID, LowHpState> LOW_HP_STATES = new HashMap<>();

	private static final class LowHpState {
		/** 最近一次触发低血反馈时的攻击者 */
		UUID attackerId;
		/** 冷却期间同一攻击者连续命中累计的伤害（音效结束后结算） */
		float accumulated;
		/** 剩余音效播放 tick，>0 表示音效还在播放 */
		int soundCooldown;
	}

	private EventOrgaRequiemHurt() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(EventOrgaRequiemHurt::onOrgaPlayerHurt);
		ServerTickEvents.END_SERVER_TICK.register(EventOrgaRequiemHurt::tickOrgaTarget);
	}

	/**  onOrgaPlayerHurt：四件套/替身 + 非己存活攻击者 → 反击 + 低血音效粒子 + 标记 */
	private static boolean onOrgaPlayerHurt(LivingEntity living, DamageSource source, float amount) {
		if (!(living instanceof PlayerEntity player) || living.getWorld().isClient) {
			return true;
		}
		// 生效条件：穿齐 ORGA 四件套/替身触发，或物品栏持有绑定本人 UUID 的镇魂曲物品
		// （tooltip 声明：只要物品栏放有与自己绑定的停不下来的奥尔加便会获得特性，与四件套无关）
		if (!ItemOrgaArmor.hasAllOrgaArmor(living) && !ItemOrgaRequiem.hasValidOrgaRequiem(player)) {
			return true;
		}
		Entity attacker = source.getAttacker();
		if (!(attacker instanceof LivingEntity livingAttacker) || attacker == living) {
			return true;
		}
		// 1) 持有效镇魂曲 → 攻击者受 2 倍伤害反击（来源为玩家本人）
		//  EntityDamageSource(REQUIEM_BACK, player)；Fabric 注册 requiem_back 伤害类型，
		// 来源=玩家，死亡提示 death.attack.huajiager.requiem.back（"%s被自己干掉了"）
		if (ItemOrgaRequiem.hasValidOrgaRequiem(player)) {
			livingAttacker.damage(
					DamageLoader.requiemBack(livingAttacker, player),
					amount * 2.0F);
		}
		// 2) 血量 < 2：低血音效 / 粒子 + 标记攻击者
		// 音效节流：ORGA_SHOT 仍在播放时再次被命中不触发；同一攻击者连续命中伤害叠加。		// 冷却结束后再次受击重新触发音效并结算累计伤害
		if (player.getHealth() < 2.0F) {
			UUID playerId = player.getUuid();
			LowHpState state = LOW_HP_STATES.computeIfAbsent(playerId, k -> new LowHpState());
			Vec3d targetPosition = player.getPos();
			if (state.soundCooldown > 0) {
				// 音效还在播放：不重复触发；同一攻击者连续命中则伤害叠加
				if (livingAttacker.getUuid().equals(state.attackerId)) {
					state.accumulated += amount;
				}
			} else {
				// 音效已结束/首次触发：先结算上一段累计伤害（同一攻击者）
				if (state.attackerId != null && state.accumulated > 0.0F) {
					java.util.List<LivingEntity> found = player.getWorld().getEntitiesByClass(
							LivingEntity.class,
							new net.minecraft.util.math.Box(player.getBlockPos()).expand(ORGA_TARGET_RANGE),
							e -> e.getUuid().equals(state.attackerId));
					if (!found.isEmpty() && found.get(0).isAlive()) {
						found.get(0).damage(DamageLoader.orgaShot(found.get(0), player), state.accumulated);
					}
				}
				// 重新触发低血反馈并重开累计窗口
				state.soundCooldown = ORGA_SHOT_SOUND_TICKS;
				state.attackerId = livingAttacker.getUuid();
				state.accumulated = 0.0F;
				if (!ItemOrgaRequiem.hasValidOrgaRequiem(player)) {
					HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.ORGA_SHOT, 1.0F);
					sendParticleNearby(player, targetPosition, "minecraft:instant_effect", 150);
				} else {
					sendParticleNearby(player, targetPosition, "minecraft:firework", 200);
				}
			}
			if (!livingAttacker.hasStatusEffect(PotionLoader.potionOrgaTarget)) {
				livingAttacker.addStatusEffect(new StatusEffectInstance(PotionLoader.potionOrgaTarget, 100));
			}
		}
		return true;
	}

	/**  onOrgaLivingUpdate：potionOrgaTarget 剩余 30 tick → 最近玩家 30 伤追踪 */
	private static void tickOrgaTarget(net.minecraft.server.MinecraftServer server) {
		// 低血音效冷却递减；离线玩家状态清理
		LOW_HP_STATES.keySet().removeIf(pid -> server.getPlayerManager().getPlayer(pid) == null);
		for (LowHpState state : LOW_HP_STATES.values()) {
			if (state.soundCooldown > 0) {
				state.soundCooldown--;
			}
		}
		for (ServerWorld world : server.getWorlds()) {
			for (Entity e : world.iterateEntities()) {
				if (!(e instanceof LivingEntity entity) || entity.getWorld().isClient) {
					continue;
				}
				StatusEffectInstance effect = entity.getStatusEffect(PotionLoader.potionOrgaTarget);
				if (effect == null || effect.getDuration() != 30) {
					continue;
				}
				PlayerEntity nearest = world.getClosestPlayer(entity, ORGA_TARGET_RANGE);
				if (nearest != null && nearest != entity) {
					if (!ItemOrgaRequiem.hasValidOrgaRequiem(nearest)) {
						nearest.sendMessage(Text.translatable("message.huaji.orga.shot"), false);
					}
					//  EntityDamageSource(ORGA_SHOT, nearest)：同反击近似策略
					entity.damage(DamageLoader.orgaShot(entity, nearest),
							ORGA_SHOT_DAMAGE);
				} else {
					//  DamageSource(ORGA_SHOT)（无来源）：generic 近似
					entity.damage(DamageLoader.orgaShotNoSource(entity), ORGA_SHOT_DAMAGE);
				}
			}
		}
	}

	/** 对齐 sendToNearby：向实体所在维度所有玩家广播粒子包（客户端按距离自然裁剪） */
	private static void sendParticleNearby(LivingEntity center, Vec3d pos, String particle, int count) {
		if (center.getWorld().isClient) {
			return;
		}
		MessageParticleGenerator payload = new MessageParticleGenerator(pos, particle, count, 2, 1);
		for (PlayerEntity player : center.getWorld().getPlayers()) {
			if (player instanceof ServerPlayerEntity serverPlayer) {
				StandNetWorkHandler.sendTo(serverPlayer, payload);
			}
		}
	}
}
