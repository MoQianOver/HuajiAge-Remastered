package org.huajiager.stand.instance;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.loaders.PotionLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.states.default_set.StateOrgaRequiemDefault;
import org.huajiager.stand.states.various.StateOrgaRequiemFly;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;

/**
 * Orga Requiem 替身。
 *
 *  收尾状态：
 *  - 默认态 StateOrgaRequiemDefault 已挂载。 *  - 飞行态 StateOrgaRequiemFly（various 重型档）已并挂载（状态机内切到 "fly" 即生效，
 *    组合 "undead"/"fly" 的 extraData 由 StandStateBase.addExtraData 记录）。 *  - doStandCapability 已还原：置 trigger、施加替身标记/替身标记(requiem)/加速 II 药水并清除减速。 */
public class StandOrgaRequiem extends StandBase {

    public StandOrgaRequiem() {
        super();
    }

    public StandOrgaRequiem(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                            String texPath, String localName, boolean displayHand) {
        super(name, speed, damage, duration, distance, cost, charge, texPath, localName, displayHand);
        initState(new StateOrgaRequiemDefault(name, ExposedData.States.DEFAULT.getName(), isHandDisplay(), true));
        this.addState("fly", new StateOrgaRequiemFly(name, "fly", isHandDisplay(), true));
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        IExposedData data = StandUtil.getStandData(user);
        if (data != null) {
            data.setTrigger(true);
            // 技能释放不广播 BGM：音乐统一由客户端在 requiem 效果首次出现时 playMusic 循环播放，
            // 避免服务端广播 + 客户端重播双路径造成音乐被掐断重播、歌词显慢
            double d = user.getRandom().nextDouble();
            if (user instanceof PlayerEntity p) {
                p.getInventory().offerOrDrop(new ItemStack(ItemLoader.orgaHairKnife, 16));
                if (d < 0.3) {
                    p.getInventory().offerOrDrop(new ItemStack(ItemLoader.blackCar, 1));
                }
            } else {
                user.dropStack(new ItemStack(ItemLoader.orgaHairKnife, 16));
                if (d < 0.3) {
                    user.dropStack(new ItemStack(ItemLoader.blackCar, 1));
                }
            }
            user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionRequiem, 600));
            user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 600));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 2));
            if (user.hasStatusEffect(StatusEffects.SLOWNESS)) {
                user.removeStatusEffect(StatusEffects.SLOWNESS);
            }
        }
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
        // 该方法为空实现
    }
}
