package org.huajiager.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;

public final class ConfigScreen {

    private ConfigScreen() {
    }

    /** 注册 AutoConfig 配置类并将当前值同步回 ConfigHuaji。 */
    public static void register() {
        AutoConfig.register(ClothConfigData.class, GsonConfigSerializer::new);
        ConfigHolder<ClothConfigData> holder = AutoConfig.getConfigHolder(ClothConfigData.class);
        holder.registerLoadListener((manager, data) -> {
            syncToStatic(data);
            return ActionResult.SUCCESS;
        });
        holder.registerSaveListener((manager, data) -> {
            syncToStatic(data);
            return ActionResult.SUCCESS;
        });
        // 启动即同步一次（加载 config/huajiager.json，无文件则用默认值）
        syncToStatic(holder.getConfig());
    }

    /** 由 ModMenu 集成调用，返回 Cloth Config 配置界面。 */
    public static Screen createScreen(Screen parent) {
        return AutoConfig.getConfigScreen(ClothConfigData.class, parent).get();
    }

    /** 将 Cloth 配置值写回内置 ConfigHuaji 静态字段（业务代码统一读取点）。 */
    private static void syncToStatic(ClothConfigData data) {
        ConfigHuaji.Huaji.heroExplode = data.Huaji.heroExplode;
        ConfigHuaji.Huaji.orgaSuit = data.Huaji.orgaSuit;
        ConfigHuaji.Huaji.useOrgaFlower = data.Huaji.useOrgaFlower;
        ConfigHuaji.Huaji.WaveHUDx = data.Huaji.WaveHUDx;
        ConfigHuaji.Huaji.WaveHUDy = data.Huaji.WaveHUDy;
        ConfigHuaji.Huaji.point_star = data.Huaji.point_star;

        ConfigHuaji.Stands.roadRolerExplosion = data.Stands.roadRolerExplosion;
        ConfigHuaji.Stands.allowTimeStopPlayer = data.Stands.allowTimeStopPlayer;
        ConfigHuaji.Stands.delayTimeStop = data.Stands.delayTimeStop;
        ConfigHuaji.Stands.allowTheWorldDestory = data.Stands.allowTheWorldDestory;
        ConfigHuaji.Stands.allowCrazyDiamondBlock = data.Stands.allowCrazyDiamondBlock;
        ConfigHuaji.Stands.allowStandPunish = data.Stands.allowStandPunish;
        ConfigHuaji.Stands.allowStandGlow = data.Stands.allowStandGlow;
        ConfigHuaji.Stands.allowStandTip = data.Stands.allowStandTip;
        ConfigHuaji.Stands.allowStandLostTip = data.Stands.allowStandLostTip;
        ConfigHuaji.Stands.allowStandMovingSound = data.Stands.allowStandMovingSound;
        ConfigHuaji.Stands.allowStandSound = data.Stands.allowStandSound;
        ConfigHuaji.Stands.allowMaskTimeStop = data.Stands.allowMaskTimeStop;
        ConfigHuaji.Stands.useTimeStopNoiseMask = data.Stands.useTimeStopNoiseMask;
        ConfigHuaji.Stands.useHuajiSplash = data.Stands.useHuajiSplash;
        ConfigHuaji.Stands.knifeHeight = data.Stands.knifeHeight;
        ConfigHuaji.Stands.chanceStandFail = data.Stands.chanceStandFail;
        ConfigHuaji.Stands.arrowStand = data.Stands.arrowStand;
        ConfigHuaji.Stands.standHUDx = data.Stands.standHUDx;
        ConfigHuaji.Stands.standHUDy = data.Stands.standHUDy;
        ConfigHuaji.Stands.timeStopScale = data.Stands.timeStopScale;
        ConfigHuaji.Stands.timeStopEffect = data.Stands.timeStopEffect;
        ConfigHuaji.Stands.showTimeStopRemain = data.Stands.showTimeStopRemain;
    }
}
