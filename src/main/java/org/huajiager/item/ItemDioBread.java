package org.huajiager.item;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.stand.messages.MessageDioBreadTimeStop;
import org.huajiager.util.ServerUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/**
 * Dio 面包（食物，饱食度 12、饱和度 2f）。
 *
 *  onFoodEaten（服务端）食用后：加 5 种强化药水(9s)、回复 5 点生命、触发时停 THE_WORLD_TIME、
 * 发送替身消息并播放 THE_WORLD / STAR_PLATINUM 音效；30% 概率发放 roadRoller。 addInformation（客户端）按 Shift 显示两段都市传说 tooltip，此后
 * 详情文本交由 createDetailedTooltip() 提供，由 client 源集 ItemTooltipHandlers 按 Shift 展开。
 */
public class ItemDioBread extends Item {

	public ItemDioBread() {
		super(new Item.Settings()
				.maxCount(64)
				.food(new FoodComponent.Builder()
						.hunger(12)
						.saturationModifier(2.0f)
						.alwaysEdible()
						.build()));
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient() && user instanceof PlayerEntity player) {
			// 药水/回血效果不再吃下即给，改由时停真正激活时（EventTimeStop.setTimeStop 触发点）
			// 统一施加，时长 THE_WORLD_TIME 与时停同 tick 开始，可精确覆盖整个时停期间。
			// 注意：Dio 面包时停效果固定为 THE_WORLD 效果，不随玩家当前替身变化。
			player.sendMessage(Text.translatable("message.huajiager.the_world"), false);

			// 时停触发改为"先响音效、到点再时停"：THE_WORLD 系列随机四选一，
			// 立即播放开场音，并按各自音效时长换算延迟 tick（20 tick/s）后再启动时停。
			int idx = (int) (Math.random() * 4);
			SoundEvent sound;
			int delayTicks;
			switch (idx) {
				case 0 -> {
					sound = SoundLoader.THE_WORLD;
					delayTicks = Math.round(0.2f * 20);   // ~0.2s
				}
				case 1 -> {
					sound = SoundLoader.THE_WORLD_1;
					delayTicks = Math.round(1.8f * 20);   // ~1.8s
				}
				case 2 -> {
					sound = SoundLoader.THE_WORLD_2;
					delayTicks = Math.round(3.8f * 20);   // ~3.8s
				}
				default -> {
					sound = SoundLoader.THE_WORLD_3;
					delayTicks = Math.round(1.7f * 20);   // ~1.7s
				}
			}
			// 音效经 S2C 广播给附近玩家（64 格内，含发动者本人），客户端以发动者坐标为
			// 声源播放（服务端世界广播到达时机/衰减不可靠，故仍走 S2C + 客户端播放）
			ServerUtil.sendPacketToNearbyPlayersStand(player,
					new MessageDioBreadTimeStop(sound.getId().toString(),
							player.getX(), player.getY(), player.getZ()));
			EventTimeStop.scheduleDelayedTimeStop(player, delayTicks);
		}
		return super.finishUsing(stack, world, user);
	}

	/**
	 * 按住 Shift 时展示的完整说明文本（由 ItemTooltipHandlers 客户端事件调用）。
	 * 两段同时保留：第一段为物品描述，第二段为都市传说内容。
	 */
	public static List<Text> createDetailedTooltip() {
		return List.of(
				Text.translatable("item.dio_bread:tooltips.1"),
				Text.translatable("item.dio_bread:tooltips.2"));
	}
}
