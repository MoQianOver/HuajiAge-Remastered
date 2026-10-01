package org.huajiager.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * 滑稽炸弹（huaji_bomb）， 。
 *
 * 行为：继承 BlockTNT，沙砾音效；EXPLODE 状态下 explode() 直接
 * createExplosion(null, x, y, z, 10f, true)（无飞行 TNT 实体）。
 *
 * Fabric 1.20.1 无 BlockTNT：TntBlock.primeTnt 为静态方法，生成的 TntEntity
 * 爆炸威力固定 4f，无法通过继承改写威力。因此继承 TntBlock 保留 TNT 方块身份
 * 与全部交互入口，重写各引爆入口（右键打火石/火焰弹、放置时红石、红石信号、
 * 被爆炸破坏、燃烧弹击中），统一改为直接生成 10f 爆炸，与
 * HuajiBomb.explode 的语义一致（不生成飞行 TNT 实体）。
 *
 * 已知边界：火烧 TNT 由 FireBlock 直接调用静态 TntBlock.primeTnt，无法
 * 拦截，火烧该方块时会退化为 TntEntity 爆炸（威力 4f），其余触发路径
 * 均为 10f。
 */
public class HuajiBomb extends TntBlock {

	public HuajiBomb() {
		super(AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY)
				.strength(0.0F)
				.sounds(BlockSoundGroup.SAND));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) {
			return super.onUse(state, world, pos, player, hand, hit);
		}
		ignite(world, pos);
		if (!player.getAbilities().creativeMode) {
			if (stack.isOf(Items.FLINT_AND_STEEL)) {
				stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
			} else {
				stack.decrement(1);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.isOf(state.getBlock()) && world.isReceivingRedstonePower(pos)) {
			ignite(world, pos);
		}
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (world.isReceivingRedstonePower(pos)) {
			ignite(world, pos);
		}
	}

	@Override
	public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
		if (!world.isClient) {
			ignite(world, pos);
		}
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (!world.isClient && projectile.isOnFire()) {
			ignite(world, hit.getBlockPos());
		}
	}

	private void ignite(World world, BlockPos pos) {
		if (!world.isClient) {
			world.removeBlock(pos, false);
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10.0F, true, World.ExplosionSourceType.BLOCK);
		}
	}
}
