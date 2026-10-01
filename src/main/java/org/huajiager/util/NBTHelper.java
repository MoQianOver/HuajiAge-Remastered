package org.huajiager.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.huajiager.attachment.Attachments;

import java.util.Map;

/**
 * 实体/物品 NBT 快捷读写（NBTHelper）。
 * 实体附加数据经 fabric-data-attachment-api 附着层读写，替代 getEntityData()。
 */
public class NBTHelper {

    // 实体附加数据调用
    public static int getEntityInteger(Entity entity, String key) {
        Object value = getEntityData(entity).get(key);
        return value instanceof Integer ? (Integer) value : 0;
    }

    public static boolean getEntityBoolean(Entity entity, String key) {
        Object value = getEntityData(entity).get(key);
        return value instanceof Boolean && (Boolean) value;
    }

    public static float getEntityFloat(Entity entity, String key) {
        Object value = getEntityData(entity).get(key);
        return value instanceof Number ? ((Number) value).floatValue() : 0F;
    }

    public static double getEntityDouble(Entity entity, String key) {
        Object value = getEntityData(entity).get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0D;
    }

    public static String getEntityString(Entity entity, String key) {
        Object value = getEntityData(entity).get(key);
        return value != null ? value.toString() : "";
    }

    public static void setEntityInteger(Entity entity, String key, int value) {
        getEntityData(entity).put(key, value);
    }

    public static void setEntityBoolean(Entity entity, String key, boolean value) {
        getEntityData(entity).put(key, value);
    }

    public static void setEntityFloat(Entity entity, String key, float value) {
        getEntityData(entity).put(key, value);
    }

    public static void setEntityDouble(Entity entity, String key, double value) {
        getEntityData(entity).put(key, value);
    }

    public static void setEntityString(Entity entity, String key, String value) {
        getEntityData(entity).put(key, value);
    }

    /** 读取白蛇心智剥夺标记（持久化 attachment，玩家重生后仍保留）。 */
    public static boolean getDiscDeprive(Entity entity) {
        return Boolean.TRUE.equals(entity.getAttached(Attachments.DISC_DEPRIVE));
    }

    /** 写入白蛇心智剥夺标记（持久化 attachment；false 时移除，避免向 NBT 落 false 冗余）。 */
    public static void setDiscDeprive(Entity entity, boolean value) {
        if (value) {
            entity.setAttached(Attachments.DISC_DEPRIVE, Boolean.TRUE);
        } else {
            entity.removeAttached(Attachments.DISC_DEPRIVE);
        }
    }

    private static Map<String, Object> getEntityData(Entity entity) {
        return entity.getAttachedOrCreate(Attachments.ENTITY_DATA);
    }

    // 物品 NBT 调用
    public static NbtCompound getTagCompoundSafe(ItemStack stack) {
        NbtCompound tagCompound = stack.getNbt();
        if (tagCompound == null) {
            tagCompound = new NbtCompound();
            stack.setNbt(tagCompound);
        }
        return tagCompound;
    }
}
