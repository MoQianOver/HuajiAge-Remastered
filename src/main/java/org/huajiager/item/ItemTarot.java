package org.huajiager.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

import net.minecraft.nbt.NbtCompound;

/**
 * 命运的塔罗牌（独立编写）。
 *
 * 作用：装载 / 卸载替身，是觉醒之箭外的常用替身切换道具。
 *  - 普通右键：若玩家当前无替身且牌中存有替身 → 装载该替身（阶段/模型一并写入），
 *    播放玻璃碎裂 + 成就音效，清空牌内数据（单次消耗）。 *  - 潜行右键：若牌为空（NBT 无替身名）且玩家已拥有替身 → 将当前替身（含阶段/模型）
 *    存入牌中，清空玩家替身，播放龙息装瓶音效并提示"替身数据已储存"。 *  - 其余情况：提示 message.huajiager.tarot.stand.fail_load（您可能已拥有替身）。
 *
 * 数据落库口径与觉醒之箭一致：替身写入统一走 getOrCreateStandData（getAttached
 * 对 createDefaulted attachment 返回临时实例不落库），装载/卸载后立即 syncStandData
 * 同步客户端，保证召唤判定与 HUD 与服务端一致。
 */
public class ItemTarot extends Item {

    private static final String DEFAULT_STAND_ID = StandLoader.EMPTY;
    private static final String DEFAULT_STAND_STATE = "default";

    public ItemTarot() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.pass(stack);
        }

        IExposedData data = StandUtil.getOrCreateStandData(player);
        if (data == null) {
            return TypedActionResult.pass(stack);
        }

        String stand = data.getStand();
        int stage = data.getStage();
        String model = data.getModel();

        NbtCompound tag = NBTHelper.getTagCompoundSafe(stack);
        boolean emptyCard = isHolderEmpty(getStandName(stack));

        if (player.isSneaking()) {
            // 潜行：卸载（存储）当前替身到空塔罗牌
            if (emptyCard && !stand.equals(DEFAULT_STAND_ID)) {
                storeStand(player, stack, data, stand, stage, model);
            }
        } else {
            // 普通右键：装载牌中替身；空牌且玩家已有替身 → 存储；其余情况静默
            String standTag = getStandName(stack);
            int stageTag = getStandStage(stack);
            String modelTag = getStandModel(stack);
            boolean cardHasStand = !standTag.isEmpty() && !standTag.equals(DEFAULT_STAND_ID);
            if (!cardHasStand) {
                // 空牌
                if (!stand.equals(DEFAULT_STAND_ID)) {
                    // 空牌 + 玩家已有替身：允许普通右键存储当前替身，便于换替身
                    storeStand(player, stack, data, stand, stage, model);
                }
                // 空牌 + 玩家无替身：无可操作，静默返回
            } else if (stand.equals(DEFAULT_STAND_ID)) {
                // 玩家无替身 + 牌中存有替身 → 装载
                data.setStand(standTag);
                data.setStage(stageTag);
                data.setState(DEFAULT_STAND_STATE);
                data.setModel(modelTag);
                data.setTrigger(false);
                // 装载替身仅为牌面生效的"消耗/装载"反馈，不触发成就类提示音
                setTarotTag(stack, DEFAULT_STAND_ID, 0, DEFAULT_STAND_ID);
                StandUtil.syncStandData((ServerPlayerEntity) player);
            } else {
                // 牌中存有替身但玩家已拥有替身：提示先找空牌卸载
                player.sendMessage(Text.translatable("message.huajiager.tarot.stand.fail_load"), false);
            }
        }
        return TypedActionResult.pass(stack);
    }

    /** 将玩家当前替身（含阶段/模型）存入空塔罗牌并清空玩家替身数据。 */
    private void storeStand(PlayerEntity player, ItemStack stack, IExposedData data,
                            String stand, int stage, String model) {
        setTarotTag(stack, stand, stage, model);
        data.setStand(DEFAULT_STAND_ID);
        data.setState(DEFAULT_STAND_STATE);
        data.setStage(0);
        data.setModel(DEFAULT_STAND_ID);
        // 收替身=解除召唤：置 false 触发 EntityStandBase.tick 的 discard()，否则残影实体
        // 不消失，且 state 已被置为 default，残影会渲染成攻击态模型（无伤害）。
        data.setTrigger(false);
        StandBase sb = StandLoader.getStand(data.getStand());
        if (sb != null) {
            StandUtil.setChargeMax(player, sb.getMaxMP());
        }
        player.sendMessage(Text.translatable("message.huajiager.tarot.stand.store"), false);
        StandUtil.syncStandData((ServerPlayerEntity) player);
    }

    @Override
    public Text getName(ItemStack stack) {
        String stand = getStandName(stack);
        if (isHolderEmpty(stand)) {
            return super.getName(stack);
        }
        //  getItemStackDisplayName 语义：存有替身时牌名带上替身本地名
        return super.getName(stack).copy()
                .append(Text.literal(" ")).append(getStandLocalName(stand));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, java.util.List<Text> tooltip,
                              net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String stand = getStandName(stack);
        int stage = getStandStage(stack);
        String model = getStandModel(stack);
        tooltip.add(Text.translatable("item.huajiager.tarot.tooltip.1").append(getStandLocalName(stand)));
        tooltip.add(Text.translatable("item.huajiager.tarot.tooltip.2").append(Text.literal(String.valueOf(stage))));
        if (!model.equals(DEFAULT_STAND_ID) && !model.isEmpty()) {
            tooltip.add(Text.translatable("item.huajiager.tarot.tooltip.3").append(Text.literal(model)));
        }
    }

    // ===== NBT 读写 =====

    public static String getStandName(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.NAME.getTag());
    }

    public static int getStandStage(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getInt(TAGS.STAGE.getTag());
    }

    public static String getStandModel(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.MODEL.getTag());
    }

    /** 写入整张塔罗牌（替身名/阶段/模型），覆盖式。 */
    public static void setTarotTag(ItemStack stack, String standId, int stage, String model) {
        NbtCompound tag = NBTHelper.getTagCompoundSafe(stack);
        tag.putString(TAGS.NAME.getTag(), standId);
        tag.putInt(TAGS.STAGE.getTag(), stage);
        tag.putString(TAGS.MODEL.getTag(), model);
    }

    /** 空牌判定：无替身名（null/空串/empty 均视为空牌）。 */
    public static boolean isHolderEmpty(String standName) {
        return standName == null || standName.isEmpty() || standName.equals(DEFAULT_STAND_ID);
    }

    /** 替身本地名（lang key → 可翻译 Text），未知替身 fallback 原 id。 */
    private static Text getStandLocalName(String standId) {
        if (isHolderEmpty(standId)) {
            return Text.literal(standId);
        }
        StandBase stand = StandLoader.getStand(standId);
        if (stand != null) {
            return Text.translatable(stand.getLocalName());
        }
        return Text.literal(standId);
    }

    public enum TAGS {
        NAME("StandId"),
        STAGE("StandStage"),
        MODEL("StandModel");

        private final String tag;

        TAGS(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }
}
