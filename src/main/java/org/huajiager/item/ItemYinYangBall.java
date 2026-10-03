package org.huajiager.item;

import org.huajiager.util.NBTHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * 阴阳玉（带师球）：捕捉/放出女仆，潜行右击方块可把女仆转成女仆替身。
 *
 * <p>本类只负责物品自身的数据层（NBT 读写与状态判断），键名与原版一致：
 * {@code model} / {@code data} / {@code owner} / {@code owner_name}。
 * 捕捉、放出、转替身这些会调用车万女仆 API 的行为放在
 * {@code org.huajiager.compat.tlm} 下，保证未安装该模组时不触碰它的类。</p>
 */
public class ItemYinYangBall extends Item {

    public ItemYinYangBall() {
        super(new Settings().maxCount(1));
    }

    /** 球里是否装着一只女仆（模型与实体数据都在）。 */
    public boolean isBallFilled(ItemStack stack) {
        return isModelLoad(stack) && isDataLoad(stack);
    }

    public NbtCompound getMaidTag(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getCompound(NBT.MAID_DATA.getName());
    }

    public void setMaidData(ItemStack stack, String modelId, NbtCompound entityData, String ownerId) {
        NbtCompound data = NBTHelper.getTagCompoundSafe(stack);
        if (modelId != null) {
            data.putString(NBT.MAID_MODEL.getName(), modelId);
        }
        if (ownerId != null) {
            data.putString(NBT.MAID_OWNER.getName(), ownerId);
            data.putString(NBT.MAID_OWNER_NAME.getName(), "empty");
        }
        data.put(NBT.MAID_DATA.getName(), entityData);
        stack.setNbt(data);
    }

    public void setMaidData(ItemStack stack, String modelId, NbtCompound entityData, LivingEntity owner) {
        NbtCompound data = NBTHelper.getTagCompoundSafe(stack);
        if (modelId != null) {
            data.putString(NBT.MAID_MODEL.getName(), modelId);
        }
        if (owner != null) {
            data.putString(NBT.MAID_OWNER.getName(), owner.getUuid().toString());
            data.putString(NBT.MAID_OWNER_NAME.getName(), owner.getName().getString());
        }
        data.put(NBT.MAID_DATA.getName(), entityData);
        stack.setNbt(data);
    }

    public String getMaidOwner(ItemStack stack) {
        String uuid = NBTHelper.getTagCompoundSafe(stack).getString(NBT.MAID_OWNER.getName());
        if (!uuid.isEmpty()) {
            return uuid;
        }
        setMaidOwner(stack, "empty");
        return "empty";
    }

    public String getMaidOwnerName(ItemStack stack) {
        String name = NBTHelper.getTagCompoundSafe(stack).getString(NBT.MAID_OWNER_NAME.getName());
        if (!name.isEmpty()) {
            return name;
        }
        setMaidOwnerName(stack, "empty");
        return "empty";
    }

    public void setMaidOwnerName(ItemStack stack, String name) {
        NBTHelper.getTagCompoundSafe(stack).putString(NBT.MAID_OWNER_NAME.getName(), name);
    }

    public void setMaidOwner(ItemStack stack, String uuid) {
        NBTHelper.getTagCompoundSafe(stack).putString(NBT.MAID_OWNER.getName(), uuid);
    }

    public void setMaidModel(ItemStack stack, String model) {
        NBTHelper.getTagCompoundSafe(stack).putString(NBT.MAID_MODEL.getName(), model);
    }

    public String getMaidModel(ItemStack stack) {
        NbtCompound compound = NBTHelper.getTagCompoundSafe(stack);
        if (compound.contains(NBT.MAID_MODEL.getName())
                && !compound.getString(NBT.MAID_MODEL.getName()).isEmpty()) {
            return compound.getString(NBT.MAID_MODEL.getName());
        }
        setMaidModel(stack, "empty");
        return "empty";
    }

    public boolean isModelLoad(ItemStack stack) {
        return !"empty".equals(getMaidModel(stack));
    }

    public boolean isDataLoad(ItemStack stack) {
        return !getMaidTag(stack).isEmpty();
    }

    public boolean hasOwner(ItemStack stack) {
        return !"empty".equals(getMaidOwner(stack));
    }

    public boolean isOwner(ItemStack stack, LivingEntity entity) {
        return getMaidOwner(stack).equals(entity.getUuid().toString());
    }

    /** 清空球里的女仆（模型与实体数据都重置，owner 也回到 empty）。 */
    public void reset(ItemStack stack) {
        setMaidData(stack, "empty", new NbtCompound(), "empty");
    }

    /** 物品自身 NBT 键名（与原版同名同值）。 */
    public enum NBT {
        MAID_MODEL("model"),
        MAID_DATA("data"),
        MAID_OWNER_NAME("owner_name"),
        MAID_OWNER("owner");

        private final String name;

        NBT(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
