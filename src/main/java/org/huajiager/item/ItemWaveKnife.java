package org.huajiager.item;

import java.util.List;

import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.stand.StandUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 波澜怒涛之刃（Wave Knife）。
 *
 * - 继承 SwordItem，自定义 ToolMaterial（等级3/耐久600/效率16.0/附魔20，修复材料波澜结晶）
 * - NBT 波澜机制：wave / wave_point / wave_max / wave_charge，随时间持续自动回复（每 100 tick +1）、命中增长、右键消耗冲刺
 * - 右键「冲浪」：消耗 1 点波澜点，进入 wave 状态并向前冲刺
 * - wave 状态：范围内小于 120° 的实体受到基于角度的魔法伤害，客户端沿视线撒水花
 * - 命中：附加魔法伤害、自身加速II+跳跃提升、波澜点 +（charge）
 * - 名称逐字彩虹色（以系统毫秒近似实现逐字循环着色）
 * - 隐者之紫替身 OVER_DRIVE 联动：持有隐者之紫（huajiager:hermit_purple）且处于波纹疾走蓄力时，每 tick 将波澜点点满
 *
 * 设计说明：伤害以 World.getDamageSources().indirectMagic 间接魔法伤害近似（沿用 DamageFivePower 口径）。
 */
public class ItemWaveKnife extends SwordItem {

    /** 波澜点在 NBT 缺省时的初始上限 */
    public static final int WAVE_MAX_INIT = 10;
    /** 进入 wave 状态后的持续 tick 数 */
    private static final int WAVE_START_TICKS = 10;

    private static final Formatting[] WAVE_COLORS = {
            Formatting.BLUE, Formatting.BLUE, Formatting.AQUA, Formatting.AQUA
    };

    private static final String KEY_WAVE = "wave";
    private static final String KEY_WAVE_POINT = "wave_point";
    private static final String KEY_WAVE_MAX = "wave_max";
    private static final String KEY_WAVE_CHARGE = "wave_charge";

    /** 波澜之刃专属 ToolMaterial：等级3/耐久600/效率16.0/附魔20，修复材料为波澜结晶 */
    private static final ToolMaterial WAVE = new ToolMaterial() {
        @Override
        public int getDurability() {
            return 600;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return 16.0f;
        }

        @Override
        public float getAttackDamage() {
            return 6.0f;
        }

        @Override
        public int getMiningLevel() {
            return 3;
        }

        @Override
        public int getEnchantability() {
            return 20;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(ItemLoader.waveCrystal);
        }
    };

    public ItemWaveKnife() {
        super(WAVE, 0, -2.4f, new Item.Settings().maxDamage(600));
    }

    // ==================== NBT 波澜数据 ====================

    private void setWave(ItemStack stack, int ticks) {
        stack.getOrCreateNbt().putInt(KEY_WAVE, ticks);
    }

    private int getWave(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(KEY_WAVE);
    }

    private boolean isWave(ItemStack stack) {
        return getWave(stack) > 0;
    }

    private void setWavePoint(ItemStack stack, int points) {
        stack.getOrCreateNbt().putInt(KEY_WAVE_POINT, points);
    }

    private int getWavePoint(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(KEY_WAVE_POINT);
    }

    private boolean isWavePointEnough(ItemStack stack) {
        return getWavePoint(stack) > 0;
    }

    private void setWaveMax(ItemStack stack, int points) {
        stack.getOrCreateNbt().putInt(KEY_WAVE_MAX, points);
    }

    private int getWaveMax(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(KEY_WAVE_MAX) + WAVE_MAX_INIT;
    }

    private void setWaveCharge(ItemStack stack, int points) {
        stack.getOrCreateNbt().putInt(KEY_WAVE_CHARGE, points);
    }

    private int getWaveCharge(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(KEY_WAVE_CHARGE) + 1;
    }

    /** 命中叠加波澜点，不超过当前上限 */
    private void waveCharge(ItemStack stack) {
        setWavePoint(stack, Math.min(getWavePoint(stack) + getWaveCharge(stack), getWaveMax(stack)));
    }

    // ==================== 实例行为 ====================

    /** 物品每 tick 结算：每 100 tick（5 秒）自动回复 1 点波澜点、wave 状态逐 tick 递减（粒子/伤害） */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        // 自动回复：每 100 tick（5 秒）回复 1 点波澜点，满 10 点共需 50 秒，与"每 1000 tick 一次性回满"的
        // 平均速率一致；改为持续小步回复，避免一次性回满在客户端/服务端 NBT 同步竞态下出现"恢复不可见"
        // （一次性回满在 50 秒内看不到任何中间变化）。
        if (entity.age % 100 == 0 && getWavePoint(stack) < getWaveMax(stack)) {
            setWavePoint(stack, getWavePoint(stack) + 1);
        }

        if (isWave(stack)) {
            setWave(stack, getWave(stack) - 1);
            if (world.isClient) {
                spawnWaveParticles(world, entity);
            } else {
                damageNearbyEntities(world, entity);
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            refreshWaveByOverdrive(stack, livingEntity);
        }
    }

