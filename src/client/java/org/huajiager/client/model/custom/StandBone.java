package org.huajiager.client.model.custom;

import org.huajiager.client.render.model.HAModelPart;

/**
 * 暴露给骨骼动画脚本的骨骼包装：脚本按名字从 modelMap 取到它，读写旋转角/偏移/隐藏。
 *
 * <p>方法名与脚本里的调用一致（setRotateAngleX/Y/Z、getRotateAngleX/Z、setHidden 等），
 * 角度单位为弧度。</p>
 */
public class StandBone {

    private final HAModelPart part;

    public StandBone(HAModelPart part) {
        this.part = part;
    }

    public HAModelPart part() {
        return part;
    }

    public float getRotateAngleX() {
        return part.rotateAngleX;
    }

    public void setRotateAngleX(float angle) {
        part.rotateAngleX = angle;
    }

    public float getRotateAngleY() {
        return part.rotateAngleY;
    }

    public void setRotateAngleY(float angle) {
        part.rotateAngleY = angle;
    }

    public float getRotateAngleZ() {
        return part.rotateAngleZ;
    }

    public void setRotateAngleZ(float angle) {
        part.rotateAngleZ = angle;
    }

    public float getOffsetX() {
        return part.offsetX;
    }

    public void setOffsetX(float offset) {
        part.offsetX = offset;
    }

    public float getOffsetY() {
        return part.offsetY;
    }

    public void setOffsetY(float offset) {
        part.offsetY = offset;
    }

    public float getOffsetZ() {
        return part.offsetZ;
    }

    public void setOffsetZ(float offset) {
        part.offsetZ = offset;
    }

    public boolean isHidden() {
        return part.isHidden;
    }

    public void setHidden(boolean hidden) {
        part.setHidden(hidden);
    }
}
