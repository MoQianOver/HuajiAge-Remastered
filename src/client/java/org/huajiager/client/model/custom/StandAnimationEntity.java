package org.huajiager.client.model.custom;

import org.huajiager.stand.custom.script.EntityLivingBaseWrapper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * 暴露给骨骼动画脚本的使用者包装（脚本里的第一个参数 player）。
 *
 * <p>只实现脚本真正用到的方法：速度、挥手进度、是否左手挥手、第一人称朝向系数。
 * 其中挥手进度取真实值（原版该值恒为 0，导致依赖挥手的动画分支从不执行）。</p>
 */
public class StandAnimationEntity {

    private final LivingEntity user;
    private final StandModelInfo info;
    private final float swingProgress;

    public StandAnimationEntity(LivingEntity user, StandModelInfo info, float swingProgress) {
        this.user = user;
        this.info = info;
        this.swingProgress = swingProgress;
    }

    /** 与替身状态脚本同口径：有替身实体时取替身实体速度模长减基准值。 */
    public float getSpeed() {
        return new EntityLivingBaseWrapper(user).getSpeed();
    }

    public float getSwingProgress() {
        return swingProgress;
    }

    public boolean isSwingLeftHand() {
        return user.getActiveHand() == Hand.OFF_HAND;
    }

    /** 第一人称下的朝向修正系数，其余视角返回 1。 */
    public float getRotationFactorFirst() {
        if (info == null || info.rotationFactorFirst == null) {
            return 1f;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null && mc.options.getPerspective().isFirstPerson()) {
            return info.rotationFactorFirst;
        }
        return 1f;
    }
}
