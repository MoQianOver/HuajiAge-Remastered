package org.huajiager.config;

/**
 * 配置类，
 * 纯 POJO 字段结构。
 */
public class ConfigHuaji {

    public static HuajiConfig Huaji = new HuajiConfig();
    public static StandConfig Stands = new StandConfig();

    public static class HuajiConfig {
        public boolean heroExplode = false;
        public boolean orgaSuit = true;
        public boolean useOrgaFlower = false;
        public double WaveHUDx = 0f;
        public double WaveHUDy = 0.2f;
        public int point_star = 81 * 9 * 3;
    }

    public static class StandConfig {
        public boolean roadRolerExplosion = true;
        public boolean allowTimeStopPlayer = true;
        public boolean delayTimeStop = true;
        public boolean allowTheWorldDestory = true;
        public boolean allowCrazyDiamondBlock = true;
        public boolean allowStandPunish = false;
        public boolean allowStandGlow = false;
        public boolean allowStandTip = true;
        public boolean allowStandLostTip = false;
        public boolean allowStandMovingSound = true;
        public boolean allowStandSound = true;
        public boolean allowMaskTimeStop = true;
        public boolean useTimeStopNoiseMask = true;
        public boolean useHuajiSplash = false;
        public double knifeHeight = -0.25f;
        public double chanceStandFail = 0.3;
        public double standHUDx = 0f;
        public double standHUDy = 0.64f;
        public double timeStopScale = 1.0;
        public double timeStopEffect = 1.5;
    }
}
