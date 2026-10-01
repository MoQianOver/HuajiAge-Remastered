package org.huajiager.item;

import java.util.List;

import org.huajiager.entity.EntityOrgaHairKnife;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 头屑飞刀（Orga Hair Knife）。
 *
 * 语义：右键投掷 EntityOrgaHairKnife，伤害 = 投掷者最大生命的一半，寿命 600 tick。 * 命中结算在实体 onCollision 中完成（生命越低伤害越高，镇魂曲期间附加因果标记）。
 *
 * Fabric 侧实现说明：
 * - 声音： HuaJiSoundPlayer.playMovingSoundToClient(ARROW_SHOOT) 为客户端音效，
 *   此处按服务端维度广播近似（world.playSound player=null，随距离衰减），
 *   与 ItemWaveKnife 等已物品的音效处理方式一致。 * - 弹道：EntityOrgaHairKnife(world, player) 构造已按投掷者视线设置初速
 *   （look * 1.5，等价 shootFromRotation(1.5)）与 owner，直接使用。 * - 消耗：非创造模式扣除 1 个，保留"投掷消耗"语义。
 */
public class ItemOrgaHairKnife extends Item {

	/** 飞刀实体寿命（tick），对齐 600 tick 飞行寿命 */
	private static final float KNIFE_LIFE = 600F;

	public ItemOrgaHairKnife() {
		super(new Item.Settings());
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			float pitch = 0.4F / (world.random.nextFloat() * 0.4F + 0.8F);
			world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5F, pitch);

			EntityOrgaHairKnife hair = new EntityOrgaHairKnife(world, player);
			hair.setDamage(player.getMaxHealth() / 2);
			hair.setLife(KNIFE_LIFE);
			world.spawnEntity(hair);
		}

		if (!player.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		player.swingHand(hand);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.orga_knife:tooltips.1"));
	}
}
