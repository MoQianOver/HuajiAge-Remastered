package org.huajiager.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * ModMenu 集成（可选前置）。仅当已安装 modmenu 时由 fabric.mod.json 的
 * "modmenu" entrypoint 加载。未安装 cloth-config 时返回 null，
 * ModMenu 自动隐藏"配置"按钮，且不会触发 Cloth 相关类加载。
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                return ConfigScreen.createScreen(parent);
            }
            return null;
        };
    }
}
