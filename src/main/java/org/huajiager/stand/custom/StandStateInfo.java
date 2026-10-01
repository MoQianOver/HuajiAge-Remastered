package org.huajiager.stand.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义替身状态信息结构（ ）。
 * <p>由 custom_stand/states/<stand>_<state>.js 脚本 eval 产出的 JS 对象映射而来，
 * 描述单个状态下：stateId / 名称 / stage / modelId / 额外标签 / 是否重复音效 / 是否手持播放。</p>
 */
public class StandStateInfo {

    private String stand;
    private String stateId;
    private String name;
    private int stage;
    private String modelId;
    private List<String> stateTags;
    private boolean soundRepeat;
    private boolean hand;
    /** eval JS 后得到的脚本对象（调用 update/timeOut/capability 用），不参与序列化 */
    private transient Object stateObject;

    public StandStateInfo() {
    }

    public String getStand() {
        return stand;
    }

    public void setStand(String stand) {
        this.stand = stand;
    }

    public Object getStateObject() {
        return stateObject;
    }

    public void setStateObject(Object stateObject) {
        this.stateObject = stateObject;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public List<String> getStateTags() {
        return stateTags == null ? new ArrayList<>() : stateTags;
    }

    public void setStateTags(List<String> stateTags) {
        this.stateTags = stateTags;
    }

    public boolean isSoundRepeat() {
        return soundRepeat;
    }

    public void setSoundRepeat(boolean soundRepeat) {
        this.soundRepeat = soundRepeat;
    }

    public boolean isHand() {
        return hand;
    }

    public void setHand(boolean hand) {
        this.hand = hand;
    }
}
