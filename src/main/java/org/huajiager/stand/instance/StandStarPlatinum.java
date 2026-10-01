package org.huajiager.stand.instance;

import org.huajiager.capability.ExposedData;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.stand.helper.TimeStopHelper;
import org.huajiager.stand.messages.MessageDioBreadTimeStop;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.states.default_set.StateStarPlatinumDefault;
import org.huajiager.stand.states.idle.StateStarPlatinumIdle;
import org.huajiager.util.ServerUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Star Platinum 替身。
 *
 *  收尾：默认态 StateStarPlatinumDefault（重型档）与闲置态已挂载。 * 时停主动技 TimeStopHelper 已迁并真实启用；时停网络广播
 * （ServerUtil.sendPacketToNearbyPlayersStand / MessageDoStandPowerClient）已启用。
 */
public class StandStarPlatinum extends StandBase {

    public StandStarPlatinum() {
        super();
    }

    public StandStarPlatinum(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                             String texPath, String localName, boolean displayHand) {
        super(name, speed, damage, duration, distance, cost, charge, texPath, localName, displayHand);
        initState(new StateStarPlatinumDefault(name, ExposedData.States.DEFAULT.getName(), isHandDisplay(), true));
        addState(ExposedData.States.IDLE.getName(),
                new StateStarPlatinumIdle(name, ExposedData.States.IDLE.getName(), true, false));
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        int total = 5 * 20 + 20;
        TimeStopHelper.setEntityTimeStopRange(user, 120);
        // 技能时停不给药水效果（对齐世界：extraEffects 只用于面包链路给药）。
        if (user instanceof ServerPlayerEntity sp) {
            // 开场音随机二选一：
            //  star_platinum_the_world_1：延迟 2 秒，到点再时停。            //  star_platinum_the_world_2（~2.11s）：立即时停。
            // 音效均经 S2C 单播由客户端本地播放（服务端世界广播到达时机/衰减不可靠）。
            boolean useLong = Math.random() < 0.5;
            // 音效经 S2C 广播给附近玩家（64 格内，含发动者本人），客户端以发动者坐标为
            // 声源播放（服务端世界广播到达时机/衰减不可靠，故仍走 S2C + 客户端播放）
            ServerUtil.sendPacketToNearbyPlayersStand(user,
                    new MessageDioBreadTimeStop(
                            (useLong ? SoundLoader.STAR_PLATINUM_THE_WORLD_1 : SoundLoader.STAR_PLATINUM_THE_WORLD_2).getId().toString(),
                            user.getX(), user.getY(), user.getZ()));
            int delayTicks = useLong ? 2 * 20 : 0;
            EventTimeStop.scheduleDelayedTimeStop(sp, delayTicks, total, false);
        } else {
            TimeStopHelper.setTimeStop(user, total);
        }
        ServerUtil.sendPacketToNearbyPlayersStand(user,
                new MessageDoStandPowerClient(user.getName().getString(), StandLoader.STAR_PLATINUM.getName()));
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
        // 时停开场音已由 S2C 单播（MessageDioBreadTimeStop）在客户端本地播放，
        // 时停本身由服务端延迟队列（EventTimeStop）在音效到点后触发，客户端无需额外动作。
    }
}
