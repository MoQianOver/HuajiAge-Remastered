package org.huajiager.init.loaders;

import org.huajiager.entity.EmeraldBulletEntity;
import org.huajiager.entity.EntityDiscCommand;
import org.huajiager.entity.EntityFivePower;
import org.huajiager.entity.EntityHeroArrow;
import org.huajiager.entity.EntityLordLuWing;
import org.huajiager.entity.EntityMultiKnife;
import org.huajiager.entity.EntityOrgaHairKnife;
import org.huajiager.entity.EntityRoadRoller;
import org.huajiager.entity.EntitySecondFoil;
import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.stand.entity.EntityStandBase;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 实体注册器（EntityEmeraldBullet 等）。
 * Fabric 下在 onInitialize 中调用 register() 即可。
 */
public final class EntityLoader {
    private EntityLoader() {
    }

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "emerald_bullet"), EmeraldBulletEntity.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "five_power"), EntityFivePower.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "road_roller"), EntityRoadRoller.TYPE_ENTITY);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "sheer_heart_attack"), EntitySheerHeartAttack.TYPE);
        // 杀手皇后·枯萎穿透（SheerHeartAttack）继承 TameableEntity，必须注册属性容器，
        // 否则构造时 getMaxHealth 读到 fallback=null 直接 NPE，召唤链路崩溃。
        FabricDefaultAttributeRegistry.register(EntitySheerHeartAttack.TYPE, EntitySheerHeartAttack.SHEER_ATTRIBUTES);
        // Lord.Lu 翅膀展示实体：继承 Entity 基类（纯展示，无 LivingEntity 属性容器，无需属性注册）
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "lord_lu_wing"), EntityLordLuWing.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "hero_arrow"), EntityHeroArrow.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "multi_knife"), EntityMultiKnife.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "orga_hair_knife"), EntityOrgaHairKnife.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "stand_base"), EntityStandBase.TYPE_ENTITY);
        // 继承马类（LivingEntity）的替身实体必须注册默认属性，否则构造器读取属性容器为 null 直接 NPE，
        // 导致召唤链路崩溃（日志: this.fallback is null @ EntityStandBase.<init>）。
        FabricDefaultAttributeRegistry.register(EntityStandBase.TYPE_ENTITY,
                AbstractHorseEntity.createBaseHorseAttributes());
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "disc_command"), EntityDiscCommand.TYPE);
        Registry.register(Registries.ENTITY_TYPE,
                new Identifier("huajiager", "second_foil"), EntitySecondFoil.TYPE);
    }
}
