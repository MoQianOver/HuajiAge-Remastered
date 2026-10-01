package org.huajiager.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * 五行至尊之芯。
 *  ：头戴已激活（active）但未 lord 的 ItemBlancedHelmet 时，
 * 将头盔置为 lord（解放 Lord.Lu）、消耗之芯、召唤三道闪电，并发送
 * messege.huaji.blancedHelmet.lord.break（含玩家名）；条件不满足时提示 failed。
 */
public class ItemLordKey extends Item {

	public ItemLordKey() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.lord_key:tooltips.1"));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
		if (head.getItem() instanceof ItemBlancedHelmet && ItemBlancedHelmet.isActive(head)
				&& !ItemBlancedHelmet.isLord(head)) {
			head.getOrCreateNbt().putBoolean("lord", true);
			stack.decrement(1);
			player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 1.0F, 1.0F);
			player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 5.0F, 1.0F);
			// 服务端召唤三道闪电（客户端经实体同步包渲染）
			if (!world.isClient) {
				for (int i = 0; i < 3; i++) {
					LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
					if (bolt != null) {
						bolt.refreshPositionAfterTeleport(
								player.getX() + world.random.nextGaussian() * 1.5D,
								player.getY(),
								player.getZ() + world.random.nextGaussian() * 1.5D);
						bolt.setCosmetic(true);
						world.spawnEntity(bolt);
					}
				}
			}
			if (world.isClient) {
				player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.lord.break", player.getName()), false);
			}
			return TypedActionResult.success(stack);
		}
		if (world.isClient) {
			player.sendMessage(Text.translatable("messege.huaji.blancedHelmet.failed"), false);
		}
		return TypedActionResult.pass(stack);
	}
}
