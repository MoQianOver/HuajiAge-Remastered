package org.huajiager.stand.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.huajiager.HuajiAgeRemastered;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import net.minecraft.util.Identifier;

/**
 * 自定义替身 JSON 信息结构。
 * <p>对应 custom_stand/*.json 的 POJO：替身注册名 / 显示名（本地化 key）/ 状态列表 /
 * disc id / 标签 / 阶段数 / 属性数组 / 音效等。字段用 {@code @SerializedName} 绑定
 * JSON 键（stand_tags / sounds_repeat 这类 snake_case 键必须显式绑定，否则读不进来）。
 * GSON 反序列化后调用 {@link #decorate()} 填充缺失字段默认值。</p>
 */
public class StandCustomInfo {

    /** 替身注册名（如 huajiager:crazy_diamond） */
    @SerializedName("stand")
    private String stand;
    /** 显示名本地化 key */
    @SerializedName("name")
    private String name;
    /** 状态集合（映射 custom_stand/states/<stand>_<state>.js） */
    @SerializedName("states")
    private List<String> states;
    /** 对应 disc id */
    @SerializedName("disc")
    private String disc;
    /** 标签（arrow / dark_core 等，供成就或创造标签页过滤） */
    @SerializedName("stand_tags")
    private List<String> standTags;
    /** 阶段总数 */
    @SerializedName("stages")
    private int stages;
    /** 属性数组：速度 / 伤害 / 持续时间 / 距离 / 冷却 / 充能 / 最大精神力 */
    @SerializedName("attributes")
    private List<Number> attributes;
    /** 音效池 */
    @SerializedName("sounds")
    private List<String> sounds;
    /** 重复音效 */
    @SerializedName("sounds_repeat")
    private List<String> soundsRepeat;
    /** 是否受重力 */
    @SerializedName("gravity")
    private boolean gravity;
    /** 作者 */
    @SerializedName("author")
    private String author;

    public StandCustomInfo() {
    }

    public String getStand() {
        return stand;
    }

    public String getName() {
        return name;
    }

    public List<String> getStates() {
        return states;
    }

    public String getDisc() {
        return disc;
    }

    public List<String> getStandTags() {
        return standTags;
    }

    public int getStages() {
        return stages;
    }

    public List<Float> getAttributes() {
        if (attributes == null) {
            return Collections.emptyList();
        }
        List<Float> result = new ArrayList<>(attributes.size());
        for (Number n : attributes) {
            result.add(n.floatValue());
        }
        return result;
    }

    public List<String> getSounds() {
        return sounds;
    }

    public List<String> getSoundsRepeat() {
        return soundsRepeat;
    }

    public boolean isGravity() {
        return gravity;
    }

    public String getAuthor() {
        return author;
    }

    /** 填充缺失字段的默认值。 */
    public StandCustomInfo decorate() {
        if (stand == null || stand.isEmpty()) {
            throw new JsonSyntaxException("Custom stand file needs a stand name");
        }
        if (name == null || name.isEmpty()) {
            name = "stand." + stand.replace(':', '.') + ".name";
        }
        if (states == null || states.isEmpty()) {
            states = Lists.newArrayList("default");
        }
        if (!states.contains("default")) {
            states.add("default");
        }
        if (disc == null || disc.isEmpty()) {
            Identifier id = Identifier.tryParse(stand);
            String path = id != null ? id.getPath() : stand;
            disc = HuajiAgeRemastered.MOD_ID + ":disc/disc_" + HuajiAgeRemastered.MOD_ID + "_" + path;
        }
        if (standTags == null) {
            standTags = Lists.newArrayList();
        }
        if (stages <= 0) {
            stages = 1;
        }
        if (attributes == null || attributes.isEmpty()) {
            attributes = Lists.newArrayList(1.2f, 10f, 200f, 2f, 60000, 75, 100000);
        } else if (attributes.size() < 7) {
            // 少于 7 项时未提供的项会保持 0，机器不可用；此处只提示，不代为补默认值
            HuajiAgeRemastered.LOGGER.warn(
                    "[HuajiAge] Custom stand {} declares only {} attributes, the missing ones stay 0",
                    stand, attributes.size());
        }
        if (sounds == null) {
            sounds = Lists.newArrayList();
        }
        if (soundsRepeat == null) {
            soundsRepeat = Lists.newArrayList();
        }
        if (author == null) {
            author = "";
        }
        return this;
    }
}
