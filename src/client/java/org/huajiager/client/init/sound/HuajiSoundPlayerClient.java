package org.huajiager.client.init.sound;

import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * 客户端真实音频播放实现（client source set，独立编写）。
 *  实现 HuajiSoundPlayer 的客户端播放方法，
 * 经 {@link HuajiSoundPlayer#setClientSoundPlayer} 注入，供 main 源集物品在
 * world.isClient 分支触发 playMusic / stopAllSounds / playMovingSoundClient / playClient。
 *
 * 与差异（均记独立编写 / Fabric API 等价近似）：
 *  - playMusic： PositionedSoundRecord.getMusicRecord + soundHandler.playSound。 *    Fabric 等价 PositionedSoundInstance.music + SoundManager.play。 *  - stopMusic：持静态 currentMusic 再 soundHandler.stopSound；Fabric 同样持静态
 *    引用再 SoundManager.stop（Fabric SoundManager 按实例判等，new 新实例 stop 无效），
 *    因此这里按“停止最近一次 playMusic 的实例”语义实现。 *  - stopAllSounds： Minecraft.getMinecraft().getSoundHandler().stopSounds()。 *    Fabric 等价 SoundManager.stop()（无参 = 停止全部）。
 */
public class HuajiSoundPlayerClient implements HuajiSoundPlayer.IClientSoundPlayer {

    /** 当前播放的旋律实例（对齐静态 currentMusic，stopMusic 需同一实例引用）。 */
    private static SoundInstance currentMusic;

    public static final HuajiSoundPlayerClient INSTANCE = new HuajiSoundPlayerClient();

    /** BGM 循环播放实例：PositionedSoundInstance 默认 repeat=false，子类强制循环。     *  必须使用 2D 无衰减（AttenuationType.NONE + relative=true）跟随玩家——
     *  此前误用 8 参 3D 构造器（AttenuationType.LINEAR + 世界坐标 0,0,0），
     *  音效被播放在世界原点，玩家远离原点后因线性衰减完全听不见（其他替身音效走
     *  playClient 真实坐标/2D 正常，唯独 BGM 不响的根因）。 */
    private static final class LoopingMusicSound extends PositionedSoundInstance {
        LoopingMusicSound(SoundEvent sound, SoundCategory category) {
            super(sound.getId(), category, 1.0F, 1.0F,
                    net.minecraft.util.math.random.Random.create(),
                    true, 0, SoundInstance.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);
        }
    }

    /** BGM 单次播放实例（repeat=false）：镇魂曲替身释放技能的 BGM（orga_requiem_gold）
     *  按需求只播放一次，播完自然结束，不再循环。与 LoopingMusicSound 保持相同的
     *  2D 无衰减（AttenuationType.NONE + relative=true）跟随玩家语义。 */
    private static final class SingleShotMusicSound extends PositionedSoundInstance {
        SingleShotMusicSound(SoundEvent sound, SoundCategory category) {
            super(sound.getId(), category, 1.0F, 1.0F,
                    net.minecraft.util.math.random.Random.create(),
                    false, 0, SoundInstance.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);
        }
    }

    /** 替身飞行态循环音实例（playStandLoop 持有，stopStandLoop 需同一实例引用）。 */
    private static SoundInstance currentStandLoop;

    /** 替身飞行态循环音：PLAYERS 分类、repeat=true，2D 无衰减跟随玩家
     *  （修复 8 参 3D 构造器播放在世界原点听不见的问题）。 */
    private static final class LoopingStandSound extends PositionedSoundInstance {
        LoopingStandSound(SoundEvent sound) {
            super(sound.getId(), SoundCategory.PLAYERS, 1.0F, 1.0F,
                    net.minecraft.util.math.random.Random.create(),
                    true, 0, SoundInstance.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);
        }
    }

    @Override
    public void playMusic(SoundEvent sound) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        // 常规 BGM 需循环播放：PositionedSoundInstance.music(sound) 默认 repeat=false，
        // 短音效文件播一遍即停（实测 BGM 一秒就没了）；改用子类强制 repeat=true 循环。
        // 例外：镇魂曲替身释放技能的 BGM（orga_requiem_gold）按需求只播放一次
        // （repeat=false，播完自然结束，不再循环）。
        // 镇魂曲替身释放技能的 BGM（orga_requiem_gold）按需求改用 PLAYERS 分类
        // （跟随"玩家"音量滑条），其余旋律保持 MUSIC 分类（跟随"音乐"音量滑条）。
        SoundCategory category = sound == SoundLoader.ORGA_REQUIEM_GOLD
                ? SoundCategory.PLAYERS : SoundCategory.MUSIC;
        currentMusic = sound == SoundLoader.ORGA_REQUIEM_GOLD
                ? new SingleShotMusicSound(sound, category)
                : new LoopingMusicSound(sound, category);
        soundManager.play(currentMusic);
    }

    @Override
    public void stopMusic(SoundEvent sound) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        if (currentMusic != null) {
            soundManager.stop(currentMusic);
            currentMusic = null;
        }
    }

    @Override
    public void stopAllSounds() {
        // Fabric 1.20.1 SoundManager 无无参 stop()，以 stopSounds(null, null) 停止全部
        // （等价 Minecraft.getMinecraft().getSoundHandler().stopSounds()）。
        MinecraftClient.getInstance().getSoundManager().stopSounds((Identifier) null, (SoundCategory) null);
    }

    @Override
    public void playStandLoop(SoundEvent sound) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        // 重复触发前先停旧实例，避免状态重复同步时多个循环音叠加
        stopStandLoop();
        currentStandLoop = new LoopingStandSound(sound);
        soundManager.play(currentStandLoop);
    }

    @Override
    public void stopStandLoop() {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        if (currentStandLoop != null) {
            soundManager.stop(currentStandLoop);
            currentStandLoop = null;
        }
    }

    @Override
    public void playMovingSoundClient(LivingEntity target, SoundEvent sound, SoundCategory category, float volume) {
        MinecraftClient.getInstance().getSoundManager()
                .play(new HuajiMovingSound(target, sound, category).setVolume(volume));
    }

    @Override
    public void playLoopingMovingSoundClient(LivingEntity target, SoundEvent sound, SoundCategory category,
                                             float volume) {
        MinecraftClient.getInstance().getSoundManager()
                .play(new HuajiMovingSound(target, sound, category).setLoopVolume(volume));
    }

    @Override
    public void playClient(World world, double x, double y, double z, SoundEvent sound,
                           SoundCategory category, float volume, float pitch) {
        if (world instanceof ClientWorld clientWorld) {
            clientWorld.playSound(x, y, z, sound, category, volume, pitch, false);
        }
    }
}
