package org.huajiager.init.loaders;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.potion.PotionFiveBuff;
import org.huajiager.potion.PotionFlowerHope;
import org.huajiager.potion.PotionOrgaTarget;
import org.huajiager.potion.PotionOverdrive;
import org.huajiager.potion.PotionRepairEffect;
import org.huajiager.potion.PotionRequiem;
import org.huajiager.potion.PotionRequiemTarget;
import org.huajiager.potion.PotionStand;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 替身相关状态效果（药水）的 Fabric 注册器（ PotionLoader）。
 * 注册 id 沿用 版 registry name，需在 onInitialize 中与其它 Loader 一并调用。
 */
public final class PotionLoader {

    public static StatusEffect potionFive;
    public static StatusEffect potionFlowerHope;
    public static StatusEffect potionOrgaTarget;
    public static StatusEffect potionRequiem;
    public static StatusEffect potionRequiemTarget;
    public static StatusEffect potionStand;
    public static StatusEffect potionRepair;
    public static StatusEffect potionOverdrive;

    private PotionLoader() {
    }

    public static void register() {
        potionFive = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_five_buff"), new PotionFiveBuff());
        potionFlowerHope = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_flower"), new PotionFlowerHope());
        potionOrgaTarget = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_orga_target"), new PotionOrgaTarget());
        potionRequiem = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_requiem"), new PotionRequiem());
        potionRequiemTarget = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_requiem_target"), new PotionRequiemTarget());
        potionStand = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_stand"), new PotionStand());
        potionRepair = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_repair"), new PotionRepairEffect());
        potionOverdrive = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(HuajiAgeRemastered.MOD_ID, "potion_huaji_overdrive"), new PotionOverdrive());
    }
}
