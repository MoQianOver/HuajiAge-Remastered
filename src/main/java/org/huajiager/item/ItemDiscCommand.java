package org.huajiager.item;

import org.huajiager.entity.EntityDiscCommand;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * 命令飞盘， 。
 *
 * 右键投掷一枚受控命令飞盘，携带三种命令类型（爆炸/上升/自伤）。
 *  addPropertyOverride / getSubItems 在 Fabric 侧分别收敛为
 * ItemGroup entries 变体注册（见 ItemLoader）与 NBT 类型读写。
 */
public class ItemDiscCommand extends Item {

	public ItemDiscCommand() {
		super(new Item.Settings().maxCount(16));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		String type = getCommandType(stack);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemDiscCommand) {
			if (!getOwnerUUID(stack).isEmpty() && !player.getUuid().toString().equals(getOwnerUUID(stack))) {
				if (world.isClient) {
					player.sendMessage(Text.translatable("message.huajiage.disc_command.fail"), false);
				}
				return TypedActionResult.fail(stack);
			}
			if (!world.isClient) {
				EntityDiscCommand disc = new EntityDiscCommand(world);
				disc.setCommand(getCommandType(stack));
				disc.setMaster(player.getUuid().toString());
				disc.setPos(player.getX(), player.getY() + player.getHeight() - 0.1f, player.getZ());
				disc.setVelocity(player, player.getPitch(), player.getYaw(), 0f, 1f, 0f);
				world.spawnEntity(disc);
			}
			stack.decrement(1);
		}
		return TypedActionResult.pass(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
		tooltip.add(Text.literal(getOwnerName(stack)).formatted(net.minecraft.util.Formatting.GRAY));
		tooltip.add(Text.literal(getOwnerUUID(stack)).formatted(net.minecraft.util.Formatting.GRAY));
		tooltip.add(Text.translatable("item.huajiage.disc_command.tooltips.type." + getCommandType(stack))
				.formatted(net.minecraft.util.Formatting.GRAY));
		tooltip.add(Text.translatable("item.huajiage.disc_command.tooltips.4"));
		super.appendTooltip(stack, world, tooltip, context);
	}

	public static String getOwnerName(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.NAME.getTag());
		}
		return "";
	}

	public static void setOwnerName(ItemStack stack, String name) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.NAME.getTag(), name);
		}
	}

	public static String getOwnerUUID(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.UUID.getTag());
		}
		return "";
	}

	public static void setOwnerUUID(ItemStack stack, String uuid) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.UUID.getTag(), uuid);
		}
	}

	public static void setOwner(ItemStack stack, String name, String uuid) {
		setOwnerName(stack, name);
		setOwnerUUID(stack, uuid);
	}

	public static String getCommandType(ItemStack stack) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.TYPE.getTag());
		}
		return "";
	}

	public static void setCommandType(ItemStack stack, String type) {
		if (stack.getItem() instanceof ItemDiscCommand) {
			NBTHelper.getTagCompoundSafe(stack).putString(TAGS.TYPE.getTag(), type);
		}
	}

	public enum TAGS {
		NAME("owner_name"),
		UUID("owner_uuid"),
		TYPE("command_type");

		private final String tag;

		TAGS(String key) {
			tag = key;
		}

		public String getTag() {
			return tag;
		}
	}

	public enum TYPES_COMMAND {
		EXPLOSION("explosion"),
		MOVE("move_up"),
		SELF_ATTACK("self_attack");

		private final String command;

		TYPES_COMMAND(String key) {
			command = key;
		}

		public String getCommand() {
			return command;
		}
	}
}
