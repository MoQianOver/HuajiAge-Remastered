package org.huajiager.capability;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 替身能量（蓄力）处理器。
 * 纯 POJO，不依赖 Minecraft/；Fabric 侧以 attachment 形式挂载到玩家实体。
 *
 * 持久化范围：仅 charge（精神力/能量）与 max（上限）落盘，保证退出重进世界后
 * 精神力保留；buffer（时停剩余 tick）/ buffTag / recorder 均为运行时瞬态量，
 * 不序列化——时停中退出重进不应残留时停状态。
 */
public class StandHandler {

    /** 持久化 Codec：仅 charge 与 max 落盘，瞬态量不序列化 */
    public static final Codec<StandHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("charge").forGetter(StandHandler::getChargeValue),
            Codec.INT.fieldOf("max").forGetter(StandHandler::getMaxValue)
    ).apply(instance, StandHandler::new));

    private int charge = 0;
    private int max = 5000 * 20;
    private int buffer = 0;
    public String buffTag = "empty";
    public Map<String, Float> recorder = new HashMap<>();
    private boolean dirty;

    public StandHandler() {
    }

    /** 全参构造器：仅用于 Codec 反序列化还原 */
    public StandHandler(int charge, int max) {
        this.charge = charge;
        this.max = max;
    }

    public void setChargeValue(int value) {
        this.charge = value;
        markDirty();
    }

    public void setMaxValue(int value) {
        this.max = value;
        markDirty();
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
        markDirty();
    }

    public void setBuffTag(String buffTag) {
        this.buffTag = buffTag;
        markDirty();
    }

    public void setRecorder(Map<String, Float> recorder) {
        this.recorder = recorder;
        markDirty();
    }

    public int getChargeValue() {
        return charge;
    }

    public int getMaxValue() {
        return max;
    }

    public int getBuffer() {
        return buffer;
    }

    public String getBuffTag() {
        return buffTag;
    }

    public Map<String, Float> getRecorder() {
        return recorder;
    }

    public boolean canBeCharge() {
        return this.charge < max;
    }

    public boolean canBeCost(int cost) {
        return this.charge - cost >= 0;
    }

    public boolean toBeContinue() {
        return this.buffer > 0;
    }

    public void bufferDecreace() {
        if (toBeContinue()) {
            buffer--;
            markDirty();
        }
    }

    public void charge(int efficiency) {
        int result = charge + efficiency;
        if (canBeCharge()) {
            if (result <= max) {
                this.charge += efficiency;
            } else {
                setChargeValue(max);
            }
        } else {
            setChargeValue(max);
        }
        markDirty();
    }

    public void zero() {
        setChargeValue(0);
    }

    public void cost(int cost) {
        int orgin = charge;
        int left = orgin - cost;
        if (left > 0) {
            setChargeValue(left);
        } else {
            zero();
        }
    }

    public void addTarget(String uuid, float value) {
        this.recorder.put(uuid, value);
        markDirty();
    }

    public void removeTarget(String uuid) {
        this.recorder.remove(uuid);
        markDirty();
    }

    public void addTargetValue(String uuid, float valueEx) {
        if (recorder.containsKey(uuid)) {
            float value = this.recorder.get(uuid);
            this.recorder.put(uuid, value + valueEx);
        } else {
            addTarget(uuid, valueEx);
        }
        markDirty();
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
