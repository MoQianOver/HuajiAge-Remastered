package org.huajiager.client.event;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemOrgaArmor;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 虫箭右键——客户端表现（ItemArrowRequiem.use 的客户端分支）。
 *
 * splitEnvironmentSourceSets 拆分设计：ItemArrowRequiem 位于 main 源集，
 * 客户端播放（HuajiSoundPlayerClient 真实播放链路）与本地聊天提示本应走
 * client-only 类；main 的 use() 只处理服务端结算，此事件在客户端预测
 * （UseItemCallback 于客户端触发）时拦截虫箭右键：
 *  - 四件套齐全（ItemOrgaArmor.hasAllOrgaArmor）：停掉当前全部音乐，
 *    播放 ORGA_REQUIEM_1，并发送 message.huaji.orga.awake.1。 *  - 四件套不全：不播音乐（提示由服务端 use() 发送，避免双播双提示）。
 *
 * 返回 TypedActionResult.pass 保持默认物品使用流程（不吞事件），
 * 服务端 use() 仍会正常执行效果结算。
 */
public final class EventArrowRequiemUse {

	private EventArrowRequiemUse() {
	}

	public static void register() {
		UseItemCallback.EVENT.register(EventArrowRequiemUse::onUse);
	}

	private static TypedActionResult<ItemStack> onUse(PlayerEntity player, World world, Hand hand) {
		if (world == null || !world.isClient || player == null) {
			return TypedActionResult.pass(ItemStack.EMPTY);
		}
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(ItemLoader.arrowRequiem)) {
			return TypedActionResult.pass(stack);
		}
		if (ItemOrgaArmor.hasAllOrgaArmor(player)) {
			HuajiSoundPlayer.stopAllSounds();
			HuajiSoundPlayer.playMusic(SoundLoader.ORGA_REQUIEM_1);
			player.sendMessage(Text.translatable("message.huaji.orga.awake.1"), false);
		}
		return TypedActionResult.pass(stack);
	}
}
