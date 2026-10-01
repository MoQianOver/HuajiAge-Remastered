package org.huajiager.stand.states.default_set;

import org.huajiager.entity.EmeraldBulletEntity;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.HAMathHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Hierophant Green 默认态，
 * 。
 *
 * 周期性向自身前方散射翡翠弹幕（EntityEmeraldBullet）。
 */
public class StateHierophantGreenDefault extends StandStateBase {
    public StateHierophantGreenDefault() {
    }

    public StateHierophantGreenDefault(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        super(stand, stateName, isHandPlay, soundLoop);
    }

    @Override
    public void doTask(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        int stage = StandUtil.getStandStage(user);
        if (type == null) {
            return;
        }
        if (!user.getWorld().isClient) {
            if (stage > 0 && user.age % 5 == 0 || user.age % 8 == 0) {
                Vec3d shoot_point = HAMathHelper.getPostionRelative2D(user, -0.55f, -0.6f);
                EmeraldBulletEntity bullet = new EmeraldBulletEntity(user.getWorld(), user);
                bullet.setPosition(user.getX() + shoot_point.x, user.getY() + 2.2f, user.getZ() + shoot_point.z);
                bullet.setRotation(user.getYaw());
                bullet.setPitch(user.getPitch());
                float r = (float) Math.random() * 360;
                bullet.setRotationRandom(r);
                bullet.setLife(10 * 20);
                bullet.setDamage(stage > 0 ? StandLoader.HIEROPHANT_GREEN.getDamage() + 2
                        : StandLoader.HIEROPHANT_GREEN.getDamage());
                bullet.setVelocity(user, user.getPitch(), user.getYaw(), 0, 2.5f, 0.2f);
                user.getWorld().spawnEntity(bullet);
            }
        }
    }

    @Override
    public void doTaskOutOfTime(LivingEntity user) {
        super.doTaskOutOfTime(user);
    }
}
