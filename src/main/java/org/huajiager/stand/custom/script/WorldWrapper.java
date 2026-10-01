package org.huajiager.stand.custom.script;

import net.minecraft.world.World;

/**
 * 世界对象包装（ ）。
 * <p>供自定义替身 JS 脚本访问实体所在世界。原代码基于酒石酸团队“车万女仆”模组代码，
 * 依据 MIT 协议进行编写。</p>
 */
public class WorldWrapper {

    private World world;

    public WorldWrapper(World world) {
        this.world = world;
    }

    public World getWorld() {
        return this.world;
    }
}
