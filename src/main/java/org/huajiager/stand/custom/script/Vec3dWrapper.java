package org.huajiager.stand.custom.script;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 三维向量包装（ ）。
 * <p>供自定义替身 JS 脚本以 OO 方式操作坐标。原代码基于酒石酸团队“车万女仆”模组代码，
 * 依据 MIT 协议进行编写。</p>
 */
public class Vec3dWrapper {

    private Vec3d vec3d;

    public Vec3dWrapper(Vec3d vec3d) {
        this.vec3d = vec3d;
    }

    public Vec3dWrapper(double x, double y, double z) {
        this.vec3d = new Vec3d(x, y, z);
    }

    /**
     * 以实体为原点，按偏航偏移旋转得到三维向量（原 getRotationVector）。
     */
    public static Vec3dWrapper getRotationVector(double x, double y, double z, float yawIn, double yOffset,
            EntityLivingBaseWrapper entityWrapper) {
        LivingEntity entity = entityWrapper.getLivingBase();
        float yaw = (entity.getYaw() + yawIn) * -0.01745329251f;
        Vec3d pos = entity.getPos();
        Vec3d vec3d = (new Vec3d(x, y, z)).rotateY(yaw).add(pos.x, pos.y + entity.getStandingEyeHeight() + yOffset, pos.z);
        return new Vec3dWrapper(vec3d.x, vec3d.y, vec3d.z);
    }

    public Vec3d getVec3d() {
        return vec3d;
    }

    public double getX() {
        return vec3d.x;
    }

    public double getY() {
        return vec3d.y;
    }

    public double getZ() {
        return vec3d.z;
    }
}
