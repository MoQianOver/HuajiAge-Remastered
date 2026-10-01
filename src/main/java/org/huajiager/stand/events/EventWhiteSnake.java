package org.huajiager.stand.events;

import org.huajiager.api.IStandState;
import org.huajiager.capability.IExposedData;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.item.ItemDiscMemory;
import org.huajiager.item.ItemDiscMind;
import org.huajiager.item.ItemDiscStand;
import org.huajiager.stand.EnumStandTag;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.util.NBTHelper;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

/**
 * 白蛇心智抽取事件（ ）。
 *
 * <p>两段逻辑：
 * <ul>
 *   <li>{@code onOverdriveAttack}（LivingAttackEvent）：替身状态带 disc_deprive 标签（白蛇
 *       default/punch）且距离 ≤ 5 时，攻击命中目标可抽取心智 DISC：
 *       非替身使者掉落心智碟+记忆碟；替身使者（阶段 ≤ 己方）掉落替身碟+记忆碟并清空替身。 *       已抽取过则提示 fail；阶段不足提示 stand_block；命中播放白蛇 hit 随机音。</li>
 *   <li>{@code onDiscDeprive}（LivingUpdateEvent）：被打上 disc_deprive 标志的生物持续
 *       施加凋零+反胃，每 20 tick 受到最大生命 1/10 的绝对伤害。</li>
 * </ul>
 *
 * <p>Fabric 1.20.1 映射：
 * <ul>
 *   <li>LivingAttackEvent（玩家本体攻击）→ {@link AttackEntityCallback}，仅服务端结算。 *       客户端分支（消息/音效）统一由服务端 sendMessage/playSound 同步。</li>
 *   <li>LivingUpdateEvent → {@link ServerTickEvents#END_SERVER_TICK} 遍历全实体检查
 *       disc_deprive NBT 标志，与 EventOrgaRequiemHit / EventPlayerFlying 同模式。</li>
 * </ul>
 */
public final class EventWhiteSnake {

	/**
	 * 本次被夺掉落的碟（ItemStack NBT）上记录的"被夺者 UUID"标记 key。
	 * 拾取拦截见 {@link org.huajiager.mixin.ItemEntityDeprivePickupMixin}：
	 * 被夺者本人不可拾取，只有记录在 {@link #TAG_DEPRIVE_TAKER} 的白蛇使用者
	 * 可拾取；白蛇使用者捡走后标记解除，转手后即可正常拾取。
	 */
	public static final String TAG_DEPRIVE_OWNER = "deprive_owner";

	/**
	 * 本次被夺掉落的碟（ItemStack NBT）上记录的"白蛇使用者（夺取者）UUID"标记 key。
	 * 拾取拦截见 {@link org.huajiager.mixin.ItemEntityDeprivePickupMixin}：
	 * 除被夺者本人外，非白蛇使用者的其他玩家同样不可拾取，仅白蛇使用者放行。
	 */
	public static final String TAG_DEPRIVE_TAKER = "deprive_taker";

	private EventWhiteSnake() {
	}

