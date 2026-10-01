package org.huajiager.stand.instance;

import java.util.Random;

import org.huajiager.capability.ExposedData;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.events.EventTimeStop;
import org.huajiager.stand.helper.TimeStopHelper;
import org.huajiager.stand.messages.MessageDioBreadTimeStop;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.states.default_set.StateTheWorldDefault;
import org.huajiager.stand.states.idle.StateTheWorldIdle;
import org.huajiager.util.ServerUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

/**
 * The World 替身， 。
 *
 *  收尾：默认态 StateTheWorldDefault（重型档）与闲置态已挂载。 * 时停主动技 TimeStopHelper 已迁并真实启用；时停网络广播
 * （ServerUtil.sendPacketToNearbyPlayersStand / MessageDoStandPowerClient）已启用。
 */
public class StandTheWorld extends StandBase {

    private static final Random RANDOM = new Random();

    public StandTheWorld() {
        super();
    }

    public StandTheWorld(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                         String texPath, String localName, boolean displayHand) {
        super(name, speed, damage, duration, distance, cost, charge, texPath, localName, displayHand);
        initState(new StateTheWorldDefault(name, ExposedData.States.DEFAULT.getName(), isHandDisplay(), true));
        addState(ExposedData.States.IDLE.getName(),
                new StateTheWorldIdle(name, ExposedData.States.IDLE.getName(), true, false));
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        int total = HuajiConstant.Tags.THE_WORLD_TIME + 20;
        TimeStopHelper.setEntityTimeStopRange(user, 120);
        // 注意：不再调 TimeStopHelper.extraEffects —— 其对非 STAR_PLATINUM 替身会即时施 5 药
        //（夜视/力量/速度/跳跃/再生 + 回血 5 点），表现同面包 5 药、且含夜视。
        // 技能时停保持纯粹冻结，不给任何状态效果。
        if (user instanceof ServerPlayerEntity sp) {
            // 时停触发对齐 Dio 面包链路："先响开场音、到点再时停"。
            // the_world 数字系列随机四选一，按各自音效时长换算延迟 tick（20 tick/s）。            // 音效经 S2C 单播由客户端本地播放（服务端世界广播到达时机/衰减不可靠），
            // 到点由 EventTimeStop 延迟队列触发时停（总时长 THE_WORLD_TIME+20，技能时停不给面包 5 药）。
            int idx = RANDOM.nextInt(4);
            SoundEvent sound;
            int delayTicks;
            switch (idx) {
                case 1 -> {
                    sound = SoundLoader.THE_WORLD_1;
                    delayTicks = Math.round(1.8f * 20);   // ~1.8s
                }
                case 2 -> {
                    sound = SoundLoader.THE_WORLD_2;
                    delayTicks = Math.round(3.8f * 20);   // ~3.8s
                }
                case 3 -> {
                    sound = SoundLoader.THE_WORLD_3;
                    delayTicks = Math.round(1.7f * 20);   // ~1.7s
                }
                default -> {
                    sound = SoundLoader.THE_WORLD;
                    delayTicks = Math.round(0.2f * 20);   // ~0.2s
                }
            }
            // 音效经 S2C 广播给附近玩家（64 格内，含发动者本人），客户端以发动者坐标为
            // 声源播放（服务端世界广播到达时机/衰减不可靠，故仍走 S2C + 客户端播放）
            ServerUtil.sendPacketToNearbyPlayersStand(user,
                    new MessageDioBreadTimeStop(sound.getId().toString(),
                            user.getX(), user.getY(), user.getZ()));
            EventTimeStop.scheduleDelayedTimeStop(sp, delayTicks, total, false);
            ServerUtil.sendPacketToNearbyPlayersStand(user,
                    new MessageDoStandPowerClient(user.getName().getString(), StandLoader.THE_WORLD.getName()));
        } else {
            // 非玩家发动者无可播放本地端，直接立即时停兜底
            TimeStopHelper.setTimeStop(user, total);
        }
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
        // 时停开场音已由 S2C 单播（MessageDioBreadTimeStop）在客户端本地播放，
        // 时停本身由服务端延迟队列（EventTimeStop）在音效到点后触发，客户端无需额外动作。
    }
}
