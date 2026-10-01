package org.huajiager.stand.custom;

import java.util.Map;
import java.util.Objects;

import org.huajiager.api.IStandState;
import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandResourceLoader;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;

import net.minecraft.entity.LivingEntity;

/**
 * 自定义替身（ ）。
 * <p>由 {@link StandCustomInfo} 驱动：attributes 数组填充基础属性（速度/伤害/持续/距离/
 * 冷却/充能/最大精神力），states 列表将 custom_stand/states/<stand>_<state>.js 逐状态
 * 包装为 {@link StandStateCustom} 并 addState 注册（addState 内部自动进 HuajiAgeAPI
 * 状态注册表）。default 态决定是否手持展示（displayHand）。</p>
 */
public class StandCustom extends StandBase {

    private StandCustomInfo info;

    public StandCustom(StandCustomInfo info) {
        Objects.requireNonNull(info, "info");
        this.info = info;
        this.name = info.getStand();
        this.localName = info.getName();
        if (info.getAttributes().size() >= 7) {
            this.speed = info.getAttributes().get(0);
            this.damage = info.getAttributes().get(1);
            this.duration = info.getAttributes().get(2).intValue();
            this.distance = info.getAttributes().get(3);
            this.cost = info.getAttributes().get(4).intValue();
            this.charge = info.getAttributes().get(5).intValue();
            this.maxMP = info.getAttributes().get(6).intValue();
        }

        Map<String, StandStateInfo> customStateServer = StandResourceLoader.CUSTOM_STATE_SERVER;
        for (String state : info.getStates()) {
            String key = info.getStand() + "_" + state;
            StandStateInfo infoState = customStateServer.get(key);
            if (infoState != null && key.equals(info.getStand() + "_" + infoState.getStateId())) {
                this.addState(infoState.getStateId(), new StandStateCustom(infoState));
            }
        }

        if (!this.states.isEmpty()) {
            IStandState defaultState = StandStates.getStandState(name, states.get(0));
            if (defaultState instanceof StandStateBase) {
                this.displayHand = ((StandStateBase) defaultState).isHandPlay();
            }
        }
    }

    public StandCustom() {
    }

    public StandCustomInfo getInfo() {
        return info;
    }

    @Override
    public void doStandPower(LivingEntity user) {
        super.doStandPower(user);
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        doStandCapabilityResult(user);
    }

    /**
     * 执行自定义替身 capability，返回 JS capability 是否真正触发技能
     * （JS 返回布尔：false = 未触发，如隐者之紫没拿相机，供服务端退还精神力）。
     * 未返回布尔 / 无自定义状态时按 true（已触发）处理，保持对其他替身行为不变。
     */
    public boolean doStandCapabilityResult(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        IExposedData data = StandUtil.getStandData(user);
        if (type == null || data == null) {
            return true;
        }
        IStandState standState = StandStates.getStandState(type.getName(), data.getState());
        if (standState instanceof StandStateCustom) {
            return ((StandStateCustom) standState).doTaskCapability(user);
        }
        return true;
    }
}
