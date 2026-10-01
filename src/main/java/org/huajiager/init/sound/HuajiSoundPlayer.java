package org.huajiager.init.sound;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 声音播放助手（独立编写）。
 *
 * splitEnvironmentSourceSets 拆分设计：
 *  - 本类位于 main 源集，只允许引用服务端/公共 API，不得引用 MinecraftClient 等
 *    client-only 类；因此客户端真实播放实现按 Fabric 源集拆分下沉到
 *    src/client（org.huajiager.client.init.sound.HuajiSoundPlayerClient），这里仅保留：
 *      a) 服务端监测/调用入口（playToNearbyClient / playToClient，按维度广播）。 *      b) 客户端播放方法的委托接口 IClientSoundPlayer + 静态注入点
 *         setClientSoundPlayer，运行时由客户端初始化
 *         （HuajiAgeRemasteredClient.onInitializeClient）注册实现。 *  - main 源集物品（ItemOrgaRequiem / ItemSingularity 等）在 world.isClient 分支调用
 *    playMusic / stopAllSounds / playMovingSoundClient / playClient 时，实际播放会转发
 *    给客户端注册的实现；客户端实现未注册时安全空转（服务端/逻辑侧友好）。
 *
 * 方法归属说明：
 *  - playToNearbyClient / playToClient：经 MessagePlaySoundClient 网络包 + ServerUtil
 *    定向广播；Fabric 简化为服务端按维度广播（World.playSound(player=null)），利用 MC
 *    自带音量随距离衰减达到“附近”效果，行为等价。 *  - playToServer：经 MessagePlaySoundToServer 由客户端发服务端再循环广播（调用方尚未接入）。
 */
public class HuajiSoundPlayer {

    // ==================== 服务端广播链路（main / 可服务端调用） ====================

    /**
     * 播放声音给实体附近的客户端（服务端按维度广播，音量随距离衰减）。
     */
    public static void playToNearbyClient(LivingEntity entity, SoundEvent sound, float volume) {
        World world = entity.getWorld();
        if (world == null || world.isClient) {
            return;
        }
        Vec3d pos = entity.getPos();
        world.playSound(null, pos.x, pos.y, pos.z, sound, SoundCategory.PLAYERS, volume, 1.0f);
    }

    /**
     * 播放声音给同维度所有玩家（对齐 playToClient 的“发给同维度玩家”语义。     * Fabric 侧同样按维度广播，近距离完全可闻，远处随距离衰减）。
     */
    public static void playToClient(LivingEntity entity, SoundEvent sound, float volume) {
        World world = entity.getWorld();
        if (world == null || world.isClient) {
            return;
        }
        Vec3d pos = entity.getPos();
        world.playSound(null, pos.x, pos.y, pos.z, sound, SoundCategory.PLAYERS, volume, 1.0f);
    }

    // ==================== 客户端播放委托链路（common 入口 → client 实现） ====================

    /** 由 src/client 的 HuajiSoundPlayerClient 在客户端初始化时注入；服务端/逻辑侧为空。 */
    private static IClientSoundPlayer clientSoundPlayer;

    public static void setClientSoundPlayer(IClientSoundPlayer impl) {
        clientSoundPlayer = impl;
    }

    /**
     * 播放旋律级背景音乐（客户端专属；委托给 client 实现，此处不引任何 client-only 类）。
     * 对齐 playMusic：PositionedSoundRecord.getMusicRecord + soundHandler.playSound。
     */
    public static void playMusic(SoundEvent sound) {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.playMusic(sound);
        }
    }

    /**
     * 停止指定旋律（客户端专属；委托给 client 实现）。
     * 对齐 stopMusic：停止当前已记录的旋律实例。
     */
    public static void stopMusic(SoundEvent sound) {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.stopMusic(sound);
        }
    }

    /**
     * 停止全部音效（客户端专属；委托给 client 实现）。
     * 对齐 Minecraft.getMinecraft().getSoundHandler().stopSounds()。
     */
    public static void stopAllSounds() {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.stopAllSounds();
        }
    }

    /**
     * 播放随实体移动的循环音效（客户端专属；委托给 client 实现）。
     * 对齐 playMovingSoundClient(Entity, SoundEvent, SoundCategory, float)。
     */
    public static void playMovingSoundClient(LivingEntity target, SoundEvent sound, SoundCategory category, float volume) {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.playMovingSoundClient(target, sound, category, volume);
        }
    }

    /**
     * 播放替身飞行态循环音（客户端专属；委托给 client 实现）。
     * 镇魂曲替身状态机切进 "fly" 时播放鞘翅飞行循环声，切出飞行态/收回替身时
     * 由 stopStandLoop 停止；仅本机玩家自己的状态同步驱动。
     */
    public static void playStandLoop(SoundEvent sound) {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.playStandLoop(sound);
        }
    }

    /**
     * 停止替身飞行态循环音（客户端专属；委托给 client 实现，幂等）。
     */
    public static void stopStandLoop() {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.stopStandLoop();
        }
    }

    /**
     * 在指定世界坐标播放音效（客户端专属；委托给 client 实现）。
     * 对齐 playClient(World, x, y, z, SoundEvent, SoundCategory, float, float)。
     */
    public static void playClient(World world, double x, double y, double z,
                                  SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (clientSoundPlayer != null) {
            clientSoundPlayer.playClient(world, x, y, z, sound, category, volume, pitch);
        }
    }


    /**
     * 客户端播放委托接口（main 源集，仅引用服务端/公共类型，供 client 源集实现注入）。
     */
    public interface IClientSoundPlayer {
        void playMusic(SoundEvent sound);

        void stopMusic(SoundEvent sound);

        void stopAllSounds();

        void playMovingSoundClient(LivingEntity target, SoundEvent sound, SoundCategory category, float volume);

        void playStandLoop(SoundEvent sound);

        void stopStandLoop();

        void playClient(World world, double x, double y, double z, SoundEvent sound,
                        SoundCategory category, float volume, float pitch);
    }
}
