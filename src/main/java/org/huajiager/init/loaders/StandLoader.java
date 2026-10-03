package org.huajiager.init.loaders;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.api.HuajiAgeAPI;
import org.huajiager.api.IStand;
import org.huajiager.init.HuajiConstant;
import org.huajiager.stand.StandResourceLoader;
import org.huajiager.stand.custom.StandCustom;
import org.huajiager.stand.custom.StandCustomInfo;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.instance.StandHierophantGreen;
import org.huajiager.stand.instance.StandKillerQueen;
import org.huajiager.stand.instance.StandOrgaRequiem;
import org.huajiager.stand.instance.StandStarPlatinum;
import org.huajiager.stand.instance.StandTheWorld;

/**
 * 替身注册器。
 *
 * 注册链路：构造时登记 5 个原生替身（The World / Star Platinum /
 * Hierophant Green / Orga Requiem / Killer Queen）至 {@link #STAND_LIST}，{@link #reloadStands()}
 * 调用 HuajiAgeAPI.standClear / statesClear 后逐个 {@code registerStand} + {@code putInternalStandStates()}。
 * - 自定义替身（由 StandResourceLoader + StandCustom 加载）：reloadStands() 内
 *   loadCustomStand() 加载 3 个内置 JSON + 7 个 state JS，并一一注册进 HuajiAgeAPI 与 STAND_LIST。
 */
public class StandLoader {

    public static final List<StandBase> STAND_LIST = new ArrayList<>();
    public static final String EMPTY = "empty";

    public static final StandTheWorld THE_WORLD = new StandTheWorld("the_world", 1.2f, 10f, 200, 2f, 60000, 75,
            "textures/entity/entity_the_world_default.png", HuajiConstant.StandType.STAND_THE_WORLD, false);
    public static final StandStarPlatinum STAR_PLATINUM = new StandStarPlatinum("star_platinum", 1.5f, 15f, 275, 2f, 50000, 95,
            "textures/entity/entity_star_platinum_default.png", HuajiConstant.StandType.STAND_STAR_PLATINUM, false);
    public static final StandHierophantGreen HIEROPHANT_GREEN = new StandHierophantGreen("hierophant_green", 1.0f, 5f, 200, 20f, 70000, 80,
            "textures/entity/entity_hierophant_green_default.png", HuajiConstant.StandType.STAND_HIEROPANT_GREEN, true);
    public static final StandOrgaRequiem ORGA_REQUIEM = new StandOrgaRequiem("orga_requiem", 0.04f, 0f, 650, 50f, 80000, 70,
            "textures/entity/entity_orga_requiem_default.png", HuajiConstant.StandType.STAND_ORGA_REQUIEM, true);
    public static final StandKillerQueen KILLER_QUEEN = new StandKillerQueen("killer_queen", 0.6f, 8f, 300, 100f, 60000, 80,
            "textures/entity/entity_killer_queen_default.png", HuajiConstant.StandType.STAND_KILLER_QUEEN, true);

    public StandLoader() {
        registerStand(THE_WORLD);
        registerStand(STAR_PLATINUM);
        registerStand(HIEROPHANT_GREEN);
        registerStand(ORGA_REQUIEM);
        registerStand(KILLER_QUEEN);
        reloadStands();
    }

    public static void reloadStands() {
        HuajiAgeAPI.standClear();
        HuajiAgeAPI.statesClear();

        // 先摘除上一轮登记的自定义替身（STAND_LIST 原生 5 站不动），再加载重建，
        // 避免 reloadStands() 多次调用导致 STAND_LIST 重复累计
        STAND_LIST.removeIf(StandCustom.class::isInstance);

        // 自定义替身资源加载（4 JSON + 8 state JS，StandResourceLoader 内 eval + 注册 state 表）
        StandResourceLoader.loadCustomStand();

        for (StandBase stand : STAND_LIST) {
            HuajiAgeAPI.registerStand(stand);
            stand.putInternalStandStates();
        }

        // 自定义替身：StandCustom.addState 内部已把各状态 registerStandState 进 API 状态表，
        // 此处仅注册替身本体（HuajiAgeAPI + STAND_LIST，创造标签页遍历可见）
        for (StandCustomInfo info : StandResourceLoader.CUSTOM_STAND_SERVER.values()) {
            StandCustom custom = new StandCustom(info);
            HuajiAgeAPI.registerStand(custom);
            STAND_LIST.add(custom);
        }
    }

    private static void registerStand(StandBase stand) {
        STAND_LIST.add(stand);
    }

    public static StandBase getStandByIndex(int index) {
        List<IStand> list = HuajiAgeAPI.getStandList();
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        IStand stand = list.get(index);
        if (stand instanceof StandBase) {
            return (StandBase) stand;
        }
        return null;
    }

    public static StandBase getStand(String name) {
        StandBase s = null;
        List<IStand> list = HuajiAgeAPI.getStandList();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                StandBase stand = getStandByIndex(i);
                if (stand != null && stand.getName().equals(name)) {
                    s = stand;
                }
            }
        }
        return s;
    }
}
