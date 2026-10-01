package org.huajiager.stand.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.huajiager.capability.StandHandler;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.network.StandNetWorkHandler;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.helper.TimeStopHelper;
import org.huajiager.stand.messages.MessageTimeStopFilterSync;
import org.huajiager.stand.messages.SyncStandChargeMessage;
import org.huajiager.util.NBTHelper;
import org.huajiager.attachment.Attachments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * THE_WORLD 时停消费链（服务端权威），
 * 。
 *
 *  通过每个实体的 {@code LivingUpdateEvent} 做两件事：
 * <ol>
 *   <li>{@code onTheWorld}：发动者身上的 {@code THE_WORLD} 倒计时递减，并按
 *       {@code TIME_STOP_RANGE}（默认 100）扫描范围内实体，把 {TIME_STOP} 打在目标上，
 *       冻结箭矢/火球/TNT/潜影弹（速度清零、物品无重力悬浮），最后一 tick 恢复原运动；</li>
 *   <li>{@code onTimeStop}：被冻结目标的 {@code TIME_STOP} 递减；非玩家实体完全冻结
 *       （速度清零 + 无重力 + Mob 关闭 AI）；受影响玩家锁定出生位置 + 致盲；末尾结算
 *       {@code DIO_FLAG}/{@code DIO_HIT} 时停拳标记。</li>
 * </ol>
 *
 * Fabric 1.20.1 没有全局 LivingUpdateEvent，本类以
 * {@link ServerTickEvents#END_SERVER_TICK} 每个世界遍历全部实体一次，等价承接上述两段
 * 逻辑（每实体先按“发动者”处理 THE_WORLD，再按“被冻结目标”处理 TIME_STOP）。
 *
 * 额外：发动者每 tick 通过 {@link SyncStandChargeMessage} 把 {@code buffTag=TIME_STOP}
 * 与剩余时间推送客户端，驱动 {@code EventTimeStopView}（时停视觉）与原有
 * {@code StandPowerHelper/StateXxx} 的 TIME_STOP 时停拳分支。
 */
public final class EventTimeStop {

	private static final String POS_X = "huajiager.time_stop.x";
	private static final String POS_Y = "huajiager.time_stop.y";
	private static final String POS_Z = "huajiager.time_stop.z";
	private static final String V_X = "huajiager.v_x";
	private static final String V_Y = "huajiager.v_y";
	private static final String V_Z = "huajiager.v_z";
	private static final String YAW = "huajiager.time_stop.yaw";
	private static final String PITCH = "huajiager.time_stop.pitch";
	private static final String MOTION_SAVED = "huajiager.time_stop.saved";

	/**
	 * 时停内累计的延迟伤害登记表（{@code 目标UUID -> 累计伤害+施力者}）。
	 *  DIO_HIT 语义：时停期间命中的伤害不实时结算，等目标时停解除（TIME_STOP 归零）时一次性释放。	 * 不管谁打的、打了多少拳，都在目标身上累计，结束时统一结算，击退/伤害源以最后一位施力者为准。
	 * 说明：目标在时停中死亡/被移除时该条目不结算、留在表内由 HashMap 天然兜底（体量受时停窗口限制，可忽略）。
	 */
	private static final Map<UUID, PendingPunch> PENDING_PUNCHES = new HashMap<>();

	private record PendingPunch(float amount, UUID sourceUuid) {
	}

	/**
	 * 时停内累计的延迟火焰登记表（{@code 目标UUID -> 累计点燃秒数}）。
	 * 时停中对冻结目标施加的火焰（火焰附加等 setOnFireFor 入口）不实时点燃，
	 * 由 {@link LivingEntityTimeStopMixin} 登记到本表；目标时停解除（TIME_STOP 归零）
	 * 时一次性点燃，时长完整保留——避免时停期间 fireTicks 照常递减、时停结束后火焰已烧完。
	 */
	private static final Map<UUID, Integer> PENDING_FIRES = new HashMap<>();

	/**
	 * 延迟启动的时停登记表（玩家UUID -> 剩余tick + 时停总时长）。
	 * 吃 Dio 面包链路现在"先播开场音、到点再时停"：开场音随机四选一，按各自音效时长
	 * 换算延迟 tick 后由本队列倒计时，归零时对该玩家执行 setTimeStop。
	 */
	private static final Map<UUID, DelayedStart> DELAYED_STARTS = new HashMap<>();

	private record DelayedStart(int ticksLeft, int duration, boolean applyPotion) {
	}

	/**
	 * 供 Dio 面包食用链路：默认时停总时长 THE_WORLD_TIME，并施加面包 5 药。
	 */
	public static void scheduleDelayedTimeStop(PlayerEntity player, int delayTicks) {
		scheduleDelayedTimeStop(player, delayTicks, HuajiConstant.Tags.THE_WORLD_TIME, true);
	}

	/**
	 * 安排一个延迟启动的时停：{@code delayTicks} 归零后对玩家执行时停（<=0 立即触发）。
	 * {@code duration} 为时停总时长；{@code applyPotion} 控制是否兼施面包 5 药
	 * （THE_WORLD 技能时停不给药，沿用技能自身的 extraEffects 语义）。
	 * 供 ItemDioBread / StandTheWorld 共用：先播放开场音，音效播放到指定时长后再冻结世界。
	 */
	/**
	 * 时停真正激活时用药（Dio 面包专属）：夜视/力量/速度/跳跃/再生 5 种强化 + 回血 5 点。
	 * 时长取 {@code duration}（=时停总长），与时停同 tick 施加，可精确覆盖整个时停期间。
	 * 食下面包瞬间不再给药，统一收敛到本方法（两处触发点）避免重复。
	 */
	private static void applyTimeStopPotions(ServerPlayerEntity player, int duration) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 4));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 6));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 4));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 2));
		player.heal(5.0f);
	}

	public static void scheduleDelayedTimeStop(PlayerEntity player, int delayTicks, int duration, boolean applyPotion) {
		if (delayTicks <= 0) {
			ServerPlayerEntity sp = (ServerPlayerEntity) player;
			TimeStopHelper.setTimeStop(sp, duration);
			if (applyPotion) {
				applyTimeStopPotions(sp, duration);
			}
			return;
		}
		DELAYED_STARTS.put(player.getUuid(), new DelayedStart(delayTicks, duration, applyPotion));
	}

	/** 每 tick 递减延迟时停队列，归零后对对应玩家实际触发时停。 */
	private static void tickDelayedStarts(MinecraftServer server) {
		if (DELAYED_STARTS.isEmpty()) {
			return;
		}
		var it = DELAYED_STARTS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, DelayedStart> entry = it.next();
			DelayedStart cur = entry.getValue();
			int left = cur.ticksLeft() - 1;
			if (left > 0) {
				entry.setValue(new DelayedStart(left, cur.duration(), cur.applyPotion()));
				continue;
			}
			it.remove();
			ServerPlayerEntity sp = server.getPlayerManager().getPlayer(entry.getKey());
			if (sp != null && !sp.isDead()) {
				TimeStopHelper.setTimeStop(sp, cur.duration());
				if (cur.applyPotion()) {
					applyTimeStopPotions(sp, cur.duration());
				}
			}
		}
	}

	/** 时停中命中目标：不立即结算，把伤害登记进延迟结算表（累计伤害）。
	 *
	 * @param source 施力者（拳击发动者）
	 * @param target 被冻结的时停目标
	 * @param damage 本次拳击伤害（替身 getDamage）
	 */
	public static void registerPendingPunch(Entity source, LivingEntity target, float damage) {
		if (damage <= 0 || target.isDead()) {
			return;
		}
		PendingPunch prev = PENDING_PUNCHES.get(target.getUuid());
		float total = damage + (prev == null ? 0 : prev.amount());
		PENDING_PUNCHES.put(target.getUuid(), new PendingPunch(total, source.getUuid()));
	}

	/**
	 * 时停中对冻结目标施加火焰：不实时点燃，登记进延迟火焰表（累计秒数）。
	 *
	 * @param target  被冻结的时停目标
	 * @param seconds 本次请求点燃的秒数（火焰附加等级等）
	 */
	public static void registerPendingFire(LivingEntity target, int seconds) {
		if (seconds <= 0 || target.isDead()) {
			return;
		}
		PENDING_FIRES.merge(target.getUuid(), seconds, Integer::sum);
	}

	/** 时停结束（目标 TIME_STOP 归零）：把登记的延迟火焰一次性点燃，时长完整保留。 */
	private static void settlePendingFires(Entity target) {
		Integer total = PENDING_FIRES.remove(target.getUuid());
		if (total == null || total <= 0) {
			return;
		}
		if (target instanceof LivingEntity le && !le.isDead()) {
			// 此刻 TIME_STOP 已归零，setOnFireFor 不再被 mixin 拦截，正常点燃
			le.setOnFireFor(total);
		}
	}

	/** 时停结束（目标 TIME_STOP 归零）：把登记的所有延迟伤害一次性结算成真实伤害。 */
	private static void settlePendingPunches(Entity target) {
		PendingPunch p = PENDING_PUNCHES.remove(target.getUuid());
		if (p == null) {
			return;
		}
		if (!(target.getWorld() instanceof ServerWorld world)) {
			return;
		}
		target.damage(DamageLoader.dioHit(target), p.amount());
	}

	private EventTimeStop() {
	}

	public static void register() {
		// 时停中退出重进世界：TIME_STOP 计数在非持久化 ENTITY_DATA 中随世界卸载丢失，
		// 但冻结写入的 NoGravity=true 会随区块持久化残留，且 onTimeStop 归零释放逻辑
		// 不再触发，实体将永久悬浮。实体加载时若带时停冻结标记，统一恢复原生状态。
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (world.isClient()) {
				return;
			}
			Boolean frozen = entity.getAttached(Attachments.TIME_STOP_FROZEN);
			if (Boolean.TRUE.equals(frozen)) {
				entity.setNoGravity(false);
				if (entity instanceof MobEntity mob) {
					mob.setAiDisabled(false);
				}
				entity.setAttached(Attachments.TIME_STOP_FROZEN, Boolean.FALSE);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickDelayedStarts(server);
			for (ServerWorld world : server.getWorlds()) {
				tickWorld(world);
			}
		});
	}

	private static void tickWorld(ServerWorld world) {
		for (Entity e : world.iterateEntities()) {
			// 1) 发动者链条：THE_WORLD 倒计时 + 范围冻结
			if (NBTHelper.getEntityInteger(e, HuajiConstant.Tags.THE_WORLD) > 0) {
				onTheWorld(e);
			}
			// 2) 被冻结目标链条：TIME_STOP 递减 + 冻结/释放
			if (NBTHelper.getEntityInteger(e, HuajiConstant.Tags.TIME_STOP) > 0) {
				onTimeStop(e);
			}
		}
	}

	/**
	 * 对应 {@code EventTimeStop.onTheWorld}：发动者倒计时递减、范围内打 TIME_STOP、
	 * 冻结运动实体；最后一 tick 恢复原运动。
	 */
	private static void onTheWorld(Entity eater) {
		int t = NBTHelper.getEntityInteger(eater, HuajiConstant.Tags.THE_WORLD);
		if (t <= 0) {
			return;
		}
		NBTHelper.setEntityInteger(eater, HuajiConstant.Tags.THE_WORLD, t - 1);

		int range = (int) NBTHelper.getEntityDouble(eater, HuajiConstant.Tags.TIME_STOP_RANGE);
		if (range <= 0) {
			range = 100;
		}
		Box box = eater.getBoundingBox().expand(range);
		List<Entity> targets = eater.getWorld().getOtherEntities(eater, box);
		for (Entity i : targets) {
			if (i instanceof ProjectileEntity || i instanceof TntEntity || i instanceof ShulkerBulletEntity) {
				freezeMovingEntity(i, t);
			} else if (i instanceof ItemEntity) {
				freezeMovingEntity(i, t);
			} else if (i instanceof LivingEntity) {
				// 受影响玩家统一收时停滤镜同步（含镇魂曲豁免玩家）：滤镜是"世界被时停"的
				// 视觉表现，即使镇魂曲玩家免疫冻结（不打 TIME_STOP / 不被锁定致盲），
				// 也应看到时停滤镜；此包只写 buffer/buffTag 不碰能量（见 syncFilterTo）。
				if (i instanceof PlayerEntity affected) {
					syncFilterTo(affected, t - 1);
				}
				// 镇魂曲免疫时停：持有 potionRequiem 效果的玩家直接跳过，不打 TIME_STOP。
				// 语义链：镇魂曲替身开技能 / 镇魂曲物品死亡后获得 potionRequiem →
				// 期间免疫 THE_WORLD/白金之星时停（阻断别人的时停）。				// 仅持有物品或替身但未触发效果则不免疫，照常被冻结。
				// 歌词反馈由客户端 EventOrgaRequiemClient（potionRequiem 首次出现）统一承担。
				if (i instanceof PlayerEntity requiemPlayer
						&& requiemPlayer.hasStatusEffect(PotionLoader.potionRequiem)) {
					continue;
				}
				// 仅对尚未进入时停的目标打标记
				if (NBTHelper.getEntityInteger(i, HuajiConstant.Tags.TIME_STOP) == 0) {
					NBTHelper.setEntityInteger(i, HuajiConstant.Tags.TIME_STOP, t);
					saveMotionAndPos(i);
					// 时停目标（THE_WORLD 冻结玩家）施加虚弱 255 级（amplifier 254 = 等级 255）：
					// 打标瞬间即生效，duration 覆盖剩余时停时长；后续每 tick 由
					// onTimeStop 的玩家分支保持覆盖。
					if (i instanceof LivingEntity stopTarget) {
						stopTarget.addStatusEffect(
								new StatusEffectInstance(StatusEffects.WEAKNESS, Math.max(t, 20), 254));
					}
				}
			}
		}

		// 发动者剩余时间推送客户端（驱动时停视觉 / 清空）
		if (eater instanceof ServerPlayerEntity sp) {
			syncEater(sp, t - 1);
		}

		// 倒计时归零后回收异常的大范围设定
		if (t - 1 == 0) {
			double r = NBTHelper.getEntityDouble(eater, HuajiConstant.Tags.TIME_STOP_RANGE);
			if (r > 50) {
				NBTHelper.setEntityDouble(eater, HuajiConstant.Tags.TIME_STOP_RANGE, 50);
			}
			// 时停结束必须复位发动者的 StandHandler.buffer/buffTag（TIME_STOP 与剩余 tick
			// 只在 setTimeStop 写入、以往无任何清空点）。否则 EventStandCharge 每 5 tick 的
			// 常驻回充广播会继续把残留的 TIME_STOP/180 推给客户端（拥有替身时必有），
			// 客户端 isTimeStopActive 误判"新一次时停开始"，重播开场反色/灰色滤镜，
			// 表现即为"反色与灰色跑到时停结束之后才出现"。
			if (eater instanceof PlayerEntity pe) {
				StandHandler h = StandUtil.getStandHandler(pe);
				if (h != null) {
					h.setBuffer(0);
					h.setBuffTag("");
				}
			}
		}
	}

	/**
	 * 对应 {@code EventTimeStop.onTimeStop}：被冻结目标递减 TIME_STOP，
	 * 非玩家冻结/释放，玩家锁定 + 致盲，并结算 DIO 标记。
	 */
	private static void onTimeStop(Entity target) {
		int t = NBTHelper.getEntityInteger(target, HuajiConstant.Tags.TIME_STOP);
		if (t <= 0) {
			return;
		}

		// 拳击标记递减
		if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_FLAG) > 0) {
			NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_FLAG,
					NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_FLAG) - 1);
		}

		NBTHelper.setEntityInteger(target, HuajiConstant.Tags.TIME_STOP, t - 1);
		boolean released = (t - 1 == 0);

		if (target instanceof PlayerEntity player) {
			// 被时停玩家（THE_WORLD 时停目标）一律施加虚弱 255 级
			// （amplifier 254 = 等级 255）：duration 取剩余时停时长（≥20 保底），
			// 使虚弱覆盖整个时停窗口；不受 allowTimeStopPlayer 开关影响。
			if (player.getStatusEffect(StatusEffects.WEAKNESS) == null) {
				player.addStatusEffect(
						new StatusEffectInstance(StatusEffects.WEAKNESS, Math.max(t, 20), 254));
			}
			// 时间停止中受影响的其他玩家：锁定出生位置 + 致盲（配置 allowTimeStopPlayer）
			if (ConfigHuaji.Stands.allowTimeStopPlayer
					&& NBTHelper.getEntityInteger(player, HuajiConstant.Tags.THE_WORLD) <= 0) {
				double px = NBTHelper.getEntityDouble(player, POS_X);
				double py = NBTHelper.getEntityDouble(player, POS_Y);
				double pz = NBTHelper.getEntityDouble(player, POS_Z);
				Vec3d pos = new Vec3d(px, py, pz);
				if (!player.getPos().equals(pos)) {
					player.requestTeleport(px, py, pz);
				}
				if (player.getStatusEffect(StatusEffects.BLINDNESS) == null) {
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, Math.max(t, 20), 0));
				}
			}
		} else if (t > 1) {
			// 非玩家：冻结（速度清零 + 无重力悬浮 + Mob 关闭 AI），等效取消实体更新
			target.setVelocity(Vec3d.ZERO);
			target.setNoGravity(true);
			target.setAttached(Attachments.TIME_STOP_FROZEN, Boolean.TRUE);
			if (target instanceof MobEntity mob) {
				mob.setAiDisabled(true);
			}
		} else {
			// t == 1 最后一 tick：解除冻结（TIME_STOP 已归 0，世界时间恢复）
			releaseFreeze(target);
		}

		// 时停结束：把时停期间登记的延迟拳击伤害一次性结算，
		// 并补上延迟火焰（时停中施加的火焰附加等，时长完整保留）
		if (released) {
			settlePendingPunches(target);
			settlePendingFires(target);
		}

		// DIO_HIT 结算：在此按 DIO_HIT_EXTRA 造成时停拳伤害。		// Fabric 端伤害额依赖替身拳击链路（StandPowerHelper 已读 TIME_STOP 打 DIO_HIT），
		// 实际伤害已由上面 settlePendingPunches 统一释放，此处保留递减与累计清零（视觉标记收敛）。
		if (NBTHelper.getEntityInteger(target, HuajiConstant.Tags.TIME_STOP) == 0
				&& NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) > 0) {
			NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT,
					NBTHelper.getEntityInteger(target, HuajiConstant.Tags.DIO_HIT) - 1);
			NBTHelper.setEntityInteger(target, HuajiConstant.Tags.DIO_HIT_EXTRA, 0);
		}
	}

	/**
	 * 竞技类运动实体（箭矢/火球/TNT/潜影弹/掉落物）冻结：首 tick 存档速度，
	 * 冻结期速度归零 + 无重力，最后一 tick 恢复原运动。
	 */
	private static void freezeMovingEntity(Entity e, int t) {
		boolean isNew = NBTHelper.getEntityInteger(e, HuajiConstant.Tags.TIME_STOP) == 0;
		if (isNew) {
			saveMotionAndPos(e);
			NBTHelper.setEntityInteger(e, HuajiConstant.Tags.TIME_STOP, t);
		}
		if (t > 1) {
			e.setVelocity(Vec3d.ZERO);
			e.setNoGravity(true);
			e.setAttached(Attachments.TIME_STOP_FROZEN, Boolean.TRUE);
		} else if (t == 1) {
			restoreMotionAndRelease(e);
		}
	}

	/** 解除冻结：恢复重力 / AI / 存档速度，清累计伤害标签。 */
	private static void releaseFreeze(Entity e) {
		e.setNoGravity(false);
		e.setAttached(Attachments.TIME_STOP_FROZEN, Boolean.FALSE);
		if (e instanceof MobEntity mob) {
			mob.setAiDisabled(false);
		}
		restoreMotionAndRelease(e);
	}

	private static void restoreMotionAndRelease(Entity e) {
		// 仅当本次冻结确实存档过运动状态才恢复，避免把未参与该次冻结的实体运动覆盖掉。
		// 恢复时无条件还原存档速度与朝向角（即使原速度为 0）：确保箭矢/火球/潜影弹等
		// 投影物恢复后按各自原速度与方向飞行，不因"速度分量恰为 0 跳过覆盖"而坍缩成统一朝向。
		boolean saved = NBTHelper.getEntityBoolean(e, MOTION_SAVED);
		if (saved) {
			double vx = NBTHelper.getEntityDouble(e, V_X);
			double vy = NBTHelper.getEntityDouble(e, V_Y);
			double vz = NBTHelper.getEntityDouble(e, V_Z);
			e.setVelocity(vx, vy, vz);
			e.setYaw(NBTHelper.getEntityFloat(e, YAW));
			e.setPitch(NBTHelper.getEntityFloat(e, PITCH));
			NBTHelper.setEntityBoolean(e, MOTION_SAVED, false);
		}
		e.setAttached(Attachments.TIME_STOP_FROZEN, Boolean.FALSE);
		NBTHelper.setEntityInteger(e, HuajiConstant.Tags.DIO_HIT_EXTRA, 0);
	}

	/** 冻结时存档速度 / 朝向角 / 玩家坐标（key 保持一致），并打上"已存档"标记。 */
	private static void saveMotionAndPos(Entity e) {
		Vec3d v = e.getVelocity();
		NBTHelper.setEntityDouble(e, V_X, v.x);
		NBTHelper.setEntityDouble(e, V_Y, v.y);
		NBTHelper.setEntityDouble(e, V_Z, v.z);
		NBTHelper.setEntityFloat(e, YAW, e.getYaw());
		NBTHelper.setEntityFloat(e, PITCH, e.getPitch());
		NBTHelper.setEntityBoolean(e, MOTION_SAVED, true);
		if (e instanceof PlayerEntity p) {
			Vec3d pos = p.getPos();
			NBTHelper.setEntityDouble(p, POS_X, pos.x);
			NBTHelper.setEntityDouble(p, POS_Y, pos.y);
			NBTHelper.setEntityDouble(p, POS_Z, pos.z);
		}
	}

	/**
	 * 把时停剩余 tick 同步给范围内受影响玩家（发动者之外的被冻结玩家 + 镇魂曲豁免玩家）。
	 * 走轻量 {@link MessageTimeStopFilterSync}：客户端只写本地 StandHandler 的
	 * buffer/buffTag 驱动滤镜（EventTimeStopView / TimeStopPostShader），不触碰
	 * charge/max，避免把发动者能量污染到受影响玩家身上。
	 * 时停归零时发 remaining=0，客户端 buffTag 变空、滤镜自动卸载（与发动者 syncEater 对称）。
	 */
	private static void syncFilterTo(PlayerEntity target, int remaining) {
		if (!(target instanceof ServerPlayerEntity sp)) {
			return;
		}
		StandNetWorkHandler.sendTo(sp, new MessageTimeStopFilterSync(Math.max(remaining, 0)));
	}

	/** 把发动者当前替身能量 + 时停剩余时间 + buffTag 同步到客户端。 */
	private static void syncEater(ServerPlayerEntity sp, int remaining) {
		StandHandler h = StandUtil.getStandHandler(sp);
		int charge = h == null ? 0 : h.getChargeValue();
		int max = h == null ? 1 : h.getMaxValue();
		int buffer = Math.max(remaining, 0);
		String tag = buffer > 0 ? HuajiConstant.BuffTags.TIME_STOP : "";
		StandNetWorkHandler.sendTo(sp, new SyncStandChargeMessage(charge, max, buffer, tag, false));
	}
}
