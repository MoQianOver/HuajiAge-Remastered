package org.huajiager.stand.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.huajiager.HuajiAgeRemastered;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.Identifier;

/**
 * 自定义替身 JSON 信息结构。
 * <p>对应 custom_stand/*.json 的 POJO：替身注册名 / 显示名（本地化 key）/ 状态列表 /
 * 唱片 id / 标签 / 阶段数 / 属性数组 / 音效等。GSON 反序列化后调用 {@link #decorate()}
 * 填充缺失字段默认值。</p>
 */
public class StandCustomInfo {

    /** 替身注册名（如 huajiager:crazy_diamond） */
    private String stand;
    /** 显示名本地化 key */
    private String name;
    /** 状态集合（映射 custom_stand/states/<stand>_<state>.js） */
    private List<String> states;
    /** 对应唱片 id */
    private String disc;
    /** 标签（arrow / dark_core 等，供成就或创造标签页过滤） */
    private List<String> standTags;
    /** 阶段总数 */
    private int stages;
    /** 属性数组：速度 / 伤害 / 持续时间 / 距离 / 冷却 / 充能 / 最大精神力 */
    private List<Number> attributes;
    /** 音效池 */
    private List<String> sounds;
    /** 重复音效 */
    private List<String> soundsRepeat;
    /** 是否受重力 */
    private boolean gravity;
    /** 作者 */
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
