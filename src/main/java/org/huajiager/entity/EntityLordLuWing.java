package org.huajiager.entity;

import java.util.UUID;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemBlancedHelmet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * 五五开头盔 Lord.Lu 翅膀展示实体（独立实体渲染，替代原 ArmorRenderer 背部渲染）。
 *
 * 背景：原实现把翅膀画在玩家盔甲渲染流程内，深度与玩家身体/披风交错，
 * 第三人称背视角翅膀被玩家身体与披风遮挡（"前方生物和披风遮住翅膀"）。
 * 改为独立实体后，实体位于玩家背后 0.5 格（排序用），背视角下比玩家离相机更近，
 * 渲染排序在玩家之后 → 翅膀必然覆盖玩家身体与披风；正视角仍被玩家身体自然遮挡。
 *
 * 行为：模式开启（MODE_SWITCH 切换 open=true）且戴平衡头盔（lord=true）时服务端
 * 生成并每 tick 跟随玩家；切回模式（open=false）、摘盔、死亡或退出时实体自身
 * tick 自愈销毁（与替身实体的自愈范式一致）。
 */
public class EntityLordLuWing extends Entity {

	private final String noUser = "8fdd0799-16c2-49d9-bdea-e75a07b9ec04";

	private static final String TAG_USER = "user";
	private static final String TAG_USER_NAME = "userName";

	private static final TrackedData<String> USER = DataTracker.registerData(EntityLordLuWing.class,
			TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> USERNAME = DataTracker.registerData(EntityLordLuWing.class,
			TrackedDataHandlerRegistry.STRING);

	public static final EntityType<EntityLordLuWing> TYPE = EntityType.Builder
			.<EntityLordLuWing>create((type, world) -> new EntityLordLuWing(type, world), SpawnGroup.MISC)
			.setDimensions(0.1f, 0.1f)
			.build("huajiager:lord_lu_wing");

	public EntityLordLuWing(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(USER, noUser);
		this.dataTracker.startTracking(USERNAME, "steve");
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putString(TAG_USER, this.dataTracker.get(USER));
		nbt.putString(TAG_USER_NAME, this.dataTracker.get(USERNAME));
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		this.dataTracker.set(USER, nbt.getString(TAG_USER).isEmpty() ? noUser : nbt.getString(TAG_USER));
		this.dataTracker.set(USERNAME, nbt.getString(TAG_USER_NAME).isEmpty() ? "steve" : nbt.getString(TAG_USER_NAME));
	}

	/**
	 * 纯展示实体：免疫一切伤害、不可推动、无重力（与替身实体同口径）。
	 */
	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean hasNoGravity() {
		return true;
	}

	/**
	 * 放大可见盒（8 格半径）：默认 0.1x0.1 极小盒在视角晃动时频繁进出视锥，
	 * 翅膀会一帧帧闪没（替身实体同款问题同款修复）。
	 */
	@Override
	public Box getVisibilityBoundingBox() {
		return new Box(getX() - 8.0, getY() - 8.0, getZ() - 8.0,
				getX() + 8.0, getY() + 8.0, getZ() + 8.0);
	}

	/**
	 * 按 DataTracker 中记录的宿主玩家查找实体（USER=UUID / USERNAME 双路匹配，
	 * 与 EntityStandBase.getUser 同口径）。
	 */
	public PlayerEntity getOwner() {
		if (this.getWorld() == null) {
			return null;
		}
		UUID uid = null;
		String userId = this.dataTracker.get(USER);
		if (!userId.isEmpty() && !userId.equals(noUser)) {
			try {
				uid = UUID.fromString(userId);
			} catch (IllegalArgumentException ignored) {
				uid = null;
			}
		}
		String userName = this.dataTracker.get(USERNAME);
		for (PlayerEntity player : this.getWorld().getPlayers()) {
			if (player.getName().getString().equals(userName)) {
				return player;
			}
			if (uid != null && uid.equals(player.getUuid())) {
				return player;
			}
		}
		return null;
	}

	public void setUser(String uuid) {
		if (uuid == null || uuid.isEmpty()) {
			this.dataTracker.set(USER, noUser);
		} else {
			this.dataTracker.set(USER, uuid);
		}
	}

	public void setUserName(String name) {
		this.dataTracker.set(USERNAME, name == null || name.isEmpty() ? "steve" : name);
	}

	/**
	 * 服务端 tick：宿主不满足"戴平衡头盔且 lord+open"（切回模式/摘盔/死亡/退出/换维度）
	 * 时立即自毁；满足时每 tick 跟随玩家位置并同步朝向。
	 * 实体位置放在玩家背后 0.5 格（沿玩家身体朝向 bodyYaw 反方向）——该偏移仅用于实体渲染排序：
	 * 第三人称背视角翅膀实体比玩家离相机更近 → 排序在玩家之后渲染 → 覆盖身体与披风。	 * 正视角翅膀实体更远 → 玩家先画 → 翅膀仍被身体自然遮挡。渲染时由
	 * RenderLordLuWing 把矩阵修正回玩家位置，模型绘制位置与 ArmorRenderer 时期一致。
	 */
	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient) {
			return;
		}
		PlayerEntity owner = getOwner();
		if (owner == null || !owner.isAlive()) {
			this.discard();
			return;
		}
		ItemStack helm = owner.getEquippedStack(EquipmentSlot.HEAD);
		if (helm.getItem() != ItemLoader.blanceHelmet
				|| !ItemBlancedHelmet.isLord(helm) || !ItemBlancedHelmet.isOpen(helm)) {
			this.discard();
			return;
		}
		// 用身体朝向（bodyYaw）而非视线 yaw：玩家原地转视角（转头）时身体不转，
		// 翅膀不跟着漂移；仅当身体真正转向时翅膀随身体转向。渲染端同口径取 bodyYaw。
		double yawRad = Math.toRadians(owner.getBodyYaw());
		double nx = -Math.sin(yawRad);
		double nz = Math.cos(yawRad);
		this.setPosition(owner.getX() - nx * 0.5D, owner.getY(), owner.getZ() - nz * 0.5D);
		this.setYaw(owner.getBodyYaw());
		this.setHeadYaw(owner.getBodyYaw());
		this.setBodyYaw(owner.getBodyYaw());
	}
}
