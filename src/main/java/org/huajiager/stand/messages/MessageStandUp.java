package org.huajiager.stand.messages;

import java.util.List;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.config.ConfigHuaji;
import org.huajiager.init.loaders.PotionLoader;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.EnumStandTag;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


/**
 * C2S：替身召唤收回消息，
 * 。
 * 载荷为是否移动（isMoving，移动时实体跟随展示）。
 */
public record MessageStandUp(boolean isMoving) implements FabricPacket {


	public static final PacketType<MessageStandUp> TYPE = PacketType.create(
			new Identifier("huajiager", "stand_up"), MessageStandUp::new);

	public MessageStandUp(PacketByteBuf buf) {
		this(buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBoolean(isMoving);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageStandUp payload, ServerPlayerEntity player, PacketSender responseSender) {
		// 必须用 getOrCreateStandData：STAND_DATA 为 createDefaulted attachment，
		// 用 getStandData(getAttached) 在未落库时每次返回新建临时实例，setTrigger/setState
		// 只写临时对象，触发状态永不持久——表现为「每次按键都被判定为未召唤、重复 spawn、
		// 旧实体不收回」。
		IExposedData data = StandUtil.getOrCreateStandData(player);
		StandBase stand = StandUtil.getType(player);
		StandHandler charge = StandUtil.getStandHandler(player);
		if (data == null || stand == null || charge == null) {
			return;
		}
		// 替身实体存在性检查（修复"替身死亡后丢失"）：
		// 替身实体可能因宿主死亡 / 登出 / 意外被移除而在 tick 中被 discard，
		// 此时 data.isTriggered() 仍为 true。若按原逻辑走"收回"分支，玩家按召唤键
		// 只会清掉触发状态、替身永远召唤不出来（必须再按一次且二次扣能量），
		// 表现即"替身死亡后丢失"。实体不在时一律视为重新召唤。
		boolean standEntityExists = !player.getServerWorld().getEntitiesByType(
				EntityStandBase.TYPE_ENTITY, e -> e.getUser() == player).isEmpty();
		// 召唤初始状态对齐： MessageStandUp 一律 data.setState(DEFAULT)。
		// 为满足"召唤默认闲置、按 I 切换才攻击"的需求，对注册了 IDLE 态的替身
		// （白金之星/世界/绿法皇）召唤进闲置态；杀手皇后仅 DEFAULT+PUNCH 两态
		// （无 IDLE），召唤必须直接进 DEFAULT（待机左前悬浮），按一次 I 即切连击(PUNCH)。
		// 若强行设 IDLE，KQ 的 states 列表不含 idle，渲染端会误判为背后闲置，
		// 且第一次切态会从列表头(default)开始，出现"召唤背后→按一次才到攻击"的错位。
		List<String> standStates = stand.getStates();
		boolean hasIdle = standStates != null
				&& standStates.contains(ExposedData.States.IDLE.getName());
		data.setState(hasIdle ? ExposedData.States.IDLE.getName()
				: ExposedData.States.DEFAULT.getName());
		if (!data.isTriggered() || !standEntityExists) {
			if (charge.canBeCost(1000)) {
				charge.setMaxValue(stand.getMaxMP());
				charge.cost(1000);
				player.addStatusEffect(new StatusEffectInstance(PotionLoader.potionStand, stand.getDuration()));
				data.setTrigger(true);
				data.setHandDisplay(stand.isHandDisplay());
				player.sendMessage(Text.translatable(stand.getLocalName()), false);
				if (payload.isMoving()) {
					EntityStandBase standBase = new EntityStandBase(EntityStandBase.TYPE_ENTITY, player.getWorld());
					standBase.setUser(player.getUuid().toString());
					standBase.setUserName(player.getGameProfile().getName());
					standBase.setPosition(player.getX(), player.getY(), player.getZ());
					standBase.setType(StandLoader.getStand(data.getStand()) != null ? data.getStand()
							: StandLoader.EMPTY);
					if (StandStates.getStandState(stand.getName(), data.getState()) instanceof StandStateBase base
							&& base.hasExtraData(EnumStandTag.StateTags.RIDE.getName())) {
						standBase.setEntity(true);
					}
					// 首帧状态对齐：DataTracker 初始值 DEFAULT（攻击态），spawn 包会把当前
					// DataTracker 全量发给客户端，若 spawn 后才 setStandState，客户端首帧
					// 仍按 DEFAULT 渲染闪现攻击态拳头（既有踩坑）。必须在 spawn 前写入目标态。
					standBase.setStandState(data.getState());
					player.getWorld().spawnEntity(standBase);
				}
				// 召唤状态变更后必须将 isTriggered=true 同步回发起者客户端。
				// 客户端 EventStandKey.performSkill/switchMode 以其判定是否发包，
				// 若不同步则客户端 data.isTriggered() 恒为 false，技能键永远不会发包。
				StandUtil.syncStandData(player);
				// 召唤音效：替身登场广播替身主题音效（服务端按维度广播，音量随距离衰减）。
				// 召唤音乐（standUp 的替身主题乐）走 playToServer → 服务端广播链路，
				// 本实现收敛为服务端世界广播 THE_WORLD 主题音效作为召唤亮相音。
				// 召唤音按替身名分发：白金之星用专属召唤音，绿法皇用 hierophant_green_stand_up，
				// 其余替身（THE_WORLD 及其它尚未录制召唤音的替身）统一回落 THE_WORLD_STAND_UP。
				net.minecraft.sound.SoundEvent standUpSound;
				if (StandLoader.STAR_PLATINUM.getName().equals(stand.getName())) {
					standUpSound = SoundLoader.STAR_PLATINUM_STAND_UP;
				} else if (StandLoader.HIEROPHANT_GREEN.getName().equals(stand.getName())) {
					standUpSound = SoundLoader.STAND_HIEROPHANT_GREEN_STAND_UP;
				} else if (StandLoader.KILLER_QUEEN.getName().equals(stand.getName())) {
					standUpSound = player.getRandom().nextBoolean()
							? SoundLoader.STAND_KILLER_QUEEN_SHOW_1
							: SoundLoader.STAND_KILLER_QUEEN_SHOW_2;
				} else if (StandLoader.ORGA_REQUIEM.getName().equals(stand.getName())) {
					standUpSound = SoundLoader.ORGA_REQUIEM_2;
				} else if ("crazy_diamond".equals(stand.getName())
						|| "huajiager:crazy_diamond".equals(stand.getName())) {
					standUpSound = SoundLoader.STAND_CRAZY_DIAMOND_STAND_UP;
				} else if ("hermit_purple".equals(stand.getName())
						|| "huajiager:hermit_purple".equals(stand.getName())) {
					// 隐者之紫召唤音：分组 stand_hermit_purple_1-2 随机二选一
					// （用户指定，见 hermit_purple.json sounds 列表前两项）
					standUpSound = player.getRandom().nextBoolean()
							? SoundLoader.STAND_HERMIT_PURPLE_1
							: SoundLoader.STAND_HERMIT_PURPLE_2;
				} else if ("white_snake".equals(stand.getName())
						|| "huajiager:white_snake".equals(stand.getName())) {
					// 白蛇召唤音：分组 stand_white_snake_1-3 随机三选一
					// （用户指定，见 white_snake.json sounds 列表前三项）
					switch (player.getRandom().nextInt(3)) {
					case 0:
						standUpSound = SoundLoader.STAND_WHITE_SNAKE_1;
						break;
					case 1:
						standUpSound = SoundLoader.STAND_WHITE_SNAKE_2;
						break;
					default:
						standUpSound = SoundLoader.STAND_WHITE_SNAKE_3;
						break;
					}
				} else {
					standUpSound = SoundLoader.THE_WORLD_STAND_UP;
				}
				// 召唤亮相音（替身主题乐）受配置 allowStandSound 控制：
				// 关闭「替身音效」后召唤替身不再广播主题音效（对齐原版
				// EventStandKey.isMovingMusic 语义；每次召唤实时读静态配置，
				// cloth 配置保存后经 syncToStatic 即时生效，无需缓存刷新）。
				if (ConfigHuaji.Stands.allowStandSound) {
					HuajiSoundPlayer.playToNearbyClient(player, standUpSound, 1.0f);
				}
			} else {
				player.sendMessage(Text.translatable("message.huajiage.stand_stand_up.cost_lack"), false);
			}
		} else {
			charge.setMaxValue(stand.getMaxMP());
			player.removeStatusEffect(PotionLoader.potionStand);
			// 隐者之紫收回时清除替身持续 buff（用户要求：替身收回效果即消失）
			if ("hermit_purple".equals(stand.getName())
					|| "huajiager:hermit_purple".equals(stand.getName())) {
				for (String eff : new String[] { "luck", "speed", "strength", "jump_boost",
						"regeneration", "huajiager:potion_huaji_overdrive" }) {
					StatusEffect se = StandPowerHelper.getPotion(eff);
					if (se != null) {
						player.removeStatusEffect(se);
					}
				}
			}
			// 白蛇收回时清除替身持续 buff（与隐者之紫同口径：替身收回效果即消失）
			if ("white_snake".equals(stand.getName())
					|| "huajiager:white_snake".equals(stand.getName())) {
				for (String eff : new String[] { "luck", "speed", "jump_boost", "invisibility" }) {
					StatusEffect se = StandPowerHelper.getPotion(eff);
					if (se != null) {
						player.removeStatusEffect(se);
					}
				}
			}
			data.setTrigger(false);
			// 收回同样同步 trigger=false，避免客户端残留"已召唤状态
			StandUtil.syncStandData(player);
		}
	}
}
