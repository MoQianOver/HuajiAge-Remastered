package org.huajiager.item;

import org.huajiager.capability.IExposedData;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 卡其托里钛（独立编写，MIT 语义参考、不照搬代码）。
 *  。
 *
 * 行为（服务端 ）：
 *  1) 玩家当前没有替身（data.getStand().equals(StandLoader.EMPTY)）时：
 *     - 随机取一个 0~99 的替身索引（经 MathHelper.nextFloat(new Random(), 0, 100)
 *       后由 StandUtil.getTypeWithIndex 映射到"觉醒之箭可抽到的替身池"之一，仅用于判空）。 *     - 按 chanceStandFail(0.3) 概率判定觉醒失败。 *     - 成功：把玩家替身直接设为 ORGA_REQUIEM 并置，播放升级音效 +
 *       ORGA_SHOT 枪声音效，附加 25s 加速 VI，发送 huajiager.orga.1 庆贺台词。 *     - 无论觉醒成败，消耗 1 个该物品。 *  2) 已持有替身的玩家右键：仅提示 message.huajiager.tarot.stand.fail_load，不消耗。
 *
 * Fabric 适配说明：
 *  - hasEffect → hasGlint（恒为真，带附魔光效）。 *  -  playToNearbyClient 经 HuajiSoundPlayer 播放事件，Fabric 已就位可直接复用。 *  - getTypeWithIndex 的 Fabric 等价实现（基于 STAND_LIST 取模）已就位，直接调用。
 */
public class ItemOrgaRunning extends Item {

	public ItemOrgaRunning() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			// 「无替身」判定统一走 StandUtil.getType == null（与 ItemArrowStand 一致）：
			// getType 内部对 null / 空串 / "empty" 三重归一，原实现
			// data.getStand().equals(StandLoader.EMPTY) 紧随 attachment 默认字符串形态，
			// 默认值形态一旦变化即误判「已拥有替身」（新世界右键误报根因之一）。
			// 写入路径改用 getOrCreateStandData：getAttached 对 createDefaulted attachment
			// 返回 initializer 每次新建的临时实例，setStand 不落库；getOrCreate 才把实例
			// attach 到实体 storage，激活出的替身才能持久保存。
			IExposedData data = StandUtil.getOrCreateStandData(player);
			if (data != null && StandUtil.getType(player) == null) {
				double chance = Math.random();
				int standNumber = (int) (Math.random() * 100);
				StandBase type = StandUtil.getTypeWithIndex(standNumber);
				if (chance >= ConfigHuaji.Stands.chanceStandFail && type != null) {
					data.setStand(StandLoader.ORGA_REQUIEM.getName());
					data.setStage(3);
					HuajiSoundPlayer.playToNearbyClient(player, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f);
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 500, 6));
					HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.ORGA_SHOT, 1.0f);
					player.sendMessage(Text.translatable("huajiager.orga.1"), false);
				}
				stack.decrement(1);
			} else {
				player.sendMessage(Text.translatable("message.huajiager.tarot.stand.fail_load"), false);
			}
		}
		return TypedActionResult.success(stack);
	}
}
