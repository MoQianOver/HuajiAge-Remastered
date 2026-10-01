package org.huajiager.item;

import java.util.List;

import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * 停不下来的奥尔加镇魂曲（独立编写，MIT 语义参考、不照搬代码）。
 *  。
 *
 * 核心为被动持有逻辑（物品 NBT 绑定 owner + inventoryTick 周期结算）：
 *  1) 首次持有（NBT 无 owner）：
 *     - 写入 owner(UUID) / owner_name(玩家名)，持有者立即获得 5s 加速 II。 *     - 在客户端分支执行 stopSounds + HuajiSoundPlayer.playMusic(ORGA_REQUIEM_2)
 *       （背景音乐级替换）并发送 message.huaji.orga.awake.2。Fabric 侧 playMusic 链路
 *       已（main 源集委托入口 → src/client HuajiSoundPlayerClient 真实播放），
 *       此处恢复真实调用（独立编写），替换此前 player.playSound(ORGA_REQUIEM_2) 本地近似。 *  2) 非持有者持有：每 60 tick 且身上无「希望之花 / 镇魂曲」效果时，播放 ORGA_REQUIEM_HIT 音效，
 *     服务端附加 3s 的「镇魂曲木大目标」标记（potionRequiemTarget）。 *  3) 持有者持有「希望之花」时移除之。 *  4) 服务端每 40 tick 向玩家实体数据同步 REQUIEM_OWNER 标签（供替身/护符联动读取）。
 *
 * tooltip（对齐 addInformation）：
 *  - 持有者行（无 Shift 与 Shift 均显示）：
 *      item.orga_requiem:tooltips.1 + GRAY owner_name，含动态 NBT，经 createOwnerLine() 提供。 *  - 非 Shift：item.orga_requiem:tooltips.2（摘要，含"按住Shift以查看更多"）。 *  - 按住 Shift：item.orga_requiem:tooltips.content.1~3。
 *  因 splitEnvironmentSourceSets，Shift 判断与追加由 client 源集 ItemTooltipHandlers 完成，
 *  本类仅暴露纯 common 静态方法（不引用任何 client 类，故不 override appendTooltip）。
 */
public class ItemOrgaRequiem extends Item {

	public ItemOrgaRequiem() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (entity instanceof PlayerEntity player) {
			update(stack, player);
			if (!player.getWorld().isClient) {
				String stackOwner = getTagCompoundSafe(stack).getString(KEY_OWNER);
				if (!NBTHelper.getEntityString(player, HuajiConstant.Tags.REQUIEM_OWNER).equals(stackOwner)
						&& player.age % 40 == 0) {
					NBTHelper.setEntityString(player, HuajiConstant.Tags.REQUIEM_OWNER, stackOwner);
				}
			}
		}
	}

	/** 对应 update()：首次绑定 owner / 非持有者周期标记 / 持有者移除希望之花 */
	private void update(ItemStack stack, PlayerEntity player) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ItemOrgaRequiem)) {
			return;
		}
		boolean owner = true;
		if (!getTagCompoundSafe(stack).contains(KEY_OWNER)) {
			if (player.getWorld().isClient) {
				// 语义：stopSounds + HuajiSoundPlayer.playMusic(ORGA_REQUIEM_2) 的
				// 客户端背景音乐级替换。playMusic 链路已（main 委托 → src/client
				// HuajiSoundPlayerClient 真实播放），替换此前 player.playSound 本地近似。
				// stopAllSounds 会清掉全部音效（含技能循环 BGM），这里改用 stopMusic
				// 只停旧音乐实例，避免打断镇魂曲技能 BGM / 环境音。
				HuajiSoundPlayer.stopMusic(SoundLoader.ORGA_REQUIEM_2);
				HuajiSoundPlayer.playMusic(SoundLoader.ORGA_REQUIEM_2);
				player.sendMessage(Text.translatable("message.huaji.orga.awake.2"), false);
			}
			getTagCompoundSafe(stack).putString(KEY_OWNER, player.getUuidAsString());
			getTagCompoundSafe(stack).putString(KEY_OWNER_NAME, player.getName().getString());
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2));
		} else if (!getTagCompoundSafe(stack).getString(KEY_OWNER).equals(player.getUuidAsString())) {
			owner = false;
		}

		if (!owner) {
			if (player.age % 60 == 0
					&& !player.hasStatusEffect(PotionLoader.potionFlowerHope)
					&& !player.hasStatusEffect(PotionLoader.potionRequiem)) {
				player.playSound(SoundLoader.ORGA_REQUIEM_HIT, 1.0f, 1.0f);
				if (!player.getWorld().isClient) {
					player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionRequiemTarget, 60));
				}
			}
		} else {
			if (player.hasStatusEffect(PotionLoader.potionFlowerHope)) {
				player.removeStatusEffect(PotionLoader.potionFlowerHope);
			}
		}
	}

	/**  hasValidOrgaRequiem：替身是 StandOrgaRequiem，或背包存在绑定本人 UUID 的镇魂曲物品 */
	public static boolean hasValidOrgaRequiem(LivingEntity living) {
		StandBase stand = StandUtil.getType(living);
		if (stand != null && StandLoader.ORGA_REQUIEM.getName().equals(stand.getName())) {
			return true;
		}
		if (!(living instanceof PlayerEntity player)) {
			return false;
		}
		for (ItemStack stack : player.getInventory().main) {
			if (stack.getItem() instanceof ItemOrgaRequiem
					&& getTagCompoundSafe(stack).getString(KEY_OWNER).equals(player.getUuidAsString())) {
				return true;
			}
		}
		return false;
	}

	// ==================== tooltip（纯 common，供 client handler 调用） ====================

	private static final String KEY_OWNER = "owner";
	private static final String KEY_OWNER_NAME = "owner_name";

	/** 持有者行：item.orga_requiem:tooltips.1 + GRAY owner_name（动态） */
	public static Text createOwnerLine(ItemStack stack) {
		String ownerName = getTagCompoundSafe(stack).getString(KEY_OWNER_NAME);
		return Text.translatable("item.orga_requiem:tooltips.1")
				.copy().append(Text.literal(ownerName).formatted(Formatting.GRAY));
	}

	/** 非 Shift 摘要行：item.orga_requiem:tooltips.2 */
	public static Text createSummaryLine() {
		return Text.translatable("item.orga_requiem:tooltips.2");
	}

	/** 按住 Shift 展示的详细能力说明：item.orga_requiem:tooltips.content.1~3 */
	public static List<Text> createDetailedTooltip() {
		return List.of(
				Text.translatable("item.orga_requiem:tooltips.content.1"),
				Text.translatable("item.orga_requiem:tooltips.content.2"),
				Text.translatable("item.orga_requiem:tooltips.content.3"));
	}

	/** 保持一致：栈上 NBT 不存在时新建空复合标签 */
	private static NbtCompound getTagCompoundSafe(ItemStack stack) {
		return NBTHelper.getTagCompoundSafe(stack);
	}
}
