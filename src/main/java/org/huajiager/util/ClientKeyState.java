package org.huajiager.util;

import java.util.function.BooleanSupplier;

/**
 * 客户端按键状态的中转。
 *
 * <p>主源码集（common）不能引用 {@code net.minecraft.client.*}，而物品 tooltip 需要判断
 * 是否按住 Shift，因此由客户端初始化时注入实现；服务端未注入时恒为 false。</p>
 */
public final class ClientKeyState {

    private static BooleanSupplier shiftDown = () -> false;

    private ClientKeyState() {
    }

    /** 由客户端初始化注入（如 {@code Screen::hasShiftDown}）。 */
    public static void setSupplier(BooleanSupplier supplier) {
        shiftDown = supplier;
    }

    /** 是否按住 Shift。 */
    public static boolean isShiftDown() {
        return shiftDown.getAsBoolean();
    }
}
