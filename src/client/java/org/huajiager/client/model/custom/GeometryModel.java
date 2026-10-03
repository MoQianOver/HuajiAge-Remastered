package org.huajiager.client.model.custom;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * 基岩几何文件（assets/&lt;ns&gt;/models/entity/*.json）的解析结构。
 *
 * <p>兼容两种顶层写法：{@code "geometry.model"}（1.10 旧格式，本模组自带资源用的就是它）
 * 与 {@code "minecraft:geometry"}（数组形式、带 description 的新格式）。骨骼与方块的
 * 字段名两种格式一致，仅纹理尺寸字段名不同（旧的 texturewidth，新的 texture_width）。</p>
 */
public class GeometryModel {

    @SerializedName("format_version")
    public String formatVersion;

    @SerializedName("geometry.model")
    public Model legacyModel;

    @SerializedName("minecraft:geometry")
    public List<Model> geometryList;

    /** 取实际使用的几何体：旧格式优先，其次新格式的第一项。 */
    public Model activeModel() {
        if (legacyModel != null) {
            return legacyModel;
        }
        return geometryList == null || geometryList.isEmpty() ? null : geometryList.get(0);
    }

    public static class Model {

        @SerializedName("texturewidth")
        public Integer legacyTextureWidth;

        @SerializedName("textureheight")
        public Integer legacyTextureHeight;

        @SerializedName("description")
        public Description description;

        @SerializedName("bones")
        public List<BonesItem> bones;

        public int textureWidth() {
            if (legacyTextureWidth != null) {
                return legacyTextureWidth;
            }
            return description == null ? 64 : description.textureWidth;
        }

        public int textureHeight() {
            if (legacyTextureHeight != null) {
                return legacyTextureHeight;
            }
            return description == null ? 32 : description.textureHeight;
        }
    }

    /** 新格式的 description 块（纹理尺寸与可见包围盒）。 */
    public static class Description {

        @SerializedName("texture_width")
        public int textureWidth = 64;

        @SerializedName("texture_height")
        public int textureHeight = 32;
    }

    /** 单根骨骼：parent 构成挂载链，pivot 为旋转轴心，cubes 为该骨骼下的方块。 */
    public static class BonesItem {

        @SerializedName("name")
        public String name;

        @SerializedName("parent")
        public String parent;

        @SerializedName("pivot")
        public List<Float> pivot;

        @SerializedName("rotation")
        public List<Float> rotation;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("inflate")
        public Float inflate;

        @SerializedName("cubes")
        public List<CubesItem> cubes;
    }

    /** 单个方块：origin/size 为像素坐标与尺寸，uv 为箱子 UV 起点（本模组资源全部是箱子 UV）。 */
    public static class CubesItem {

        @SerializedName("origin")
        public List<Float> origin;

        @SerializedName("size")
        public List<Float> size;

        @SerializedName("uv")
        public List<Float> uv;

        @SerializedName("inflate")
        public Float inflate;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("rotation")
        public List<Float> rotation;

        @SerializedName("pivot")
        public List<Float> pivot;
    }
}
