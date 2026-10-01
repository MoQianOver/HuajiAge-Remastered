package org.huajiager.stand.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.huajiager.capability.StandHandler;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.util.NBTHelper;

import java.util.List;

/**
 * 时停辅助， 。
 *
 *  81 行整体。doTimeStopClient 入参 WorldClient，此处以 World + World.playSound 承载，
 * 仅客户端侧调用，达到同等本地播放效果。
 */
public final class TimeStopHelper {

    private TimeStopHelper() {
    }

    public static List<Entity> getTagetsInRange(Entity entity, double distance) {
        Box box = entity.getBoundingBox().expand(distance);
        return entity.getWorld().getEntitiesByClass(Entity.class, box, e -> true);
    }

    public static void setTimeStop(Entity entity, int ticks) {
        NBTHelper.setEntityInteger(entity, HuajiConstant.Tags.THE_WORLD, ticks);
        if (entity instanceof PlayerEntity) {
            StandHandler chargeHandler = StandUtil.getStandHandler((LivingEntity) entity);
            if (chargeHandler != null) {
                chargeHandler.setBuffer(ticks);
                chargeHandler.setBuffTag(HuajiConstant.BuffTags.TIME_STOP);
            }
        }
    }

    public static void setEntityTimeStopRange(Entity entity, double distance) {
        NBTHelper.setEntityDouble(entity, HuajiConstant.Tags.TIME_STOP_RANGE, distance);
    }

    public static void extraEffects(LivingEntity entity, int time) {
        StandBase stand = StandUtil.getType(entity);
        if (stand == null) {
            return;
        }
        double rand = Math.random() * 100;
        if (!stand.equals(StandLoader.STAR_PLATINUM)) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, time * 20, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, time * 20, 4));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, time * 20, 6));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, time * 20, 4));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, time * 20, 2));
            entity.heal(5f);
            // 行为：30% 概率向玩家发放 roadRoller（压路机）
            if (rand < 30d && entity instanceof PlayerEntity player) {
                player.getInventory().offerOrDrop(new ItemStack(ItemLoader.roadRoller));
            }
        } else {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, time * 20, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, time * 20, 1));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, time * 20, 2));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, time * 20, 1));
        }
    }

    @SuppressWarnings("unused")
    public static void doTimeStopClient(World world, Vec3d pos, StandBase stand) {
        double rand = Math.random() * 100;
        if (!stand.getName().equals(StandLoader.STAR_PLATINUM.getName())) {
            if (rand < 25) {
                world.playSound((PlayerEntity) null, pos.x, pos.y, pos.z, SoundLoader.THE_WORLD, SoundCategory.PLAYERS, 5f, 1f);
            } else if (rand < 50) {
                world.playSound((PlayerEntity) null, pos.x, pos.y, pos.z, SoundLoader.THE_WORLD_1, SoundCategory.PLAYERS, 5f, 1f);
            } else if (rand < 75) {
                world.playSound((PlayerEntity) null, pos.x, pos.y, pos.z, SoundLoader.THE_WORLD_2, SoundCategory.PLAYERS, 5f, 1f);
            } else {
                world.playSound((PlayerEntity) null, pos.x, pos.y, pos.z, SoundLoader.THE_WORLD_3, SoundCategory.PLAYERS, 5f, 1f);
            }
        } else {
            // 白金之星时停开场音：用户新录 star_platinum_the_world_2，
            // 替换此前沿用的 STAR_PLATINUM_THE_WORLD_1。
            world.playSound((PlayerEntity) null, pos.x, pos.y, pos.z, SoundLoader.STAR_PLATINUM_THE_WORLD_2, SoundCategory.PLAYERS, 5f, 1f);
        }
    }
}
