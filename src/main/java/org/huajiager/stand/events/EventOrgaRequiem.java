package org.huajiager.stand.events;

import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemOrgaArmor;
import org.huajiager.item.ItemOrgaHair;
import org.huajiager.item.ItemOrgaRequiem;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Predicate;

/**
 * 停不下来的奥尔加——不死被动，效果由 handleOrgaEntityDeath / handleOrgaEntityUpdate 驱动。
 *
 * 死亡判定（仅玩家，满足 hasAllOrgaArmor 或物品栏持有绑定本人 UUID 的镇魂曲物品时生效）：
 *  1) 替身 StandOrgaRequiem 已召唤触发：取消死亡 + setHealth(1f)（最多半颗心）
 *     + 加速 IV + 跳跃 III + 播 ORGA_REQUIEM_PROTECT。 *  2) 无 requiem / 无希望之花效果：背包持有效镇魂曲物品 → 施加 potionRequiem 600tick，
 *     否则施加 potionFlowerHope 2820tick（无替身给希望之花）。 *  3) 希望之花 duration < 20 → 真死（放行死亡）；其余情况取消死亡 + setHealth(1f)。
 *
 * 花效果 tick（服务端 END_SERVER_TICK）：
 *  - 持有效镇魂曲物品 → 移除希望之花（取回物品花即消散）。 *  - duration == 2720：减速 V + 挖掘疲劳 V + 虚弱 V。 *  - duration == 844：减速 X + 挖掘疲劳 X + 虚弱 X + 失明 + 黑暗。 *  - duration < 20：直接处死（HOPE_FLOWER 无来源巨量伤害，死亡提示"终究逃不过世界线收束的生草命运"）。
 */
public final class EventOrgaRequiem {

	/** 镇魂曲歌词播放的 elapsed tick 列表 */
	private static final List<Integer> SING_LIST = List.of(
			104, 244, 368, 474, 544, 776, 862, 942, 962, 1010,
			1096, 1226, 1364, 1460, 1470, 1520, 1566, 1620, 1700, 1876,
			1956, 2008, 2226, 2260, 2280, 2520);

