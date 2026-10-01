package org.huajiager.damage_source;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

/**
 * 五行之力伤害源。
 *
 * 继承 EntityDamageSource 并以玩家为伤害来源；Fabric 侧同 DamageHopeHit 的策略，
 * 以间接魔法伤害近似"绝对伤害"语义。
 */
public final class DamageFivePower {

    private DamageFivePower() {
    }

    /**
     * 构造以 player 为直接伤害来源的魔法伤害源。
     *
     * @param world  世界
     * @param player 持有五行的玩家
     */
    public static DamageSource create(World world, Entity player) {
        return world.getDamageSources().indirectMagic(player, player);
    }
}
