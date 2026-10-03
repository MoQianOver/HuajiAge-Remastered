package org.huajiager.client.model.custom;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * stand_model.json 的解析结构：一个资源包用它声明"哪些 model_id 用哪个几何文件"。
 *
 * <p>文件放在 assets/&lt;ns&gt;/stand_model.json，多个资源包的声明会合并；
 * 几何文件按约定从同一命名空间取 models/entity/&lt;model_id 的 path&gt;.json，
 * 贴图取 textures/entity/&lt;同一 path&gt;.png。</p>
 */
public class CustomModelPack {

    @SerializedName("pack_name")
    public String packName;

    @SerializedName("author")
    public List<String> author;

    @SerializedName("description")
    public List<String> description;

    @SerializedName("date")
    public String date;

    @SerializedName("version")
    public String version;

    @SerializedName("model_list")
    public List<StandModelInfo> modelList;
}
