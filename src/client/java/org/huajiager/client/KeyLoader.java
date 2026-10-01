package org.huajiager.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

/**
 * 替身按键注册器， 。
 *
 * 版四个按键均带 CONTROL 修饰符；Fabric 1.20.1 的 KeyBinding 不支持
 * 组合修饰符。后改为「独立单键绑定」：这里仅注册原始键位（默认 K/P/O/I），
 * 玩家可在 设置→控制→按键绑定 里改为任意单键（如 G），EventStandKey 直接
 * 轮询各 KeyBinding 触发，不再要求按住 Ctrl。
 */
public final class KeyLoader {

	private static final String HUAJI_KEY_GROUP = "key.category.huajiager";

	public static final KeyBinding MODE_SWITCH = new KeyBinding("key.huajiager.switch",
			InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, HUAJI_KEY_GROUP);
	public static final KeyBinding STAND_UP = new KeyBinding("key.huajiager.stand_up",
			InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, HUAJI_KEY_GROUP);
	public static final KeyBinding STAND_SKILL = new KeyBinding("key.huajiager.stand_skill",
			InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, HUAJI_KEY_GROUP);
	public static final KeyBinding STAND_SWITCH = new KeyBinding("key.huajiager.stand_switch",
			InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, HUAJI_KEY_GROUP);

	private KeyLoader() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(MODE_SWITCH);
		KeyBindingHelper.registerKeyBinding(STAND_UP);
		KeyBindingHelper.registerKeyBinding(STAND_SKILL);
		KeyBindingHelper.registerKeyBinding(STAND_SWITCH);
	}
}
