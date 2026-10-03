package org.huajiager.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**。
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
        public boolean heroExplode = false;
        public boolean orgaSuit = true;
        public boolean useOrgaFlower = true;
        public double WaveHUDx = 0f;
        public double WaveHUDy = 0.2f;
        @ConfigEntry.BoundedDiscrete(min = 10, max = 21870)
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
