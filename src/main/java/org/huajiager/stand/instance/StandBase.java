package org.huajiager.stand.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.huajiager.api.HuajiAgeAPI;
import org.huajiager.api.IStand;
import org.huajiager.api.IStandState;
import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.states.StandStateBase;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/**
 * 替身(Stand)基类。
 *
 * 保留字段 / 构造 / getter / 状态容器（addState、initState、
 * putInternalStandStates、chaeckState 等）构成的注册链路核心，无重型外部依赖。 * doStandPower / doStandCapability / doStandCapabilityClient / getBindingRes 等方法
 * 由具体替身按需实现。
 *
 * 注意：状态常量统一收敛为
 * org.huajiager.capability.ExposedData.States。
 */
public class StandBase implements IStand {

    protected String name;
    protected float speed;
    protected float damage;
    protected int duration;
    protected int charge;
    protected float distance;
    protected int cost;
    protected int maxMP;
    protected String texPath;
    protected String localName;
    protected List<String> states = new ArrayList<>();
    protected Map<String, StandStateBase> statesMap = new HashMap<>();
    protected boolean displayHand;

    public StandBase() {
        // loadStates() 未启用，保持空
    }

    public StandBase(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                     String texPath, String localName, boolean displayHand) {
        this.name = name;
        this.speed = speed;
        this.damage = damage;
        this.duration = duration;
        this.distance = distance;
        this.cost = cost;
        this.charge = charge;
        this.texPath = texPath;
        this.localName = localName;
        this.displayHand = displayHand;
        this.maxMP = this.charge * 1200;
    }

    public StandBase(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                     int maxMP, String texPath, String localName, boolean displayHand) {
        this.name = name;
        this.speed = speed;
        this.damage = damage;
        this.duration = duration;
        this.distance = distance;
        this.cost = cost;
        this.charge = charge;
        this.maxMP = maxMP;
        this.texPath = texPath;
        this.localName = localName;
        this.displayHand = displayHand;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDamage() {
        return damage;
    }

    public int getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }

    public int getCost() {
        return cost;
    }

    public int getCharge() {
        return charge;
    }

    public int getMaxMP() {
        return maxMP;
    }

    public String getName() {
        return name;
    }

    public String getTexPath() {
        return texPath;
    }

    public String getLocalName() {
        return localName;
    }

    public boolean isHandDisplay() {
        return displayHand;
    }

    // }

    public Map<String, StandStateBase> getStatesMap() {
        return statesMap;
    }

    public List<String> getStates() {
        return states;
    }

    public void addStates(List<String> states) {
        this.states.addAll(states);
    }

    public void addStates(Map<String, StandStateBase> states) {
        this.states.addAll(states.keySet());
        this.statesMap.putAll(states);
    }

    public void addState(String state, StandStateBase stateBase) {
        this.states.add(state);
        this.statesMap.put(state, stateBase);
        HuajiAgeAPI.registerStandState(stateBase);
    }

    public void initState(StandStateBase stateBase) {
        addState(ExposedData.States.DEFAULT.getName(), stateBase);
    }

    public void putInternalStandStates() {
        for (String key : statesMap.keySet()) {
            StandStateBase stateBase = statesMap.get(key);
            if (stateBase != null) {
                HuajiAgeAPI.registerStandState(stateBase);
            }
        }
    }

    public boolean chaeckState(String state) {
        return this.states.contains(state);
    }

    @Override
    public void doStandPower(LivingEntity user) {
        StandBase type = StandUtil.getType(user);
        IExposedData data = StandUtil.getStandData(user);
        if (type == null && data == null) {
            return;
        }
        IStandState standState = StandStates.getStandState(type.getName(), data.getState());
        if (standState != null) {
            standState.doTask(user);
        }
    }

    @Override
    public void doStandCapability(LivingEntity user) {
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
    }
}
