package org.huajiager.stand.instance;

import org.huajiager.capability.ExposedData;
import org.huajiager.entity.EmeraldBulletEntity;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.states.default_set.StateHierophantGreenDefault;
import org.huajiager.stand.states.idle.StateHierophantGreenIdle;
import org.huajiager.util.HAMathHelper;
import org.huajiager.util.NBTHelper;
import org.huajiager.util.ServerUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Hierophant Green 替身。
 *
 * 还原状态：
 */
public class StandHierophantGreen extends StandBase {

    public StandHierophantGreen() {
        super();
    }

    public StandHierophantGreen(String name, float speed, float damage, int duration, float distance, int cost, int charge,
                                String texPath, String localName, boolean displayHand) {
        super(name, speed, damage, duration, distance, cost, charge, texPath, localName, displayHand);
        initState(new StateHierophantGreenDefault(name, ExposedData.States.DEFAULT.getName(), isHandDisplay(), true));
        addState(ExposedData.States.IDLE.getName(),
                new StateHierophantGreenIdle(name, ExposedData.States.IDLE.getName(), true, false));
    }

    @Override
    public void doStandCapability(LivingEntity user) {
        Vec3d look = user.getRotationVector();
        Vec3d dist = new Vec3d(user.getX() + 8 * look.x, user.getY() + 8 * look.y, user.getZ() + 8 * look.z);
        List<LivingEntity> entityCollection = user.getWorld().getEntitiesByClass(LivingEntity.class,
                user.getBoundingBox().expand(30), e -> true);
        String uuid = "";
        double bestDegrees = Double.MAX_VALUE;
        if (!entityCollection.isEmpty()) {
            for (LivingEntity i : entityCollection) {
                if (i != user) {
                    Vec3d vec = HAMathHelper.getVectorEntityEye(user, i);
                    float degree1 = (float) HAMathHelper.getDegreeXZ(look, vec);
                    float degree2 = (float) HAMathHelper.getDegreeXY(look, vec);
                    float degree3 = (float) HAMathHelper.getDegreeXZ(look, vec);
                    if (degree1 < 15 && degree2 < 15 && degree3 < 15) {
                        float degrees = Math.abs(degree1) + Math.abs(degree2) + Math.abs(degree3);
                        if (degrees < bestDegrees) {
                            bestDegrees = degrees;
                            uuid = i.getUuid().toString();
                        }
                    }
                }
            }
        }
        if (!uuid.isEmpty()) {
            for (LivingEntity e : entityCollection) {
                if (e.getUuid().toString().equals(uuid)) {
                    doEmeraldSlashLiving(e, user);
                }
            }
        } else if (dist != null) {
            doEmeraldSlash(dist, user);
        }

        if (user instanceof ServerPlayerEntity) {
            ServerUtil.sendPacketToNearbyPlayersStand(user,
                    new MessageDoStandPowerClient(user.getName().getString(), StandLoader.HIEROPHANT_GREEN.getName()));
        }
    }

    @Override
    public void doStandCapabilityClient(World world, LivingEntity user) {
        world.playSound(user.getX(), user.getY(), user.getZ(),
                SoundLoader.STAND_HIEROPHANT_GREEN_EMERALD_SPLASH, SoundCategory.PLAYERS, 5f, 1f, true);
    }

    private void doEmeraldSlash(Vec3d dist, LivingEntity user) {
        if (dist != null) {
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                float rx = random.nextFloat() * 40 - 20;
                float ry = random.nextFloat() * 20;
                float rz = random.nextFloat() * 40 - 20;
                Vec3d pos = new Vec3d(user.getX() + rx, user.getY() + ry, user.getZ() + rz);
                Vec3d v = HAMathHelper.getVector(pos, dist);

                EmeraldBulletEntity bullet = new EmeraldBulletEntity(user.getWorld(), user);
                bullet.setPosition(pos.x, pos.y, pos.z);
                NBTHelper.setEntityFloat(bullet, "huajiage.motion.x", (float) v.x * 2.5f);
                NBTHelper.setEntityFloat(bullet, "huajiage.motion.y", (float) v.y * 2.5f);
                NBTHelper.setEntityFloat(bullet, "huajiage.motion.z", (float) v.z * 2.5f);
                bullet.setSplashHuge(true);
                bullet.setLife(360f);
                bullet.setStayTime(50);
                bullet.setRotation(360 * rx / 20);
                bullet.setPitch(360 * ry / 20);
                bullet.setDamage(5f);
                user.getWorld().spawnEntity(bullet);
            }
        }
    }

    private void doEmeraldSlashLiving(LivingEntity target, LivingEntity user) {
        if (target != null) {
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                float rx = random.nextFloat() * 40 - 20;
                float ry = random.nextFloat() * 20;
                float rz = random.nextFloat() * 40 - 20;
                Vec3d pos = new Vec3d(target.getX() + rx, target.getY() + ry, target.getZ() + rz);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60));
                EmeraldBulletEntity bullet = new EmeraldBulletEntity(user.getWorld(), user);
                bullet.setPosition(pos.x, pos.y, pos.z);
                bullet.setTarget(target.getUuid().toString());
                bullet.setSplashHuge(true);
                bullet.setLife(360f);
                bullet.setStayTime(50);
                bullet.setRotation(360 * rx / 20);
                bullet.setPitch(360 * ry / 20);
                bullet.setDamage(5f);
                user.getWorld().spawnEntity(bullet);
            }
        }
    }
}
