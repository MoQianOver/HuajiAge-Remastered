package org.huajiager.damage_source;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

/**
 * 希望命中伤害源。
 *
 * 继承 EntityDamageSource 并对攻击方施加"绝对伤害"（setDamageIsAbsolute，无视护甲）。
 * 此处以间接魔法伤害近似：
 * indirectMagic(attacker, attacker) 保留击杀归属，魔法伤害同样不被护甲减免，语义接近。
 */
public final class DamageHopeHit {

    private DamageHopeHit() {
    }

    /**
     * 构造以 attacker 为直接伤害来源的魔法伤害源。
     *
     * @param world    世界
     * @param attacker 攻击者
     */
    public static DamageSource create(World world, Entity attacker) {
        return world.getDamageSources().indirectMagic(attacker, attacker);
    }
}
