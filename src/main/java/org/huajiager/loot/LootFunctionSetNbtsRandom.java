package org.huajiager.loot;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.HuajiAgeRemastered;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

/**
 * 掉落物 NBT 随机化函数（注册名 huajiager:set_nbts_random）。
 *
 * <p>JSON 里声明 number 与 tag1..tagN，每份 tagN 是一段 SNBT；结算时随机取一份合并进
 * 掉落物已有的 NBT（物品没有 NBT 时直接采用），用于让宝箱开出的替身 disc 随机带一个
 * StandId。StandId 里用 '-' 代替命名空间分隔符，读取时替换成 ':'，与物品自身存储口径一致。</p>
 */
public class LootFunctionSetNbtsRandom implements LootFunction {

    private final List<String> tags;

    private LootFunctionSetNbtsRandom(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public LootFunctionType getType() {
        return LootLoader.SET_NBTS_RANDOM;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (tags.isEmpty()) {
            return stack;
        }
        NbtCompound parsed = parseSnbt(tags.get(context.getRandom().nextInt(tags.size())));
        if (parsed == null) {
            return stack;
        }
        if (parsed.contains("StandId")) {
            String id = parsed.getString("StandId");
            if (id.contains("-")) {
                parsed.putString("StandId", id.replace("-", ":"));
            }
        }
        NbtCompound target = stack.getOrCreateNbt();
        for (String key : parsed.getKeys()) {
            target.put(key, parsed.get(key));
        }
        return stack;
    }

    private static NbtCompound parseSnbt(String snbt) {
        try {
            return StringNbtReader.parse(snbt);
        } catch (Exception e) {
            // 单条 SNBT 写坏只跳过该条，不影响其余战利品结算与整张表加载
            HuajiAgeRemastered.LOGGER.error("[HuajiAge] Invalid SNBT in set_nbts_random: {}", snbt, e);
            return null;
        }
    }

    /** Gson 序列化器：number 为条数，键名固定为 tag1..tagN。 */
    public static class Serializer implements JsonSerializer<LootFunctionSetNbtsRandom> {

        @Override
        public void toJson(JsonObject json, LootFunctionSetNbtsRandom object, JsonSerializationContext context) {
            json.addProperty("number", object.tags.size());
            for (int i = 0; i < object.tags.size(); i++) {
                json.addProperty("tag" + (i + 1), object.tags.get(i));
            }
        }

        @Override
        public LootFunctionSetNbtsRandom fromJson(JsonObject json, JsonDeserializationContext context) {
            int size = Math.max(1, JsonHelper.getInt(json, "number", 1));
            List<String> tags = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                tags.add(JsonHelper.getString(json, "tag" + (i + 1)));
            }
            return new LootFunctionSetNbtsRandom(tags);
        }
    }
}