	public static void register() {
		// 攻击命中（玩家本体攻击，白蛇 default/punch 状态均可）：尝试抽取心智 DISC
		AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
			if (world.isClient) {
				return ActionResult.PASS;
			}
			if (target instanceof LivingEntity living) {
				tryDepriveDisc(player, living);
			}
			return ActionResult.PASS;
		});

		// 被夺生物持续掉血：每 tick 遍历全实体，命中 disc_deprive 标志则结算（对应 LivingUpdateEvent）
		// 标记为持久化 attachment（重生/重进保留，直到用心智碟或替身碟右键解除）
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (Entity entity : world.iterateEntities()) {
					if (entity instanceof LivingEntity living
							&& NBTHelper.getDiscDeprive(living)) {
						onDiscDepriveTick(living);
					}
				}
			}
		});

		// 方案二：重生后持续掉血/药水继续继承（不清除 disc_deprive），由被夺者用
		// 自己的心智碟或替身碟右键解除。此处仅处理"玩家类型记忆碟死亡强制掉落"：
		// 即使开启死亡不掉落（keepInventory），玩家类型记忆碟也必定掉落；生物/无类型不受影响。
		ServerLivingEntityEvents.AFTER_DEATH.register((living, damageSource) -> {
			if (living instanceof ServerPlayerEntity player) {
				dropPlayerMemoryDiscs(player);
			}
		});
	}

	/** 对应 onOverdriveAttack：白蛇抽取心智 DISC。 */
	private static void tryDepriveDisc(PlayerEntity attacker, LivingEntity living) {
		StandBase stand = StandUtil.getType(attacker);
		IExposedData data = StandUtil.getStandData(attacker);
		if (stand == null || data == null || attacker.getStatusEffect(PotionLoader.potionStand) == null) {
			return;
		}
		String state = StandUtil.getStandState(attacker);
		int stage = data.getStage();
		IStandState stateBase = StandStates.getStandState(stand.getName(), state);
		if (!(stateBase instanceof StandStateBase ssb)
				|| !ssb.hasExtraData(EnumStandTag.StateTags.DISC_DEPRIVE.getName())) {
			return;
		}
		// 目标未死且与攻击者距离 ≤ 5（HAMathHelper 块距离，此处用三维欧氏平方）
		if (!living.isAlive() || living.squaredDistanceTo(attacker) > 5 * 5) {
			return;
		}
		boolean isDeprived = NBTHelper.getDiscDeprive(living);
		if (!isDeprived) {
			// 被夺者替身数据必须读写同一真实 attachment 实例：getOrCreateStandData 落库，
			// 若用 getStandData(getAttached) 读未落库玩家会拿到临时实例，setTrigger 写不进实体，
			IExposedData dataHurt = StandUtil.getOrCreateStandData(living);
			ItemStack discMind = ItemDiscMind.getDiscMind(living);
			ItemStack discMemory = ItemDiscMemory.getDiscMemory(living);
			boolean isStandUser = dataHurt != null && !dataHurt.getStand().equals(StandLoader.EMPTY);
			if (!isStandUser) {
				// 非替身使者：掉落心智碟 + 记忆碟，标记剥夺
				living.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1f, 1f);
				NBTHelper.setDiscDeprive(living, true);
				// 标记必须在 dropStack 之前写入 ItemStack：掉落物实体的 stack 与传入引用共享，
				// 掉落后 40 tick 拾取延迟内标记必然就绪，且不依赖 dropStack 返回实体的二次打标。
				markDepriveStack(discMind, living, attacker);
				markDepriveStack(discMemory, living, attacker);
				markDepriveDrop(living.dropStack(discMind, 0.25f), living, attacker);
				markDepriveDrop(living.dropStack(discMemory, 0.25f), living, attacker);
				attacker.sendMessage(Text.translatable(
						"stand.huajiager.skill.huajiager.white_snake.mind_deprive.success", living.getName()));
				playSounds(living);
			} else {
				// 替身使者：阶段 ≤ 己方则抽走替身碟 + 记忆碟并清空替身数据，否则 stand_block
				// 夺取有替身的玩家/生物只掉替身碟 + 记忆碟，不掉心智碟；
				// 心智碟仅在夺取无替身目标时掉落（见上方非替身使者分支）。
				String hurtStand = dataHurt.getStand();
				int hurtStage = dataHurt.getStage();
				String hurtModel = dataHurt.getModel();
				if (!hurtStand.equals(StandLoader.EMPTY)) {
					if (hurtStage <= stage) {
						NBTHelper.setDiscDeprive(living, true);
						// 掉碟同时剥夺替身（对照原版：setTrigger(false)+setStand("empty")+setStage(0)）：
						// 1) 两碟预标记后再 dropStack；2) 清空被夺者替身数据；
						// 3) 立即移除被夺者替身展示实体（不依赖实体 tick 的心跳判定）；
						// 4) 被夺者为玩家时同步客户端替身数据（trigger=false/stand=empty 复位）。
						ItemStack discStack = ItemDiscStand.createDisc(
								new ItemStack(ItemLoader.discStand), hurtStand, hurtStage, hurtModel);
						ItemStack discMemoryCopy = ItemDiscMemory.getDiscMemory(living);
						markDepriveStack(discStack, living, attacker);
						markDepriveStack(discMemoryCopy, living, attacker);
						markDepriveDrop(living.dropStack(discStack, -0.5f), living, attacker);
						markDepriveDrop(living.dropStack(discMemoryCopy, -0.25f), living, attacker);
						dataHurt.setTrigger(false);
						dataHurt.setStand("empty");
						dataHurt.setStage(0);
						dataHurt.setState("default");
						dataHurt.setModel(HuajiConstant.StandModels.DEFAULT_MODEL_ID);
						removeStandEntity(living);
						if (living instanceof ServerPlayerEntity spVictim) {
							StandUtil.syncStandData(spVictim);
						}
						playSounds(living);
					} else {
						attacker.sendMessage(Text.translatable(
								"stand.huajiager.skill.huajiager.white_snake.mind_deprive.stand_block"));
					}
				}
			}
		} else {
			// 已抽取过：提示 fail
			attacker.sendMessage(Text.translatable(
					"stand.huajiager.skill.huajiager.white_snake.mind_deprive.fail"));
		}
	}

	/** 对应 onDiscDeprive：被夺生物持续凋零 + 每 20 tick 掉 1/10 最大生命。 */
	private static void onDiscDepriveTick(LivingEntity living) {
		if (!living.hasStatusEffect(StatusEffects.WITHER) && !living.hasStatusEffect(StatusEffects.NAUSEA)) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60));
		}
		if (living.getHealth() > 0 && living.age % 20 == 0) {
			living.damage(DamageLoader.discDeprive(living), living.getMaxHealth() / 10f);
		}
	}

	/**
	 * 在 dropStack 之前给待掉落 ItemStack 打上"被夺者 + 白蛇使用者"标记：
	 * 掉落物实体的 stack 与传入引用共享，标记在实体生成瞬间即存在，
	 * 不依赖 dropStack 返回后的二次写入（配合 {@link #markDepriveDrop} 双保险）。
	 */
	private static void markDepriveStack(ItemStack stack, LivingEntity victim, LivingEntity taker) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		NBTHelper.getTagCompoundSafe(stack).putString(TAG_DEPRIVE_OWNER, victim.getUuid().toString());
		NBTHelper.getTagCompoundSafe(stack).putString(TAG_DEPRIVE_TAKER, taker != null ? taker.getUuid().toString() : "");
	}

	/**
	 * 给本次被夺掉落的碟打上"被夺者 + 白蛇使用者"标记：
	 * 被夺者本人与非白蛇使用者均不可拾取，仅白蛇使用者可拾取（见 ItemEntityDeprivePickupMixin）。
	 */
	private static void markDepriveDrop(ItemEntity itemEntity, LivingEntity victim, LivingEntity taker) {
		if (itemEntity == null) {
			return;
		}
		markDepriveStack(itemEntity.getStack(), victim, taker);
	}

	/**
	 * 立即移除被夺者当前召唤的替身展示实体（user 匹配）。
	 * 对照原版：替身数据清空（setTrigger(false)）后替身消失；
	 * Fabric 端替身是独立实体，除 tick 心跳 discard 外，在此直接移除，
	 * 确保"掉碟瞬间替身即消失"，不依赖后续 tick。
	 */
	private static void removeStandEntity(LivingEntity living) {
		if (living.getWorld() instanceof ServerWorld sw) {
			sw.getEntitiesByType(EntityStandBase.TYPE_ENTITY, e -> e.getUser() == living)
					.forEach(Entity::discard);
		}
	}

	/** 玩家死亡时强制掉落玩家类型记忆碟：遍历主背包 + 副手，无视死亡不掉落（生物/无类型不受影响）。 */
	private static void dropPlayerMemoryDiscs(ServerPlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.main.size(); i++) {
			dropIfPlayerMemoryDisc(player, inv, i);
		}
		dropIfPlayerMemoryDisc(player, inv, 40);
	}

	private static void dropIfPlayerMemoryDisc(ServerPlayerEntity player, PlayerInventory inv, int slot) {
		ItemStack stack = inv.getStack(slot);
		if (stack.getItem() instanceof ItemDiscMemory && isPlayerMemoryDisc(stack)) {
			inv.removeStack(slot);
			player.dropItem(stack, false);
		}
	}

	/** 判断记忆碟是否为"玩家类型"：owner_type 记录的是玩家实体类名（玩家类名均含 player）。 */
	private static boolean isPlayerMemoryDisc(ItemStack stack) {
		return ItemDiscMemory.getOwnerType(stack).toLowerCase(java.util.Locale.ROOT).contains("player");
	}

	/** 对应 playSounds：随机播放白蛇命中音 hit_1/2/3。 */
	private static void playSounds(LivingEntity entity) {
		float i = entity.getRandom().nextFloat() * 10f;
		if (i < 3.3f) {
			entity.playSound(SoundLoader.STAND_WHITE_SNAKE_HIT_1, 1f, 1f);
		} else if (i < 6.6f) {
			entity.playSound(SoundLoader.STAND_WHITE_SNAKE_HIT_2, 1f, 1f);
		} else {
			entity.playSound(SoundLoader.STAND_WHITE_SNAKE_HIT_3, 1f, 1f);
		}
	}
}
