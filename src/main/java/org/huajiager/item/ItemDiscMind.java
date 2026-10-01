package org.huajiager.item;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 心智Disc， 。
 *
 * 保留：tooltip 展示主人、对实体/自身右键解 strip（disc_deprive）、显示名追加主人名。
 *  onItemUse(GarageKit) 分支强依赖 TouhouMaid 女仆模组 TileEntityGarageKit，
 * 本次按任务要求裁剪，其余逻辑完整保留。
 */
public class ItemDiscMind extends Item {

	public ItemDiscMind() {
		super(new Item.Settings());
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
		tooltip.add(Text.translatable("item.huajiage.disc_mind.tooltips.1", Formatting.GRAY + getOwnerName(stack)));
		tooltip.add(Text.translatable("item.huajiage.disc_mind.tooltips.2", Formatting.GRAY + getOwnerUUID(stack)));
		tooltip.add(Text.translatable("item.huajiage.disc_mind.tooltips.3"));
		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
		if (stack.getItem() instanceof ItemDiscMind) {
			String name = target.getName().getString();
			String uuid = target.getUuid().toString();
			if (name.equals(getOwnerName(stack)) && uuid.equals(getOwnerUUID(stack))) {
				boolean isDeprived = NBTHelper.getDiscDeprive(target);
				if (isDeprived) {
					NBTHelper.setDiscDeprive(target, false);
					if (!player.getWorld().isClient) {
						stack.decrement(1);
					}
					if (player.getWorld().isClient) {
						player.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
						player.getWorld().syncGlobalEvent(2005, BlockPos.ofFloored(target.getPos()).add(0, 1, 0), 0);
					}
				} else {
					if (player.getWorld().isClient) {
						player.sendMessage(Text.translatable("message.huajiage.disc_mind.mind_recover"), false);
					}
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.getItem() instanceof ItemDiscMind) {
			String name = player.getName().getString();
			String uuid = player.getUuid().toString();
			if (name.equals(getOwnerName(stack)) && uuid.equals(getOwnerUUID(stack))) {
				boolean isDeprived = NBTHelper.getDiscDeprive(player);
				if (isDeprived) {
					NBTHelper.setDiscDeprive(player, false);
					if (!world.isClient) {
						stack.decrement(1);
					}
					if (world.isClient) {
						player.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
						world.syncGlobalEvent(2005, player.getBlockPos().add(0, 1, 0), 0);
					}
				} else {
					if (world.isClient) {
						player.sendMessage(Text.translatable("message.huajiage.disc_mind.mind_recover"), false);
					}
				}
			}
		}
		return TypedActionResult.pass(stack);
	}

	@Override
	public Text getName(ItemStack stack) {
		if (!getOwnerName(stack).isEmpty()) {
			return Text.literal(super.getName(stack).getString() + " " + getOwnerName(stack));
		}
		return super.getName(stack);
	}

	public static String getOwnerName(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMind) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.NAME.getTag());
		}
		return "";
	}

	public static void setOwnerName(ItemStack stack, String name) {
		if (stack.getItem() instanceof ItemDiscMind) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.NAME.getTag(), name);
		}
	}

	public static String getOwnerUUID(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMind) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.UUID.getTag());
		}
		return "";
	}

	public static void setOwnerUUID(ItemStack stack, String uuid) {
		if (stack.getItem() instanceof ItemDiscMind) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.UUID.getTag(), uuid);
		}
	}

	public static void setOwner(ItemStack stack, String name, String uuid) {
		setOwnerName(stack, name);
		setOwnerUUID(stack, uuid);
	}

	public static ItemStack getDiscMind(LivingEntity livingBase) {
		ItemStack discMind = new ItemStack(ItemLoader.discMind);
		ItemDiscMind.setOwner(discMind, livingBase.getName().getString(), livingBase.getUuid().toString());
		return discMind;
	}

	public enum TAGS {
		NAME("owner_name"),
		UUID("owner_uuid");

		private final String tag;

		TAGS(String key) {
			tag = key;
		}

		public String getTag() {
			return tag;
		}
	}
}
