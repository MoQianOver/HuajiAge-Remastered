package org.huajiager.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

/**
 * 替身 Disc（独立编写）， 。
 *
 * 作用：收纳 / 切换替身的道具，是测试多种替身的前置载体。
 * 右键（服务端）：将 Disc 存储的替身（StandId / StandStage / StandModel）装载到玩家——
 *   - 写入玩家替身数据、状态复位 default、trigger 复位 false、能量上限按新替身重置。 *   - 若玩家原先已拥有其他替身，则在服务器侧把原替身（含阶段/模型）打包为一张
 *     新 Disc 掉落，实现"切换替身"语义。 *   - 消耗手中的 Disc，清除 disc_deprive（被白蛇夺走）标记，播放校准器点击音效。
 *
 * 落库口径与觉醒之箭一致：写入走 getOrCreateStandData（getAttached 对 createDefaulted
 * attachment 返回临时实例不落库），装载后立即 syncStandData 同步客户端。
 *
 * tooltip：显示存储替身 / 阶段 / 模型（本地化替身名，StandUtil 约定 lang key + translatable）。
 *
 * 注：创造模式变体（各原生替身 stage0/1 + 奥尔加镇魂曲 stage3）在 ItemLoader 的
 * ItemGroup entries 中通过 {@link #createDisc(ItemStack, String, int)} 生成。
 */
public class ItemDiscStand extends Item {

    private static final String DEFAULT_STAND_ID = StandLoader.EMPTY;
    private static final String DEFAULT_STAND_STATE = "default";

    public ItemDiscStand() {
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

        String standTag = getStandId(stack);
        if (standTag.isEmpty() || standTag.equals(DEFAULT_STAND_ID)) {
            return TypedActionResult.pass(stack);
        }

        String oldType = data.getStand();
        int oldStage = data.getStage();
        String oldModel = data.getModel();

        int stageTag = getStandStage(stack);
        String modelTag = getStandModel(stack);

        data.setStand(standTag);
        data.setStage(stageTag);
        data.setState(DEFAULT_STAND_STATE);
        data.setModel(modelTag);
        data.setTrigger(false);

        StandBase newStand = StandLoader.getStand(standTag);
        if (newStand != null) {
            StandUtil.setChargeMax(player, newStand.getMaxMP());
            // 需求：Disc 装载后一次性灌满该替身能量，换上即可立刻释放时停等技能。
            // 仅设置能量上限、不补当前值；此为便利增强，不改变蓄能/消耗规则。
            StandHandler handler = StandUtil.getStandHandler(player);
            if (handler != null && handler.getMaxValue() > 0) {
                handler.setChargeValue(handler.getMaxValue());
            }
        }

        // 清除被白蛇夺走替身的标记
        NBTHelper.setDiscDeprive(player, false);

        // 玩家原持有其他替身：打包为一张 Disc 掉落，实现切换
        if (!oldType.equals(DEFAULT_STAND_ID)) {
            player.dropItem(createDisc(new ItemStack(ItemLoader.discStand), oldType, oldStage, oldModel), true);
        }

        HuajiSoundPlayer.playToNearbyClient(player, SoundEvents.BLOCK_COMPARATOR_CLICK, 1f);
        stack.decrement(1);
        StandUtil.syncStandData((ServerPlayerEntity) player);
        return TypedActionResult.pass(stack);
    }

    @Override
    public Text getName(ItemStack stack) {
        String stand = getStandId(stack);
        if (stand.isEmpty() || stand.equals(DEFAULT_STAND_ID)) {
            return super.getName(stack);
        }
        //  getItemStackDisplayName 语义：Disc 名字带上所存储的替身本地名
        return super.getName(stack).copy()
                .append(Text.literal(" ")).append(getStandLocalName(stand));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, java.util.List<Text> tooltip,
                              net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String stand = getStandId(stack);
        int stage = getStandStage(stack);
        String model = getStandModel(stack);
        tooltip.add(Text.translatable("item.huajiager.disc.tooltip.1").append(getStandLocalName(stand)));
        tooltip.add(Text.translatable("item.huajiager.disc.tooltip.2").append(Text.literal(String.valueOf(stage))));
        if (!model.equals(DEFAULT_STAND_ID) && !model.isEmpty()) {
            tooltip.add(Text.translatable("item.huajiager.disc.tooltip.3").append(Text.literal(model)));
        }
        // 自定义替身（紫色隐者/疯狂钻石/白蛇）Disc 追加灰色作者行：
        // getStandId 可能存短名或带 huajiager: 前缀的完整 ID，两种形式都兼容判断。
        String shortId = stand.startsWith("huajiager:") ? stand.substring("huajiager:".length()) : stand;
        if (shortId.equals("hermit_purple") || shortId.equals("crazy_diamond") || shortId.equals("white_snake")) {
            tooltip.add(Text.translatable("item.huajiager.disc.tooltip.author")
                    .append(Text.literal("LH_Lshen")).formatted(Formatting.GRAY));
        }
    }

    // ===== NBT 读写 =====

    public static String getStandId(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.STAND_ID.getTag());
    }

    public static int getStandStage(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getInt(TAGS.STAND_STAGE.getTag());
    }

    public static String getStandModel(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getString(TAGS.STAND_MODEL.getTag());
    }

    /** 通用创造模式变体工厂：单参数（默认 stage0，模型 empty）。 */
    public static ItemStack createDisc(ItemStack stack, String standId) {
        return createDisc(stack, standId, 0, DEFAULT_STAND_ID);
    }

    /** 通用创造模式变体工厂：指定替身与阶段，模型置默认空。 */
    public static ItemStack createDisc(ItemStack stack, String standId, int stage) {
        return createDisc(stack, standId, stage, DEFAULT_STAND_ID);
    }

    /** 通用创造模式变体工厂：完整指定替身 / 阶段 / 模型。 */
    public static ItemStack createDisc(ItemStack stack, String standId, int stage, String model) {
        NbtCompound tag = NBTHelper.getTagCompoundSafe(stack);
        tag.putString(TAGS.STAND_ID.getTag(), standId);
        tag.putInt(TAGS.STAND_STAGE.getTag(), stage);
        tag.putString(TAGS.STAND_MODEL.getTag(), model);
        return stack;
    }

    /** 替身本地名（lang key → 可翻译 Text），未知替身 fallback 原 id。 */
    private static Text getStandLocalName(String standId) {
        if (standId == null || standId.isEmpty() || standId.equals(DEFAULT_STAND_ID)) {
            return Text.literal(standId);
        }
        StandBase stand = StandLoader.getStand(standId);
        if (stand != null) {
            return Text.translatable(stand.getLocalName());
        }
        return Text.literal(standId);
    }

    public enum TAGS {
        STAND_ID("StandId"),
        STAND_STAGE("StandStage"),
        STAND_MODEL("StandModel");

        private final String tag;

        TAGS(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }
}
