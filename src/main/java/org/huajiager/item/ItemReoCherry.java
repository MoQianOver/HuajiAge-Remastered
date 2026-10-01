package org.huajiager.item;

import org.huajiager.init.sound.SoundLoader;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Reo 樱桃， 。
 *
 * 构造 mp=20000（吃完给替身充 20000 精神力），hunger=3/saturation=1，
 * alwaysEdible；onFoodEaten 服务端回 1 血、发「reo~reo~」消息，随后播放 REO_CHERRY 音效。 * tooltip 三行（第三行取子类未赋值 mp 字段恒为 0，此处改为展示实际充能值 getMp()=20000，
 * 语义对齐父类）。Fabric 侧食物食用手由 FoodComponent 自动接管，无需重写 use。
 */
public class ItemReoCherry extends ItemFoodMP {

    public ItemReoCherry() {
        super(3, 1.0f, 20000);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!world.isClient() && user instanceof PlayerEntity player) {
            player.heal(1.0f);
            player.sendMessage(Text.translatable("message.huajiager.reo_cherry.reo"), false);
        }
        if (user != null) {
            user.playSound(SoundLoader.REO_CHERRY, 1.0f, 1.0f);
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.huajiager.reo_cherry.tooltip.1"));
        tooltip.add(Text.translatable("item.huajiager.reo_cherry.tooltip.2"));
        Text stored = Text.translatable("item.huajiager.reo_cherry.tooltip.3").copy()
                .append(Text.literal(String.valueOf(getMp())).formatted(Formatting.AQUA));
        tooltip.add(stored);
    }
}
