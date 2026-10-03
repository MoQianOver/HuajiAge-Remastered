package org.huajiager.client.model.custom;

import org.huajiager.client.render.model.HAModelPart;

import net.minecraft.client.model.ModelPart;

/**
 * 暴露给骨骼动画脚本的骨骼包装，脚本按名字从 modelMap 取到它，读写旋转角/偏移/隐藏。
 *
 * <p>方法名与脚本里的调用一致（setRotateAngleX/Y/Z、getRotateAngleX/Z、setHidden 等），
 * 角度单位为弧度。可包装两类骨骼：基岩几何构建的 {@link HAModelPart}，以及借用的原版
 * 实体模型 {@link ModelPart}（原版没有"偏移"概念，偏移读写对它为空操作）。</p>
 */
public class StandBone {

    private final HAModelPart haPart;
    private final ModelPart vanillaPart;

    public StandBone(HAModelPart part) {
        this.haPart = part;
        this.vanillaPart = null;
    }

    public StandBone(ModelPart part) {
        this.haPart = null;
        this.vanillaPart = part;
    }

    public float getRotateAngleX() {
        return haPart != null ? haPart.rotateAngleX : vanillaPart.pitch;
    }

    public void setRotateAngleX(float angle) {
        if (haPart != null) {
            haPart.rotateAngleX = angle;
        } else {
            vanillaPart.pitch = angle;
        }
    }

    public float getRotateAngleY() {
        return haPart != null ? haPart.rotateAngleY : vanillaPart.yaw;
    }

    public void setRotateAngleY(float angle) {
        if (haPart != null) {
            haPart.rotateAngleY = angle;
        } else {
            vanillaPart.yaw = angle;
        }
    }

    public float getRotateAngleZ() {
        return haPart != null ? haPart.rotateAngleZ : vanillaPart.roll;
    }

    public void setRotateAngleZ(float angle) {
        if (haPart != null) {
            haPart.rotateAngleZ = angle;
        } else {
            vanillaPart.roll = angle;
        }
    }

    public float getOffsetX() {
        return haPart != null ? haPart.offsetX : 0f;
    }

    public void setOffsetX(float offset) {
        if (haPart != null) {
            haPart.offsetX = offset;
        }
    }

    public float getOffsetY() {
        return haPart != null ? haPart.offsetY : 0f;
    }

    public void setOffsetY(float offset) {
        if (haPart != null) {
            haPart.offsetY = offset;
        }
    }

    public float getOffsetZ() {
        return haPart != null ? haPart.offsetZ : 0f;
    }

    public void setOffsetZ(float offset) {
        if (haPart != null) {
            haPart.offsetZ = offset;
        }
    }

    public boolean isHidden() {
        return haPart != null ? haPart.isHidden : !vanillaPart.visible;
    }

    public void setHidden(boolean hidden) {
        if (haPart != null) {
            haPart.setHidden(hidden);
        } else {
            vanillaPart.visible = !hidden;
        }
    }
}
