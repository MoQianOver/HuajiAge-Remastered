package org.huajiager.stand.instance;

import org.huajiager.capability.ExposedData;
import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.states.default_set.StateKillerQueenDefault;
import org.huajiager.stand.states.various.StateKillerQueenPunch;
import org.huajiager.util.ServerUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

/**
 * Killer Queen 替身， 。
 *
 *  收尾状态：
 *  - 默认态 StateKillerQueenDefault 已挂载。 *  - 拳击态 StateKillerQueenPunch（various 重型档）已并挂载，doStandPower 的
 *    punch 分支已真实调用 rangePunchAttack。 *  - doStandCapability 的网络广播（MessageDoStandPowerClient → 客户端 doStandCapabilityClient
 *    播放 STAND_KILLER_QUEEN_TRIGGER 音效）已启用。 */
public class StandKillerQueen extends StandBase {

    public StandKillerQueen() {
        super();
    }

    public StandKillerQueen(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                            String texPath, String localName, boolean displayHand) {
        super(name, speed, damage, duration, distance, cost, charge, texPath, localName, displayHand);
        initState(new StateKillerQueenDefault(name, ExposedData.States.DEFAULT.getName(), isHandDisplay(), false));
        addState(ExposedData.States.PUNCH.getName(),
                new StateKillerQueenPunch(name, ExposedData.States.PUNCH.getName(), false, true));
    }

    @Override
    public void doStandPower(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        int stage = StandUtil.getStandStage(user);
        boolean isPunch = ExposedData.States.PUNCH.getName().equals(StandUtil.getStandState(user));
        if (type == null) {
            return;
        } else if (isPunch) {
            StandPowerHelper.rangePunchAttack(user, 45, getDamage() * (1 + stage / 2), 2);
        } else {
            StandPowerHelper.MPCharge(user, (int) (getCharge() / 3));
        }
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            World world = player.getWorld();
            EntitySheerHeartAttack attack = new EntitySheerHeartAttack(world);
            attack.setOwner(player);
            // 对齐 setTamedBy(player)：同时置 tamed=true 与 ownerId。
            // Fabric 的 setOwner 只写 ownerUuid，不置 tamed，会导致 RevengeGoal 的
            // "主人不打我"保护分支失效（isTamed()=false 时不排除主人复仇目标）。
            attack.setTamed(true);
            attack.setDamage(15f);
            // 狗式 AI 由 FollowOwnerGoal/MeleeAttackGoal 接管移动，不再给生成初速。
            // 残留 setVelocity 在贴墙召唤时会把车往方块里推一下，与"遁地"同源。
            // 生成点：不能用 player.getY()+0.5。 的方块碰撞会把"生成即与方块
            // 重叠"的实体主动挤出（collideWithBlock），而 1.20.1 的 VoxelShape 碰撞对已
            // 重叠实体不主动推出。玩家站在半砖/台阶/雪层/地毯等非整高地形时脚底 getY()
            // 不是方块顶，+0.5 的抬升量不足以让小车完全脱离方块碰撞箱 → 生成即嵌地
            // "遁地"。改为以 getBlockY()（脚底所在格）为基准抬 1 格，保证小车脚底
            // 严格在玩家所在格的方块顶之上，任何地形都绝不与地面方块重叠。
            attack.setPosition(player.getX(), player.getBlockY() + 1.0D, player.getZ());
            attack.pushOutOfBlocksSafe();
            world.spawnEntity(attack);
            // 实体生成后再挤一次：spawn 前 world 碰撞可能未生效，生成瞬间仍可能卡进方块
            attack.pushOutOfBlocksSafe();
        }
        if (user instanceof ServerPlayerEntity) {
            ServerUtil.sendPacketToNearbyPlayersStand(user,
                    new MessageDoStandPowerClient(user.getName().getString(), StandLoader.KILLER_QUEEN.getName()));
        }
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
        world.playSound(user.getX(), user.getY(), user.getZ(),
                SoundLoader.STAND_KILLER_QUEEN_TRIGGER, SoundCategory.PLAYERS, 2f, 1f, true);
    }
}
