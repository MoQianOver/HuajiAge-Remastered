package org.huajiager.damage_source;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

/**
 * 绿宝石弹幕间接伤害源， 。
 *
 * 继承 EntityDamageSourceIndirect("explosion", source, indirectEntityIn) 并追加
 * setExplosion()/setProjectile()/setIsThornsDamage()。Fabric 1.20.1 中直接使用
 * DamageSources#explosion(attacker, source) 工厂即可获得等价语义的"间接爆炸投射伤害"。
 */
public final class DamageEmeraldSplash {
    private DamageEmeraldSplash() {
    }

    public static DamageSource create(World world, Entity attacker, Entity indirectSource) {
        return world.getDamageSources().explosion(attacker, indirectSource);
    }
}
