package org.huajiager.stand.custom.script;

import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.helper.StandPowerHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 生物实体包装。
 * <p>供自定义替身 JS 脚本读取使用者实时属性（朝向 / 位置 / 速度 / 替身实体等）。
 */
public class EntityLivingBaseWrapper {

    private LivingEntity livingBase;

    public EntityLivingBaseWrapper(LivingEntity livingBase) {
        this.livingBase = livingBase;
    }

    public LivingEntity getLivingBase() {
        return this.livingBase;
    }

    public int ticksExisted() {
        return this.livingBase.age;
    }

    public float getYaw() {
        return this.livingBase.getYaw();
    }

    public float getPitch() {
        return this.livingBase.getPitch();
    }

    public Vec3dWrapper getPos() {
        return new Vec3dWrapper(this.livingBase.getPos());
    }

    public Vec3dWrapper getEyePos() {
        return new Vec3dWrapper(this.livingBase.getEyePos());
    }

    public Vec3dWrapper getLookVec() {
        return new Vec3dWrapper(this.livingBase.getRotationVector());
    }

    public float getSpeed() {
        LivingEntity standEntity = StandPowerHelper.getUserStand(this.livingBase);
        float s = (float) new Vec3d(livingBase.getVelocity().x, livingBase.getVelocity().y, livingBase.getVelocity().z)
                .length();
        if (standEntity != null) {
            s = (float) (new Vec3d(standEntity.getVelocity().x, standEntity.getVelocity().y,
                    standEntity.getVelocity().z).length() - 0.784);
        }
        return s;
    }

    public EntityStandBase getStandEntity() {
        return StandPowerHelper.getUserStand(this.livingBase);
    }
}
