package org.huajiager.item;

import java.util.List;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.PotionLoader;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 虫箭（独立编写）。
 *
 * 行为：仅当玩家穿戴全套「替身装甲基类 ItemOrgaArmorBase」时右键才生效——
 * 客户端停止全部音乐并播放 SoundLoader.ORGA_REQUIEM_1 旋律、提示
 * message.huaji.orga.awake.1；服务端消耗主手虫箭、掉落 ItemLoader.orgaRequiem
 * （拾取延迟 120），并施加缓慢/失明/发光/虚弱/反胃/凋零多项负面效果，
 * 若玩家持有「花之希望」药水则一并移除。
 *
 * splitEnvironmentSourceSets 拆分设计：
 *  - use() 仅实现服务端逻辑（四件套校验 / 负面效果 / 移除希望之花 / 消耗 / 掉落）。 *  - 客户端音乐播放与 awake.1 提示由 client 源集事件
 *    {@link org.huajiager.client.event.EventArrowRequiemUse}（UseItemCallback）
 *    拦截实现：main 源集不可引用 client-only 类，且客户端预测与真实播放
 *    统一走 client 事件避免双播。
 *  - 四件套判定复用 {@link ItemOrgaArmor#hasAllOrgaArmor(LivingEntity)}
 *    （替身 StandOrgaRequiem 或 HEAD=发型 + CHEST/LEGS/FEET=衣裤靴）。
 */
public class ItemArrowRequiem extends Item {

    public ItemArrowRequiem() {
        super(new Item.Settings().maxCount(1));
    }

    /**
     * 虫箭完整说明文本（按住 Shift 时展示）。main 源集不含 client 屏幕类，
     * 仅提供文本工厂；Shift 交互由 client 源集 ItemTooltipHandlers 处理。
     */
    public static List<Text> createDetailedTooltip() {
        return List.of(
                Text.translatable("item.huajiager.arrow_requiem.tooltips.1"),
                Text.translatable("item.huajiager.arrow_requiem.tooltips.2"),
                Text.translatable("item.huajiager.arrow_requiem.tooltips.3"));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        // 客户端：音乐 / awake.1 提示由 client 源集 UseItemCallback 事件处理，
        // 此处仅保留默认行为（保持物品可用、向服务端同步使用动作）。
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        // 四件套不全：服务端提示（复用 awake.1），不消耗、不施加效果。
        if (!ItemOrgaArmor.hasAllOrgaArmor(player)) {
            player.sendMessage(Text.translatable("message.huaji.orga.awake.1"), false);
            return TypedActionResult.success(stack);
        }

        // 7 个负面效果（对齐：SLOWNESS 120t 6级 / BLINDNESS 120t /
        // GLOWING 120t / SLOWNESS 120t 7级 / WEAKNESS 120t 6级 /
        // NAUSEA 120t / WITHER 120t 2级）
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 6));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 120, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 120, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 7));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 6));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 2));

        // 移除「花之希望」效果
        if (player.hasStatusEffect(PotionLoader.potionFlowerHope)) {
            player.removeStatusEffect(PotionLoader.potionFlowerHope);
        }

        // 非创造模式消耗 1 个虫箭
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        // 掉落 1 个奥尔加镇魂曲（拾取延迟 120 tick）
        ItemEntity requiemDrop = new ItemEntity(world,
                player.getX(), player.getY() + 0.5, player.getZ(),
                new ItemStack(ItemLoader.orgaRequiem));
        requiemDrop.setPickupDelay(120);
        world.spawnEntity(requiemDrop);

        return TypedActionResult.success(player.getStackInHand(hand));
    }
}