	private EventOrgaRequiem() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DEATH.register(EventOrgaRequiem::onOrgaPlayerDeath);
		// 退出重进：身上残留希望之花效果 → 直接处死（复用到期语义：毁四件套 + HOPE_FLOWER 巨量伤害）
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (player.hasStatusEffect(PotionLoader.potionFlowerHope)) {
				destroyOrgaSet(player);
				player.damage(DamageLoader.hopeFlower(player), Float.MAX_VALUE);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				updateFlowerHope(player);
				updateRequiemLyrics(player);
			}
		});
	}

	private static boolean onOrgaPlayerDeath(LivingEntity living, DamageSource source, float amount) {
		if (!(living instanceof PlayerEntity player) || living.getWorld().isClient) {
			return true;
		}
		// 真名解放大英雄之弓（STELLA 伤害）可杀死使用者：放行死亡
		if (source.isOf(DamageLoader.STELLA)) {
			return true;
		}
		// 有效判定：穿齐 ORGA 四件套/替身触发，或物品栏持有绑定本人 UUID 的镇魂曲物品
		// （tooltip 声明：只要物品栏放有与自己绑定的停不下来的奥尔加便会获得特性，与四件套无关）
		boolean validRequiem = ItemOrgaRequiem.hasValidOrgaRequiem(player);
		if (!ItemOrgaArmor.hasAllOrgaArmor(player) && !validRequiem) {
			//  else 分支：无四件套/替身且无绑定镇魂曲物品时，若已有希望之花且剩余时间充足 → 依然取消死亡
			StatusEffectInstance flower = player.getStatusEffect(PotionLoader.potionFlowerHope);
			if (flower != null && flower.getDuration() >= 20) {
				player.setHealth(1.0F);
				return false;
			}
			return true;
		}
		// 替身触发中：取消死亡 + 半颗心 + 加速 IV + 跳跃 III + 保护音效
		if (ItemOrgaArmor.isStandTriggered(player)) {
			player.setHealth(1.0F);
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 3));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 100, 2));
			HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.ORGA_REQUIEM_PROTECT, 1.0f);
			// 补语义：不死后同样进入镇魂曲状态，客户端在 requiem==599 时播放 ORGA_REQUIEM_GOLD BGM
			if (!player.hasStatusEffect(PotionLoader.potionRequiem) && !player.hasStatusEffect(PotionLoader.potionFlowerHope)) {
				player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionRequiem, 600));
			}
			return false;
		}
		boolean hasRequiem = player.hasStatusEffect(PotionLoader.potionRequiem);
		boolean hasFlower = player.hasStatusEffect(PotionLoader.potionFlowerHope);
		if (!hasRequiem && !hasFlower) {
			if (ItemOrgaRequiem.hasValidOrgaRequiem(player)) {
				player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionRequiem, 600));
			} else {
				player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionFlowerHope, 2820));
			}
		}
		StatusEffectInstance flower = player.getStatusEffect(PotionLoader.potionFlowerHope);
		if (flower != null && flower.getDuration() < 20) {
			// 希望之花到期真死：摧毁四件套（头饰 + 胸甲/护腿/靴子）
			destroyOrgaSet(player);
			return true;
		}
		player.setHealth(1.0F);
		return false;
	}

	private static void updateFlowerHope(LivingEntity entity) {
		if (entity.getWorld().isClient) {
			return;
		}
		StatusEffectInstance flower = entity.getStatusEffect(PotionLoader.potionFlowerHope);
		if (flower == null) {
			return;
		}
		// 持有有效镇魂曲物品 → 移除希望之花
		if (entity instanceof PlayerEntity player && ItemOrgaRequiem.hasValidOrgaRequiem(player)) {
			player.removeStatusEffect(PotionLoader.potionFlowerHope);
			return;
		}
		int duration = flower.getDuration();
		// 希望之花歌词：按 elapsed tick 播放 sing 台词。
		// elapsed = 2720 - 剩余tick；效果初始 2820，sing.1 在 elapsed==104（效果开始后
		// 2820-2616=204 tick≈10.2s）播放，sing.26 在剩余 200 tick 播放。		// p00~p17 在 elapsed 1876~1893（剩余约 41.4~42.2s）逐 tick 播放，形成 ASCII 动画。
		if (!ConfigHuaji.Huaji.useOrgaFlower && entity instanceof PlayerEntity player) {
			int elapsed = 2720 - duration;
			if (SING_LIST.contains(elapsed)) {
				int singIndex = SING_LIST.indexOf(elapsed) + 1;
				player.sendMessage(Text.translatable("message.huajiager.orga.sing." + singIndex), false);
			}
			if (elapsed >= 1876 && elapsed <= 1893) {
				player.sendMessage(Text.translatable("message.huajiager.orga.sing.p" + (elapsed - 1876)), false);
			}
		}
		// 对齐 onOrgaLivingUpdate：potionFlowerHope duration==112*20-10 时 playSound(ORGA_FLOWER,5f) 并给迟缓。		// Fabric 起始时长 2820，等价播放点为 2820-110=2710（死后约 5.5 秒），由服务端广播保证可靠触发。
		if (duration == 2710) {
			HuajiSoundPlayer.playToNearbyClient(entity, SoundLoader.ORGA_FLOWER, 5.0f);
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2240, 4));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 2240, 4));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 2240, 4));
		} else if (duration == 844) {
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 844, 9));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 844, 9));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 844, 9));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 844, 0));
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 844, 0));
		} else if (duration < 20) {
			// 希望之花到期真死：摧毁四件套（头饰 + 胸甲/护腿/靴子），再以 HOPE_FLOWER（无来源）处死
			// → 死亡提示 death.attack.huajiager.hope_flower（"%s终究逃不过世界线收束的生草命运"）
			if (entity instanceof PlayerEntity player) {
				destroyOrgaSet(player);
			}
			entity.damage(DamageLoader.hopeFlower(entity), Float.MAX_VALUE);
		}
	}

	/**
	 * 镇魂曲 BGM 两句歌词（bgm.1/bgm.2）：
	 * 由服务端在 requiem 效果第 1 tick（duration==599）发送，
	 * 不再依赖客户端同步 duration（Fabric 下 ==599 几乎必错过）。
	 */
	private static void updateRequiemLyrics(ServerPlayerEntity player) {
		StatusEffectInstance requiem = player.getStatusEffect(PotionLoader.potionRequiem);
		if (requiem != null && requiem.getDuration() == 30 * 20 - 1) {
			player.sendMessage(Text.translatable("message.huajiager.orga.requiem.bgm.1"), false);
			player.sendMessage(Text.translatable("message.huajiager.orga.requiem.bgm.2"), false);
		}
	}

	/**
	 * 摧毁玩家身上的四件套：头饰（ItemOrgaHair）+ 胸甲/护腿/靴子（ItemOrgaArmor 各部件）。
	 * 槽位内非四件套物品不动，防止误删玩家其他装备。
	 */
	private static void destroyOrgaSet(PlayerEntity player) {
		destroyIfOrga(player, EquipmentSlot.HEAD, stack -> stack.getItem() instanceof ItemOrgaHair);
		destroyIfOrga(player, EquipmentSlot.CHEST, stack -> stack.getItem() instanceof ItemOrgaArmor.Chestplate);
		destroyIfOrga(player, EquipmentSlot.LEGS, stack -> stack.getItem() instanceof ItemOrgaArmor.Leggings);
		destroyIfOrga(player, EquipmentSlot.FEET, stack -> stack.getItem() instanceof ItemOrgaArmor.Boots);
	}

	private static void destroyIfOrga(PlayerEntity player, EquipmentSlot slot, Predicate<ItemStack> test) {
		ItemStack stack = player.getEquippedStack(slot);
		if (!stack.isEmpty() && test.test(stack)) {
			stack.decrement(1);
		}
	}
}
