package org.huajiager.stand;

import org.huajiager.api.HuajiAgeAPI;
import org.huajiager.api.IStandState;

import java.util.ArrayList;
import java.util.List;

/**
 * 替身状态机的注册表查询工具（StandStates）。
 * 返回类型放宽为 IStandState，避免对具体的 StandStateBase 实现产生编译期依赖。
 */
public class StandStates {

    public static IStandState getStateByIndex(int index) {
        List<IStandState> list = HuajiAgeAPI.getStandStateList();
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    public static List<IStandState> getStandStateListByStand(String name) {
        List<IStandState> s = new ArrayList<>();
        List<IStandState> list = HuajiAgeAPI.getStandStateList();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                IStandState state = getStateByIndex(i);
                if (state != null && state.getStand().equals(name)) {
                    s.add(state);
                }
            }
        }
        return s;
    }

    public static IStandState getStandState(String stand, String state) {
        List<IStandState> list = HuajiAgeAPI.getStandStateList();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                IStandState s = getStateByIndex(i);
                if (s != null && s.getStand().equals(stand) && s.getStateName().equals(state)) {
                    return s;
                }
            }
        }
        return null;
    }
}
