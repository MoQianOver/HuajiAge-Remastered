package org.huajiager.capability;

/**
 * 替身玩家的外露附属数据。
 * 在 Fabric 侧通过 attachment 挂载到实体上，保存替身名 / 阶段 / 触发状态 / 显示状态 / 状态机 / 模型。
 */
public interface IExposedData {

    void setStand(String standName);

    String getStand();

    void setStage(int stage);

    int getStage();

    void setTrigger(boolean trigger);

    boolean isTriggered();

    void setHandDisplay(boolean display);

    boolean isHandDisplay();

    String getState();

    void setState(String state);

    String getModel();

    void setModel(String model);

    boolean isDirty();

    void setDirty(boolean dirty);
}
