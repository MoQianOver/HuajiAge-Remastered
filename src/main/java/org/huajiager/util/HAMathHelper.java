package org.huajiager.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * 替身形体学/向量数学工具（HAMathHelper）。
 */
public class HAMathHelper {

    public static Vec3d getVectorEntity(Entity source, Entity target) {
        BlockPos eaterPos = source.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        return new Vec3d(targetPos.getX() - eaterPos.getX(),
                targetPos.getY() - eaterPos.getY(),
                targetPos.getZ() - eaterPos.getZ()).normalize();
    }

    public static Vec3d getVectorEntityEye(Entity source, Entity target) {
        Vec3d eaterPos = source.getEyePos();
        Vec3d targetPos = target.getEyePos();
        return new Vec3d(targetPos.x - eaterPos.x, targetPos.y - eaterPos.y, targetPos.z - eaterPos.z).normalize();
    }

    public static Vec3d getVector(Vec3d sourcePos, Vec3d targetPos) {
        return new Vec3d(targetPos.x - sourcePos.x, targetPos.y - sourcePos.y, targetPos.z - sourcePos.z).normalize();
    }

    public static double getDistance(Vec3d sourcePos, Vec3d targetPos) {
        return new Vec3d(targetPos.x - sourcePos.x, targetPos.y - sourcePos.y, targetPos.z - sourcePos.z).length();
    }

    public static double getDistance(BlockPos sourcePos, BlockPos targetPos) {
        return new Vec3d(targetPos.getX() - sourcePos.getX(),
                targetPos.getY() - sourcePos.getY(),
                targetPos.getZ() - sourcePos.getZ()).length();
    }

    public static float getAABBSize(Box box) {
        float a = MathHelper.abs((float) (box.maxX - box.minX));
        float b = MathHelper.abs((float) (box.maxY - box.minY));
        float c = MathHelper.abs((float) (box.maxZ - box.minZ));
        return a * b * c;
    }

    public static double getDegreeXZ(Vec3d v1, Vec3d v2) {
        Vec3d vec1 = v1.add(0, -v1.y, 0).normalize();
        Vec3d vec2 = v2.add(0, -v2.y, 0).normalize();
        double cos = (vec1.x * vec2.x + vec1.z * vec2.z) / (vec1.length() * vec2.length());
        return Math.round(Math.toDegrees(Math.acos(cos)));
    }

    public static double getDegreeXY(Vec3d v1, Vec3d v2) {
        Vec3d vec1 = v1.add(0, 0, -v1.z).normalize();
        Vec3d vec2 = v2.add(0, 0, -v2.z).normalize();
        double cos = (vec1.x * vec2.x + vec1.y * vec2.y) / (vec1.length() * vec2.length());
        return Math.round(Math.toDegrees(Math.acos(cos)));
    }

    public static double getDegreeZY(Vec3d v1, Vec3d v2) {
        Vec3d vec1 = v1.add(-v1.x, 0, 0).normalize();
        Vec3d vec2 = v2.add(-v2.x, 0, 0).normalize();
        double cos = (vec1.z * vec2.z + vec1.y * vec2.y) / (vec1.length() * vec2.length());
        return Math.round(Math.toDegrees(Math.acos(cos)));
    }

    public static Vec3d getVecPlus(Vec3d v1, Vec3d v2, double l1, double l2) {
        return new Vec3d(l1 * v1.x + l2 * v2.x, l1 * v1.y + l2 * v2.y, l1 * v1.z + l2 * v2.z);
    }

    public static Vec3d getVecPlus(Vec3d v1, Vec3d v2, Vec3d v3, double l1, double l2, double l3) {
        return new Vec3d(l1 * v1.x + l2 * v2.x + l3 * v3.x,
                l1 * v1.y + l2 * v2.y + l3 * v3.y,
                l1 * v1.z + l2 * v2.z + l3 * v3.z);
    }

    public static Vec3d getVecCross(Vec3d v1, Vec3d v2) {
        return new Vec3d(v1.y * v2.z - v1.z * v2.y,
                -v1.x * v2.z + v1.z * v2.x,
                v1.x * v2.y - v1.y * v2.x);
    }

    public static Vec3d getPostionRelative2D(Entity entity, float x, float z) {
        float yaw = entity.getYaw();
        if (entity instanceof LivingEntity) {
            yaw = ((LivingEntity) entity).getBodyYaw();
        }
        Vec3d forward = getVectorForRotation(0, yaw);
        Vec3d vertical = getVectorForRotation(0, yaw + 90);
        return getVecPlus(vertical, forward, x, z);
    }

    public static Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    public static class CommonMath {
        public static double sin(double d) {
            return MathHelper.sin((float) d);
        }

        public static double cos(double d) {
            return MathHelper.cos((float) d);
        }
    }
}
