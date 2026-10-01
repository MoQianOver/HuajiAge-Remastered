package org.huajiager.stand.custom.script;

import java.util.Objects;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.stand.StandUtil;

import net.minecraft.entity.LivingEntity;

/**
 * 替身数据包装。
 * <p>供自定义替身 JS 脚本读取/修改替身名、阶段、状态、触发标记与精神力（MP）等数据。
 * 访问实体时经由 {@link StandUtil#getStandData(LivingEntity)} / getStandHandler 获取。
 * 对未觉醒替身的实体做了 null 兜底，避免脚本在任何时刻调用而崩溃。</p>
 */
public class StandDadaWrapper {

    private IExposedData data;
    private StandHandler chargeHandler;

    public StandDadaWrapper(IExposedData data, StandHandler chargeHandler) {
        this.data = data;
        this.chargeHandler = chargeHandler;
    }

    public StandDadaWrapper(LivingEntity livingBase) {
        this.data = StandUtil.getStandData(livingBase);
        this.chargeHandler = StandUtil.getStandHandler(livingBase);
    }

    public IExposedData getData() {
        return data;
    }

    public String getStandName() {
        if (data == null) {
            return ExposedData.EMPTY_STAND;
        }
        return data.getStand();
    }

    public int getStage() {
        if (data == null) {
            return 0;
        }
        return data.getStage();
    }

    public String getModel() {
        if (data == null) {
            return ExposedData.EMPTY_STAND;
        }
        return data.getModel();
    }

    public String getState() {
        if (data == null) {
            return ExposedData.States.DEFAULT.getName();
        }
        return data.getState();
    }

    public boolean isTriggered() {
        return data != null && data.isTriggered();
    }

    public int getMP() {
        return chargeHandler == null ? 0 : chargeHandler.getChargeValue();
    }

    public int getMaxMP() {
        return chargeHandler == null ? 0 : chargeHandler.getMaxValue();
    }

    public int getBuffer() {
        return chargeHandler == null ? 0 : chargeHandler.getBuffer();
    }

    public String getBufferTag() {
        return chargeHandler == null ? "empty" : Objects.requireNonNullElse(chargeHandler.getBuffTag(), "empty");
    }

    public void setMP(int mp) {
        if (chargeHandler != null) {
            chargeHandler.setChargeValue(mp);
        }
    }

    public void setMaxMP(int mp) {
        if (chargeHandler != null) {
            chargeHandler.setMaxValue(mp);
        }
    }

    public void setBuffer(int buffer) {
        if (chargeHandler != null) {
            chargeHandler.setBuffer(buffer);
        }
    }

    public void setBufferTag(String tag) {
        if (chargeHandler != null) {
            chargeHandler.setBuffTag(tag);
        }
    }
}
