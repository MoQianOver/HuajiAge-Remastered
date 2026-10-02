package org.huajiager.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Cloth Config 配置数据类。
 * 分组与默认值严格对齐原版 ConfigHuaji（Forge 1.12.2）：Huaji / Stands 两个分类。
 * 仅当已安装 cloth-config 时才由 {@link ConfigScreen#register()} 加载。
 */
@Config(name = "huajiager")
public class ClothConfigData implements ConfigData {

    @ConfigEntry.Category("huaji")
    @ConfigEntry.Gui.TransitiveObject
    public HuajiConfig Huaji = new HuajiConfig();

    @ConfigEntry.Category("stands")
    @ConfigEntry.Gui.TransitiveObject
    public StandConfig Stands = new StandConfig();

    public static class HuajiConfig {
        /** The Terrain destruction of the big explosion created by the big hero's bow */
        public boolean heroExplode = false;
        /** Dose The Infinite Charm Change mode when dress Orga suit? */
        public boolean orgaSuit = true;
        /** Use the music of orga flower? */
        public boolean useOrgaFlower = false;
        /** The X position of Wave Knife Using on HUD */
        public double WaveHUDx = 0f;
        /** The Y position of Wave Knife Using on HUD */
        public double WaveHUDy = 0.2f;
        /** The Points Needed for Making Universe Star */
        @ConfigEntry.BoundedDiscrete(min = 10, max = 21870)
        public int point_star = 81 * 9 * 3;
    }

    public static class StandConfig {
        /** The Terrain destruction of the big explosion created by road roller */
        public boolean roadRolerExplosion = true;
        /** Can THE WORLD stop the time of players? */
        public boolean allowTimeStopPlayer = true;
        /** Can THE WORLD Destory blocks? */
        public boolean allowTheWorldDestory = true;
        /** Can CRAZY DIAMOND Change the structure of blocks? */
        public boolean allowCrazyDiamondBlock = true;
        /** To punish the stand users who out of time */
        public boolean allowStandPunish = false;
        /** To glow the stand users who idle its stand */
        public boolean allowStandGlow = false;
        /** Need some tips to notice you how to use STANDs? */
        public boolean allowStandTip = true;
        /** Need some tips to notice you when you lost STANDs? */
        public boolean allowStandLostTip = false;
        /** Need the moving sound for you stand? */
        public boolean allowStandMovingSound = true;
        /** Need the sound playing for you stand? */
        public boolean allowStandSound = true;
        /** Need the mask of time stop? */
        public boolean allowMaskTimeStop = true;
        /** Use the noise version mask of time stop? */
        public boolean useTimeStopNoiseMask = true;
        /** Use the HUAJI splash replace the image of emerald splash? */
        public boolean useHuajiSplash = false;
        /** The flight height of the multi knife */
        public double knifeHeight = -0.25f;
        /** The chance for weak up stand fail */
        public double chanceStandFail = 0.3;
        /** The X position of Stand on HUD */
        public double standHUDx = 0f;
        /** The Y position of Stand on HUD */
        public double standHUDy = 0.64f;
        /** The scale of effect icon of time stop */
        public double timeStopScale = 1.0;
        /** The time of invert effect in time stop */
        public double timeStopEffect = 1.5;
    }
}
