package org.huajiager.entity;

import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemDiscCommand;
import org.huajiager.util.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 命令飞盘实体， 。
 *
 * 继承 EntityThrowable；Fabric 1.20.1 对应 ThrownEntity（投掷运动/重力/摩擦/
 * 碰撞分发均内置于 tick），投掷后按主 UUID 存活校验并维持 180 tick 生命周期，
 * 命中实体按命令类型触发效果，命中方块落回可拾取物品。
 */
public class EntityDiscCommand extends ThrownEntity {

	/** 瞬态弹体不持久化：避免写入存档，防止残留实体在下次进图时批量加载拖累服务端 tick。 */
	@Override
	public boolean shouldSave() {
		return false;
	}

	private static final String TAG_LIFE = "life";
	private static final String TAG_COMMAND = "command";
	private static final String TAG_MASTER = "master";

	private static final TrackedData<Float> LIFE = DataTracker.registerData(EntityDiscCommand.class,
			TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<String> COMMAND = DataTracker.registerData(EntityDiscCommand.class,
			TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> MASTER = DataTracker.registerData(EntityDiscCommand.class,
			TrackedDataHandlerRegistry.STRING);

	public static final EntityType<EntityDiscCommand> TYPE = EntityType.Builder
			.<EntityDiscCommand>create((type, world) -> new EntityDiscCommand(type, world), SpawnGroup.MISC)
			.setDimensions(0.25f, 0.25f)
			.build("huajiager:disc_command");

	public EntityDiscCommand(EntityType<EntityDiscCommand> type, World world) {
		super(type, world);
	}

	public EntityDiscCommand(World world) {
		this(TYPE, world);
	}

	@Override
	protected void initDataTracker() {
		DataTracker tracker = this.getDataTracker();
		tracker.startTracking(LIFE, 0F);
		tracker.startTracking(COMMAND, "null");
		tracker.startTracking(MASTER, "acfd894c-ad88-4b34-addf-a8d10e2a67f7");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat(TAG_LIFE, getLife());
		nbt.putString(TAG_COMMAND, getCommand());
		nbt.putString(TAG_MASTER, getMaster());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setLife(nbt.getFloat(TAG_LIFE));
		setCommand(nbt.getString(TAG_COMMAND));
		setMaster(nbt.getString(TAG_MASTER));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved()) {
			// ThrownEntity.tick 已包含位移/重力/碰撞分发，碰撞命中可能已 discard
			return;
		}
		PlayerEntity player = findMasterPlayer();
		if (player == null) {
			this.discard();
			return;
		}
		if (getLife() < 180f) {
			setLife(getLife() + 1f);
		} else {
			this.discard();
		}
	}

	private PlayerEntity findMasterPlayer() {
		for (PlayerEntity p : this.getWorld().getPlayers()) {
			if (p.getUuid().toString().equals(getMaster())) {
				return p;
			}
		}
		return null;
	}

	@Override
	protected void onCollision(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			Entity entityHit = ((EntityHitResult) result).getEntity();
			boolean isThrower = entityHit.getUuid().toString().equals(getMaster());
			if (entityHit instanceof LivingEntity && !isThrower) {
				if (!this.getWorld().isClient) {
					String type = getCommand();
					if (type != null) {
						switch (type) {
							case "explosion":
								this.getWorld().createExplosion(this, getX(), getY(), getZ(), 1f, false,
										World.ExplosionSourceType.NONE);
								entityHit.damage(this.getWorld().getDamageSources().generic(), 20f);
								break;
							case "move_up":
								entityHit.setVelocity(entityHit.getVelocity().x, 3f, entityHit.getVelocity().z);
								break;
							case "self_attack":
								entityHit.damage(DamageLoader.selfAttack(entityHit),
										3f + ((LivingEntity) entityHit).getMaxHealth() / 3);
								break;
						}
						this.discard();
					}
				}
				this.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1f);
			}
		}
		if (result.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) result).getBlockPos();
			BlockState state = this.getWorld().getBlockState(pos);
			if (!state.getCollisionShape(this.getWorld(), pos).isEmpty()) {
				ItemStack stack = new ItemStack(ItemLoader.discCommand);
				PlayerEntity player = findMasterPlayer();
				ItemDiscCommand.setCommandType(stack, getCommand());
				if (player != null) {
					ItemDiscCommand.setOwner(stack, player.getName().getString(), getMaster());
				}
				if (!this.getWorld().isClient) {
					ItemEntity itemEntity = new ItemEntity(this.getWorld(), getX(), getY() + 0.5f, getZ(), stack);
					this.getWorld().spawnEntity(itemEntity);
				}
				this.discard();
			}
		}
	}

	public float getLife() {
		return this.getDataTracker().get(LIFE);
	}

	public void setLife(float timeTick) {
		this.getDataTracker().set(LIFE, timeTick);
	}

	public String getCommand() {
		return this.getDataTracker().get(COMMAND);
	}

	public void setCommand(String command) {
		this.getDataTracker().set(COMMAND, command);
	}

	public String getMaster() {
		return this.getDataTracker().get(MASTER);
	}

	public void setMaster(String uuid) {
		this.getDataTracker().set(MASTER, uuid);
	}
}
