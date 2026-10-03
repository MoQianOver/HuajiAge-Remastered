package org.huajiager.item;

import org.huajiager.compat.tlm.MaidBallHelper;
import org.huajiager.util.NBTHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 阴阳玉（带师球）：捕捉/放出女仆，潜行右击方块可把女仆转成女仆替身。
 *
 * <p>本类只做物品自身的数据层与三个入口的分支判断（键名与原版一致：
 * {@code model} / {@code data} / {@code owner} / {@code owner_name}），
 * 真正调用车万女仆 API 的行为在 {@code com.huajiager.compat.tlm.MaidBallHelper}。
 * 物品只在该模组存在时才注册，因此未安装时不会加载到它的类。</p>
 */
public class ItemYinYangBall extends Item {

    public ItemYinYangBall() {
        super(new Settings().maxCount(1));
    }

    /** 潜行右击方块转女仆替身，否则把球里的女仆放回方块上方。 */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.PASS;
        }
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        if (player.isSneaking()) {
            return MaidBallHelper.becomeMaidStand(player, stack, pos)
                    ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return MaidBallHelper.release(player, stack, pos)
                ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    /** 右击女仆本体：可抓（未驯服或属主是自己）就直接抓当前目标。 */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // 原版此处复用射线分支，但车万女仆的射线只认"属于自己"的女仆，
        // 会让"未驯服女仆也能抓"这条分支永远抓不到，因此改为直接抓当前目标
        if (entity instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid
                && MaidBallHelper.isCapturable(entity, user)
                && MaidBallHelper.capture(user, maid, stack)) {
            return ActionResult.SUCCESS;
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    /** 对着空气右击：射线找 8 格内属于该玩家的女仆并捕捉。 */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (captureByRay(user, stack)) {
            return TypedActionResult.success(stack);
        }
        return super.use(world, user, hand);
    }

    /** 原版在抓到女仆后是复用射线分支完成写入，这里保持一致。 */
    private boolean captureByRay(PlayerEntity player, ItemStack stack) {
        return MaidBallHelper.traceMaid(player)
                .map(maid -> MaidBallHelper.capture(player, maid, stack))
                .orElse(false);
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
