package org.huajiager.stand.states;

import java.util.ArrayList;
import java.util.List;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.api.IStandState;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.PotionLoader;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

/**
 * 替身状态基类（StandStateBase）。
 * 注意 splitEnvironmentSourceSets 约束：本类只允许引用服务端/公共 API，
 * 纹理仅存 Identifier，不做 client 加载。
 */
public abstract class StandStateBase implements IStandState {
    protected String stand;
    protected String stateName;
    protected String ID = HuajiConstant.StandModels.DEFAULT_MODEL_ID;
    protected Identifier tex = new Identifier(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_the_world_default.png");
    protected List<String> extraDatas = new ArrayList<>();
    protected int stage;
    protected boolean isHandPlay;
    protected boolean soundLoop;
    /** 该状态声明的骨骼动画脚本列表（空表表示用模型条目声明 / 默认脚本）。 */
    protected List<String> animations = new ArrayList<>();

    public StandStateBase() {
    }

    public StandStateBase(String stand, String stateName, boolean isHandPlay, boolean soundLoop) {
        this.stand = stand;
        this.stateName = stateName;
        this.isHandPlay = isHandPlay;
        this.soundLoop = soundLoop;
        this.stage = 0;
        ID = HuajiAgeRemastered.MOD_ID + ":" + stand + "_" + stateName;
        tex = new Identifier(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_" + stand + "_" + stateName + ".png");
    }

    public StandStateBase(String stand, String stateName, boolean isHandPlay, boolean soundLoop, int stage) {
        this.stand = stand;
        this.stateName = stateName;
        this.isHandPlay = isHandPlay;
        this.soundLoop = soundLoop;
        this.stage = stage;
        ID = HuajiAgeRemastered.MOD_ID + ":" + stand + "_" + stateName;
        tex = new Identifier(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_" + stand + "_" + stateName + ".png");
    }

    public String getStand() {
        return stand;
    }

    public void setStand(String stand) {
        this.stand = stand;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public String getModelID() {
        return ID;
    }

    @Override
    public Identifier getTex() {
        return tex;
    }

    @Override
    public int getStage() {
        return stage;
    }

    public List<String> getExtraDatas() {
        return extraDatas;
    }

    public void addExtraData(String extraData) {
        this.extraDatas.add(extraData);
    }

    public boolean hasExtraData(String extraData) {
        return extraData != null && extraDatas.contains(extraData);
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setTex(Identifier tex) {
        this.tex = tex;
    }

    public boolean isHandPlay() {
        return isHandPlay;
    }

    public void setHandPlay(boolean handPlay) {
        isHandPlay = handPlay;
    }

    @Override
    public boolean isSoundLoop() {
        return soundLoop;
    }

    /** 该状态的骨骼动画脚本列表；空表表示使用模型条目声明或默认脚本。 */
    public List<String> getAnimations() {
        return animations;
    }

    public void setSoundLoop(boolean soundLoop) {
        this.soundLoop = soundLoop;
    }

    @Override
    public void doTaskOutOfTime(LivingEntity user) {
        user.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, 5 * 20));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5 * 20, 1));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20,
                ConfigHuaji.Stands.allowStandPunish ? 24 : 49));
        if (ConfigHuaji.Stands.allowStandPunish) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 5 * 20, 1));
        }
    }
}