    private void spawnWaveParticles(World world, Entity entity) {
        Vec3d look = entity.getRotationVec(1.0f);
        double x = entity.getX() + look.x * 2.0;
        double y = entity.getBodyY(0.5) + look.y * 2.0;
        double z = entity.getZ() + look.z * 2.0;
        world.addParticle(ParticleTypes.SPLASH, x, y, z, -look.x, -look.y, -look.z);
    }

    private void damageNearbyEntities(World world, Entity source) {
        Box box = source.getBoundingBox().expand(2.0);
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box, e -> e != source)) {
            Vec3d toTarget = target.getEyePos().subtract(source.getPos());
            double degree = angleBetweenDegrees(source.getRotationVec(1.0f), toTarget);
            if (degree <= 120.0) {
                float damage = (float) ((180.0 - degree) / 20.0) + 8.0f;
                target.damage(DamageLoader.waveHit(target), damage);
            }
        }
    }

    private static double angleBetweenDegrees(Vec3d a, Vec3d b) {
        double la = a.length();
        double lb = b.length();
        if (la < 1.0e-6 || lb < 1.0e-6) {
            return 180.0;
        }
        double cos = a.dotProduct(b) / (la * lb);
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return Math.toDegrees(Math.acos(cos));
    }

    /**
     * 隐者之紫 OVER_DRIVE 联动：玩家持有隐者之紫替身（huajiager:hermit_purple）且处于波纹疾走蓄力、
     * 附带 potionOverdrive 且蓄力缓存 > 0 时，每 tick 将波澜点点满。
     */
    private void refreshWaveByOverdrive(ItemStack stack, LivingEntity user) {
        IExposedData data = StandUtil.getStandData(user);
        StandHandler handler = StandUtil.getStandHandler(user);
        if (data != null && handler != null
                // 替身名兼容短名（hermit_purple）与完整 ID（huajiager:hermit_purple）两种形态：
                // Disc/塔罗牌装载写入的 standId 形式取决于获得路径，与工程内其它隐者之紫判断口径一致
                && ("hermit_purple".equals(data.getStand())
                        || "huajiager:hermit_purple".equals(data.getStand()))
                && HuajiConstant.BuffTags.OVER_DRIVE.equals(handler.getBuffTag())
                && user.hasStatusEffect(PotionLoader.potionOverdrive)
                && handler.getBuffer() > 0) {
            setWavePoint(stack, getWaveMax(stack));
        }
    }

    /** 命中：附加魔法伤害、自身加速II+跳跃提升、波澜点叠加 */
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.damage(DamageLoader.waveHit(target), 5.0f);
        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2));
        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 0));
        waveCharge(stack);
        return super.postHit(stack, target, attacker);
    }

    /**
     * 右键「冲浪」：消耗 1 点波澜点，进入 wave 状态并向前冲刺。
     * 波澜点不足时同样按 SUCCESS 处理不落空物品（波澜点依赖 inventoryTick 每 100 tick 自动回复 1 点），
     * 并额外补摆臂 + 服务端提示消息，让波澜点不足这一状态可感知，避免"右键无反应"被误解为 bug。
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (isWavePointEnough(stack)) {
            setWave(stack, WAVE_START_TICKS);
            setWavePoint(stack, getWavePoint(stack) - 1);

            Vec3d vec = user.getRotationVec(1.0f);
            user.addVelocity(vec.x * 1.5, vec.y * 1.5, vec.z * 1.5);
            user.fallDistance = 0.0f;
            user.swingHand(hand);

            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.NEUTRAL, 1.5f, 1.0f);
            world.playSound(null, user.getBlockPos().add((int) vec.x, (int) vec.y, (int) vec.z),
                    SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return TypedActionResult.success(stack);
        }
        // 波澜点不足（典型：新合成/刚用尽，需等 1000 tick 自动回满）：
        // 仍按 SUCCESS 返回语义处理，同时给出可感知反馈，避免"右键无效果"误判。
        user.swingHand(hand);
        if (!world.isClient) {
            user.sendMessage(Text.translatable("item.wave_knife.tooltip.nopoint", getWavePoint(stack)), true);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public Text getName(ItemStack stack) {
        String name = super.getName(stack).getString();
        int base = (int) (System.currentTimeMillis() / 200) % WAVE_COLORS.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            sb.append(WAVE_COLORS[(base + i) % WAVE_COLORS.length]).append(name.charAt(i));
        }
        return Text.literal(sb.toString());
    }

    /** 动态状态 tooltip（波澜点/上限/每命中增长），依赖语言键 item.wave_knife.tooltip.1~3 */
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.wave_knife.tooltip.1", getWavePoint(stack)));
        tooltip.add(Text.translatable("item.wave_knife.tooltip.2", getWaveMax(stack)));
        tooltip.add(Text.translatable("item.wave_knife.tooltip.3", getWaveCharge(stack)));
    }
}
