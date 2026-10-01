package org.huajiager.item;

import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 替身精神力食物基类。
 *
 *  onFoodEaten（服务端）吃完后：若持有替身（StandLoader.getStand 非空）
 * 则给替身充能（chargeHandler.charge(mp)）。Fabric 侧每个子类吃完后也累加替身精神力。
 * 独立编写，行为见各子类实现。
 */
public abstract class ItemFoodMP extends Item {
    private int mp;

    public ItemFoodMP(int amount, float saturation, int mp) {
        super(new Item.Settings()
                .maxCount(64)
                .food(new FoodComponent.Builder()
                        .hunger(amount)
                        .saturationModifier(saturation)
                        .alwaysEdible()
                        .build()));
        setMp(mp);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            IExposedData data = StandUtil.getStandData(player);
            StandHandler chargeHandler = StandUtil.getStandHandler(player);
            if (data != null && chargeHandler != null && StandLoader.getStand(data.getStand()) != null) {
                chargeHandler.charge(this.getMp());
            }
        }
        return super.finishUsing(stack, world, user);
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMp() {
        return mp;
    }
}
