package org.huajiager.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * IExposedData 的默认实现。
 * 纯 POJO，不依赖 Minecraft/，Fabric 侧以 attachment 形式挂载。
 * 提供 Codec 用于 attachment 持久化（重进世界/重启服务器后替身数据不丢失）。
 */
public class ExposedData implements IExposedData {

    public static final String EMPTY_STAND = "empty";

    /** 持久化 Codec：dirty 为运行时瞬态量，不落盘 */
    public static final Codec<ExposedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stand").forGetter(ExposedData::getStand),
            Codec.BOOL.fieldOf("triggered").forGetter(ExposedData::isTriggered),
            Codec.BOOL.fieldOf("hand_display").forGetter(ExposedData::isHandDisplay),
            Codec.INT.fieldOf("stage").forGetter(ExposedData::getStage),
            Codec.STRING.fieldOf("state").forGetter(ExposedData::getState),
            Codec.STRING.fieldOf("model").forGetter(ExposedData::getModel)
    ).apply(instance, ExposedData::new));

    private String standName = EMPTY_STAND;
    private boolean isTriggered = false;
    private boolean isHandDisplay = true;
    private boolean dirty = false;
    private int stage = 0;
    private String state = States.DEFAULT.getName();
    private String modelID = EMPTY_STAND;

    public ExposedData() {
    }

    /** 全参构造器：仅用于 Codec 反序列化还原 */
    public ExposedData(String standName, boolean isTriggered, boolean isHandDisplay,
                       int stage, String state, String modelID) {
        this.standName = standName;
        this.isTriggered = isTriggered;
        this.isHandDisplay = isHandDisplay;
        this.stage = stage;
        this.state = state;
        this.modelID = modelID;
    }

    @Override
    public void setStand(String standName) {
        this.standName = standName;
        markDirty();
    }

    @Override
    public String getStand() {
        return this.standName;
    }

    @Override
    public void setTrigger(boolean trigger) {
        this.isTriggered = trigger;
        markDirty();
    }

    @Override
    public boolean isTriggered() {
        return isTriggered;
    }

    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isHandDisplay() {
        return isHandDisplay;
    }

    @Override
    public void setHandDisplay(boolean handDisplay) {
        isHandDisplay = handDisplay;
        markDirty();
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(String state) {
        this.state = state;
        markDirty();
    }

    @Override
    public String getModel() {
        return modelID;
    }

    @Override
    public void setModel(String model) {
        this.modelID = model;
        markDirty();
    }

    @Override
    public void setStage(int stage) {
        this.stage = stage;
        markDirty();
    }

    @Override
    public int getStage() {
        return stage;
    }

    /**
     * 替身状态机枚举。
     */
    public enum States {
        DEFAULT("default"),
        IDLE("idle"),
        PUNCH("punch"),
        OVERDRIVE("overdrive"),
        PROTECT("protect");

        private final String name;

        States(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
