package org.huajiager.item;

import java.util.List;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.capability.IExposedData;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 觉醒之箭（独立编写）。
 *
 * 行为（服务端右键）：若玩家尚未拥有替身（StandData 为空替身），
 * 授予口径由 ConfigHuaji.Stands.arrowStand 决定：
 *   - 已配置且注册名有效：直接授予该替身，不判失败概率；
 *   - 留空或名字无效：从 StandUtil.getArrowStands() 抽取池取随机索引，
 *     按 ConfigHuaji.Stands.chanceStandFail 概率判定觉醒失败。
 * 成功：写入替身名、向附近客户端播放升级音效、提示 mesage.huajiager.stand.gain。
 * 失败：施加缓慢/失明/虚弱/反胃/凋零多项负面效果，播放凋灵受伤音效，
 * 提示 mesage.huajiager.stand.fail。
 * 无论觉醒成败均消耗一支觉醒之箭；玩家已拥有替身时提示 message.huajiager.tarot.stand.fail_load。
 *
 * tooltip：main 源集（splitEnvironmentSourceSets）不含 client 屏幕类，无法直接
 * 调用 Screen.hasShiftDown()，故此处仅提供文本工厂 {@link #createDetailedTooltip()}，
 * 「未按 Shift 显示短提示 / 按住 Shift 显示完整说明」的交互由 client 源集
 * client/event/ItemTooltipHandlers 通过 Fabric ItemTooltipCallback 事件完成。
 */
public class ItemArrowStand extends Item {

    public ItemArrowStand() {
        super(new Item.Settings().maxCount(1));
    }

    /**
     * 觉醒之箭完整说明文本（按住 Shift 时展示）。main 源集不含 client 屏幕类，
     * 仅提供文本工厂；Shift 交互由 client 源集 ItemTooltipHandlers 处理。
     */
    public static List<Text> createDetailedTooltip() {
        return List.of(
                Text.translatable("item.huajiager.arrow_stand.tooltips.1"),
                Text.translatable("item.huajiager.arrow_stand.tooltips.2"),
                Text.translatable("item.huajiager.arrow_stand.tooltips.3"),
                Text.translatable("item.huajiager.arrow_stand.tooltips.4"));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            // 「无替身」判定统一走工程语义 StandUtil.getType == null：
            // 其内部对 null / 空串 / "empty" 三重容错，规避 attachment 初始值
            // 与 StandLoader.EMPTY 字符串直接比对的分支不确定性
            // （data.getStand().equals(EMPTY) 直接比对实测误入 fail_load 分支）。
            // 写入路径改用 getOrCreateStandData：getAttached 对 createDefaulted
            // attachment 返回 initializer 每次新建的临时实例，setStand 不落库。            // getOrCreate 才把实例 attach 到实体 storage，觉醒出的替身才能持久保存。
            IExposedData data = StandUtil.getOrCreateStandData(player);
            if (data != null && StandUtil.getType(player) == null) {

                // 配置指定替身时直接授予（不判失败概率）；留空或名字无效则按抽取池随机 + 失败概率判定。
                StandBase fixed = resolveConfiguredStand();
                StandBase type = fixed != null ? fixed
                        : StandUtil.getTypeWithIndex(world.random.nextInt(100));
                if (fixed != null || Math.random() >= ConfigHuaji.Stands.chanceStandFail) {
                    if (type != null) {
                        data.setStand(type.getName());
                        HuajiSoundPlayer.playToNearbyClient(player, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f);
                        // 冒号后拼接替身本地名（localName 为 lang key，translatable 二次翻译成
                        // 如 "The World-世界"），此前仅发送无参文本，lang 键也无 %s 占位，
                        // 导致提示"恭喜你获得替身："后空白。
                        player.sendMessage(Text.translatable("mesage.huajiager.stand.gain")
                                .append(Text.translatable(type.getLocalName())), false);
                        // 觉醒成功后立即把 STAND_DATA 同步给客户端，使 EventStandKey 召唤判定与
                        // HUD 状态在进入世界后即与服务端一致（避免重进后客户端本地数据缺失）。
                        StandUtil.syncStandData((ServerPlayerEntity) player);
                    }
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 500, 6));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 500, 0));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 500, 3));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 500, 6));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 500, 0));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 500, 2));
                    HuajiSoundPlayer.playToNearbyClient(player, SoundEvents.ENTITY_WITHER_HURT, 1.0f);
                    player.sendMessage(Text.translatable("mesage.huajiager.stand.fail"), false);
                }
                player.getMainHandStack().decrement(1);
            } else {
                player.sendMessage(Text.translatable("message.huajiager.tarot.stand.fail_load"), false);
            }
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    /** 读取配置指定的替身注册名；未配置、空白或名字无效时返回 null（走抽取池随机）。 */
    private static StandBase resolveConfiguredStand() {
        String name = ConfigHuaji.Stands.arrowStand;
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = name.trim();
        StandBase stand = StandLoader.getStand(key);
        if (stand == null) {
            HuajiAgeRemastered.LOGGER.warn(
                    "[HuajiAgeRemastered] 配置的觉醒替身 '{}' 未注册，本次回退到觉醒之箭抽取池", key);
        }
        return stand;
    }
}
