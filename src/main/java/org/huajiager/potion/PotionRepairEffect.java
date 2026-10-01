package org.huajiager.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * 对应 版 PotionRepairEffect。
 * <p>"物品修复"药水效果：持续修复副手 / 背包 / 装备栏物品耐久，每秒恢复 40 点。
 * 原实现为空壳（未覆写 applyUpdateEffect），大招只瞬间修了主手、药水效果形同虚设
 * → 副手/背包/装备栏耐久不回。现按原作描述实现：主手已由 capability 瞬间修复，
 * 药水持续修复仅覆盖副手、背包（36 格，排除主手格）与装备栏（4 格）。</p>
 */
public class PotionRepairEffect extends StatusEffect {

    /** 每秒恢复的耐久点数 */
    private static final int REPAIR_PER_SECOND = 40;

    public PotionRepairEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFF00);
    }

    /** 每 tick 都进入 applyUpdateEffect，由其内部按 20 tick 节奏批量修复 */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // 每秒（20 tick）一次批量修复，避免逐 tick 轮询背包
        if (entity.age % 20 != 0) {
            return;
        }
        if (entity instanceof PlayerEntity player) {
            // 副手
            repairStack(player.getOffHandStack(), REPAIR_PER_SECOND);
            // 背包 36 格（含快捷栏），排除主手格（主手由技能瞬间修复，不参与持续修复）
            int selected = player.getInventory().selectedSlot;
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                if (i == selected) {
                    continue;
                }
                repairStack(player.getInventory().main.get(i), REPAIR_PER_SECOND);
            }
            // 装备栏 4 格
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                repairStack(player.getInventory().armor.get(i), REPAIR_PER_SECOND);
            }
        }
    }

    /** 单件物品恢复指定耐久点数，不超过上限 */
    private static void repairStack(ItemStack stack, int amount) {
        if (stack == null || stack.isEmpty() || stack.getMaxDamage() <= 0 || !stack.isDamaged()) {
            return;
        }
        stack.setDamage(Math.max(0, stack.getDamage() - amount));
    }
}
