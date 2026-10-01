package org.huajiager.item;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 记忆Disc。
 */
public class ItemDiscMemory extends Item {

	public ItemDiscMemory() {
		super(new Item.Settings());
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
		tooltip.add(Text.translatable("item.huajiage.disc_memory.tooltips.1", Formatting.GRAY + getOwnerName(stack)));
		tooltip.add(Text.translatable("item.huajiage.disc_memory.tooltips.2", Formatting.GRAY + getOwnerType(stack)));
		tooltip.add(Text.translatable("item.huajiage.disc_memory.tooltips.3", Formatting.GRAY + getOwnerUUID(stack)));
		tooltip.add(Text.translatable("item.huajiage.disc_memory.tooltips.4"));
		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			String name = target.getName().getString();
			String uuid = target.getUuid().toString();
			String type = target.getClass().getName();
			if (type.equals(getOwnerType(stack)) && !(target instanceof PlayerEntity)) {
				if (!player.getWorld().isClient) {
					NbtCompound data = getNbtData(stack);
					if (!data.isEmpty()) {
						ItemStack discMemory = ItemDiscMemory.getDiscMemory(target);
						if (!target.getName().getString().equals(getOwnerName(stack))) {
							target.setCustomName(Text.literal(getOwnerName(stack)));
						}
						target.readNbt(data);
						stack.decrement(1);
						target.dropStack(discMemory, 0.25f);
					}
				}
				if (player.getWorld().isClient) {
					player.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
					player.getWorld().syncGlobalEvent(2003, BlockPos.ofFloored(target.getPos().add(0, target.getStandingEyeHeight(), 0)), 0);
				}
			} else {
				if (player.getWorld().isClient) {
					player.sendMessage(Text.translatable("message.huajiage.disc_memory.insert.fail.type"), false);
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public Text getName(ItemStack stack) {
		if (!getOwnerName(stack).isEmpty()) {
			return Text.literal(super.getName(stack).getString() + " " + getOwnerName(stack));
		}
		return super.getName(stack);
	}

	public static String getOwnerName(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.NAME.getTag());
		}
		return "";
	}

	public static void setOwnerName(ItemStack stack, String name) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.NAME.getTag(), name);
		}
	}

	public static String getOwnerType(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.TYPE.getTag());
		}
		return "";
	}

	public static void setOwnerType(ItemStack stack, String name) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.TYPE.getTag(), name);
		}
	}

	public static String getOwnerUUID(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.UUID.getTag());
		}
		return "";
	}

	public static void setOwnerUUID(ItemStack stack, String uuid) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.UUID.getTag(), uuid);
		}
	}

	public static NbtCompound getNbtData(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			return NBTHelper.getTagCompoundSafe(stack).getCompound(TAGS.NBT.getTag());
		}
		return new NbtCompound();
	}

	public static void setNbtData(ItemStack stack, NbtCompound nbtTagCompound) {
		if (stack.getItem() instanceof ItemDiscMemory) {
			NBTHelper.getTagCompoundSafe(stack).put(TAGS.NBT.getTag(), nbtTagCompound);
		}
	}

	public static void setOwner(ItemStack stack, String name, String uuid, String type) {
		setOwnerName(stack, name);
		setOwnerUUID(stack, uuid);
		setOwnerType(stack, type);
	}

	public static ItemStack getDiscMemory(LivingEntity livingBase) {
		ItemStack discMemory = new ItemStack(ItemLoader.discMemory);
		NbtCompound nbt = new NbtCompound();
		livingBase.writeNbt(nbt);
		ItemDiscMemory.setOwner(discMemory, livingBase.getName().getString(), livingBase.getUuid().toString(),
				livingBase.getClass().getName());
		setNbtData(discMemory, nbt);
		return discMemory;
	}

	public enum TAGS {
		NAME("owner_name"),
		TYPE("owner_type"),
		UUID("owner_uuid"),
		NBT("nbt_entity");

		private final String tag;

		TAGS(String key) {
			tag = key;
		}

		public String getTag() {
			return tag;
		}
	}
}
