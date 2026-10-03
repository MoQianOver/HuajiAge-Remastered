package org.huajiager.compat.tlm;

import java.util.Optional;

import org.huajiager.capability.IExposedData;
import org.huajiager.item.ItemYinYangBall;
import org.huajiager.stand.StandUtil;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SpawnParticleMessage;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.github.tartaricacid.touhoulittlemaid.util.MaidRayTraceHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 阴阳玉与车万女仆交互的行为层（只在安装车万女仆时才会被加载）。
 *
 * <p>对应原版 ItemYinYangBall 的三条分支：射线捕捉、放出女仆、潜行转女仆替身。
 * 与 1.12.2 的差异按 TLMO 的 API 变化改写：NBT 读写改名、实体用 discard()、
 * 掉落用 util.ItemsUtil（原 ItemDropUtil 已删除）、服务端粒子必须发包。</p>
 */
public final class MaidBallHelper {

    /** 女仆替身的注册名（与内置 maid.json 一致）。 */
    public static final String MAID_STAND = "huajiager:maid";

    /** 手办柜物品的实体数据键名在 TLMO 里是 private，只能照抄字符串。 */
    private static final String ENTITY_INFO = "EntityInfo";

    private MaidBallHelper() {
    }

    /** 该实体是不是"可以抓的女仆"：存活 + 未驯服或属主是自己。 */
    public static boolean isCapturable(Entity target, PlayerEntity player) {
        return target instanceof EntityMaid maid && maid.isAlive()
                && (!maid.isTamed() || maid.isOwner(player));
    }

    /** 射线找 8 格内属于该玩家的女仆（原版 rayTraceMaid 签名未变）。 */
    public static Optional<EntityMaid> traceMaid(PlayerEntity player) {
        return MaidRayTraceHelper.rayTraceMaid(player, 8);
    }

    /** 把女仆写进球里：整只实体 NBT + 模型 id + 属主，然后移除该实体。 */
    public static boolean capture(PlayerEntity player, EntityMaid maid, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemYinYangBall ball) || !maid.isAlive()) {
            return false;
        }
        World world = player.getWorld();
        if (world.isClient) {
            return false;
        }
        NbtCompound data = new NbtCompound();
        maid.writeNbt(data);
        ball.setMaidData(stack, maid.getModelId(), data, player);
        explosionParticle(maid);
        maid.discard();
        return true;
    }

    /** 放出球里的女仆：按球里存的实体 NBT 还原并放到指定位置。 */
    public static boolean release(PlayerEntity player, ItemStack stack, BlockPos pos) {
        if (!(stack.getItem() instanceof ItemYinYangBall ball) || !ball.isBallFilled(stack)) {
            return false;
        }
        World world = player.getWorld();
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
            return false;
        }
        EntityMaid maid = new EntityMaid(serverWorld);
        maid.readNbt(ball.getMaidTag(stack));
        maid.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        explosionParticle(maid);
        serverWorld.spawnEntity(maid);
        ball.reset(stack);
        return true;
    }

    /**
     * 潜行右击：把球里的女仆转成"女仆替身"。
     * 与替身数据一并落到玩家身上后，女仆的背包物品与带数据的手办柜会掉在原地。
     */
    public static boolean becomeMaidStand(PlayerEntity player, ItemStack stack, BlockPos pos) {
        if (!(stack.getItem() instanceof ItemYinYangBall ball) || !ball.isBallFilled(stack)) {
            return false;
        }
        World world = player.getWorld();
        if (world.isClient) {
            return false;
        }
        IExposedData data = StandUtil.getStandData(player);
        if (data == null) {
            return false;
        }
        String modelId = ball.getMaidModel(stack);
        data.setStand(MAID_STAND);
        data.setStage(1);
        data.setModel(modelId + "_default");
        data.setTrigger(false);

        // 对齐原版：先按球里的数据临时还原一只女仆，把她的背包掉在原地再移除，
        // 这样转替身后女仆身上的东西不会凭空消失
        if (world instanceof ServerWorld serverWorld) {
            EntityMaid temp = new EntityMaid(serverWorld);
            temp.readNbt(ball.getMaidTag(stack));
            temp.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            dropMaidItems(temp);
            temp.discard();
        }

        // 带数据的手办柜：把球里那份实体数据原样塞进物品的 EntityInfo
        ItemStack kit = new ItemStack(InitItems.GARAGE_KIT);
        kit.getOrCreateNbt().put(ENTITY_INFO, ball.getMaidTag(stack).copy());
        ItemEntity kitEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, kit);
        kitEntity.setInvulnerable(true);
        world.spawnEntity(kitEntity);

        ball.reset(stack);
        return true;
    }

    /** 掉光女仆身上与背包里的物品（原 ItemDropUtil 的替代，服务端语义）。 */
    public static void dropMaidItems(EntityMaid maid) {
        ItemsUtil.dropEntityItems(maid, maid.getAllInv());
    }

    /** 服务端可用的爆炸粒子：原版 spawnExplosionParticle() 在服务端是空实现，必须发包。 */
    private static void explosionParticle(Entity entity) {
        if (entity.getWorld() instanceof ServerWorld) {
            NetworkHandler.sendToNearby(entity, SpawnParticleMessage.ID,
                    SpawnParticleMessage.encode(entity.getId(), SpawnParticleMessage.Type.EXPLOSION));
        }
    }
}
