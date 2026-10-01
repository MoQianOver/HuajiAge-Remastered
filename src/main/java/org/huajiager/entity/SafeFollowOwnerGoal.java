package org.huajiager.entity;

import java.util.EnumSet;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * 基于 FollowOwnerGoal 构造（this, 1.0D, 5, 20）：
 * canStart：主人 5 格内不启动；shouldContinue：导航空闲或距离进入 20 格内即停。 * tick：距离 >= 12 格尝试安全传送，否则 2 格外导航走路跟随。
 * 传送点按 isTeleportFriendlyBlock 校验（脚下 UP 面实心 + 自身/上方非完整方块），
 * 主人飞行时传送点脚下无支撑 -> 不传送、不跟飞，与行为一致。
 */
public class SafeFollowOwnerGoal extends Goal {

	private final TameableEntity tameable;
	private final double speed;
	private final float minDistance; // 默认 minDist=5
	private final float maxDistance; // 默认 maxDist=20
	private final World world;
	private final EntityNavigation navigation;
	private LivingEntity owner;
	private int updateCountdownTicks;

	public SafeFollowOwnerGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance) {
		this.tameable = tameable;
		this.speed = speed;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.world = tameable.getWorld();
		this.navigation = tameable.getNavigation();
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		LivingEntity owner = this.tameable.getOwner();
		if (owner == null || owner.isSpectator() || this.tameable.isSitting()) {
			return false;
		}
		// 5 格内不跟
		if (this.tameable.squaredDistanceTo(owner) < (double) (this.minDistance * this.minDistance)) {
			return false;
		}
		this.owner = owner;
		return true;
	}

	@Override
	public boolean shouldContinue() {
		//  shouldContinueExecuting：导航空闲（已到/失败）或距离进入 20 格内即停止，
		// 小车跟到 20 格内就停住看主人，不会贴脸也不会一直追着走
		return !this.navigation.isIdle()
				&& this.tameable.squaredDistanceTo(this.owner) > (double) (this.maxDistance * this.maxDistance);
	}

	@Override
	public void start() {
		this.updateCountdownTicks = 0;
	}

	@Override
	public void stop() {
		this.owner = null;
		this.navigation.stop();
	}

	@Override
	public void tick() {
		if (this.owner == null) {
			return;
		}
		this.tameable.getLookControl().lookAt(this.owner, 10.0F, (float) this.tameable.getMaxHeadRotation());
		if (--this.updateCountdownTicks > 0) {
			return;
		}
		this.updateCountdownTicks = this.getTickCount(10);
		//  updateTask：距离 >= 12 格（144）尝试传送；否则 2 格外导航走路。
		// 主人飞行（脚下无支撑）时传送点不满足 isTeleportFriendlyBlock 条件、
		// 不会传送到半空；导航也走不到空中，小车原地待命——与行为一致，不跟飞。
		if (this.tameable.squaredDistanceTo(this.owner) >= 144.0D) {
			this.tryTeleportToEntity();
		} else if (this.tameable.squaredDistanceTo(this.owner) > 4.0D) {
			this.navigation.startMovingTo(this.owner, this.speed);
		}
	}

	//  tryTeleportToEntity：在主人脚底那一层（x/z ±2、y=主人脚部高度）
	// 找 isTeleportFriendlyBlock 的安全落点。主人飞行时脚下无支撑，找不到安全点，
	// 小车不会传送到半空，也就不会"跟着主人飞"。
	private void tryTeleportToEntity() {
		int ox = MathHelper.floor(this.owner.getX()) - 2;
		int oz = MathHelper.floor(this.owner.getZ()) - 2;
		int oy = MathHelper.floor(this.owner.getBoundingBox().minY);
		for (int l = 0; l <= 4; ++l) {
			for (int i1 = 0; i1 <= 4; ++i1) {
				if (isTeleportFriendlyBlock(ox + l, oy, oz + i1)) {
					this.tameable.refreshPositionAndAngles(
							(double) (ox + l) + 0.5D, (double) oy, (double) (oz + i1) + 0.5D,
							this.tameable.getYaw(), this.tameable.getPitch());
					this.navigation.stop();
					return;
				}
			}
		}
	}

	//  isTeleportFriendlyBlock：脚下 UP 面实心可站立，
	// 自身格与上方格不是完整方块（不会卡进地里/顶头）。
	private boolean isTeleportFriendlyBlock(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockState below = this.world.getBlockState(pos.down());
		if (!below.isSideSolidFullSquare(this.world, pos.down(), Direction.UP)) {
			return false;
		}
		BlockState self = this.world.getBlockState(pos);
		BlockState up = this.world.getBlockState(pos.up());
		return !self.isFullCube(this.world, pos) && !up.isFullCube(this.world, pos.up());
	}
}
