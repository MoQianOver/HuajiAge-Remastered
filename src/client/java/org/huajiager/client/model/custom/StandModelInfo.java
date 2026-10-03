package org.huajiager.client.model.custom;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * stand_model.json 里 model_list 的单项：某个 model_id 在某个状态下的模型信息。
 *
 * <p>同一个 model_id 可以按 state 分多条（不写 state 视作 default），因此查表 key 是
 * {@code model_id + "_" + state}，正好等于替身状态自己声明的 modelId
 * （加载器会把 JS 的 modelId 拼成 &lt;id&gt;_&lt;stateId&gt;）。</p>
 */
public class StandModelInfo {

    @SerializedName("model_id")
    public String modelId;

    @SerializedName("state")
    public String state;

    /** 模型相对替身实体的位移（格），用于把模型摆到正确高度/前后位置。 */
    @SerializedName("transfer")
    public List<Float> transfer;

    /** 模型绕 Y 轴的朝向修正（度）。 */
    @SerializedName("rotation")
    public List<Float> rotation;

    /** 第一人称下的位移（像素口径，随视角缩放使用）。 */
    @SerializedName("transfer_first")
    public List<Float> transferFirst;

    /** 第一人称下的朝向修正（度）。 */
    @SerializedName("rotation_first")
    public List<Float> rotationFirst;

    /** 第一人称朝向修正的系数。 */
    @SerializedName("rotation_factor_first")
    public Float rotationFactorFirst;

    /** 关闭模型随替身"悬浮"的上下浮动。 */
    @SerializedName("no_float")
    public Boolean noFloat;

    /** 关闭模型的自发光/满亮渲染。 */
    @SerializedName("no_light")
    public Boolean noLight;

    /** 模型整体缩放（作用于实体渲染矩阵）。 */
    @SerializedName("render_entity_scale")
    public Float renderEntityScale;

    /** 该模型使用的骨骼动画脚本（animation/ 目录下的文件名）。 */
    @SerializedName("animation")
    public List<String> animation;

    /** 自定义标签，供资源包按用途筛选。 */
    @SerializedName("tags")
    public List<String> tags;

    /** 查表 key：model_id + "_" + state（state 缺省为 default）。 */
    public String key() {
        String st = state == null || state.isBlank() ? "default" : state;
        return (modelId == null ? "" : modelId) + "_" + st;
    }
}
