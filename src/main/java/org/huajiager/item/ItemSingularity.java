package org.huajiager.item;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 特异点， （独立编写）。
 *
 * 行为：右手持拿右击时，若玩家已觉醒替身且阶段为 0 ——
 * 服务端：消耗主手 1 个，施加失明/凋零/中毒/缓慢/饥饿各 200 tick，
 * 并在实体附加数据写 SINGULARITY=200 标记（用于后续压缩结算）。 * 客户端：在此处用 HuajiSoundPlayer.playMusic 依次播放 CHARGE / ENERGY_HIT /
 * NOISE_FURNACE 三条音乐并本地发送提示消息。playMusic 链路已（main 源集
 * HuajiSoundPlayer 委托入口 → src/client HuajiSoundPlayerClient 真实播放），
 * 此处恢复真实调用（独立编写）。
 */
public class ItemSingularity extends Item {

	public ItemSingularity() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		boolean flag = false;
		StandBase stand = StandUtil.getType(player);
		int stage = StandUtil.getStandStage(player);
		if (stand != null && stage == 0) {
			flag = true;
		}
		if (flag) {
			if (world.isClient) {
				// 用户要求：使用瞬间不播放 CHARGE / ENERGY_HIT 等自定义音效，
				// 仅保留效果完成时（EventStandUpgrade t==3）的 MC 
				// UI_TOAST_CHALLENGE_COMPLETE 音效，故此处不再调用 playMusic。
				player.sendMessage(Text.translatable("message.huajiager.singularity"), false);
			}
			if (!world.isClient) {
				player.getMainHandStack().decrement(1);
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 9));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 9));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 5));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 9));
				NBTHelper.setEntityInteger(player, HuajiConstant.Tags.SINGULARITY, 200);
			}
		}
		return TypedActionResult.success(player.getStackInHand(hand));
	}
}
