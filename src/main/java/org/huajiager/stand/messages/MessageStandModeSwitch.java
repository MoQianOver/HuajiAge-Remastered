package org.huajiager.stand.messages;

import java.util.List;

import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.custom.StandStateCustom;
import org.huajiager.stand.custom.StandStateInfo;
import org.huajiager.stand.helper.StandPowerHelper;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.states.StandStateBase;
import org.huajiager.stand.states.default_set.StateStarPlatinumDefault;
import org.huajiager.stand.states.default_set.StateTheWorldDefault;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * C2S：替身状态机切换消息，
 * 。
 * 无载荷，服务端按当前状态在替身状态列表中循环切换。
 */
public record MessageStandModeSwitch() implements FabricPacket {

	public static final PacketType<MessageStandModeSwitch> TYPE = PacketType.create(
			new Identifier("huajiager", "stand_mode_switch"), MessageStandModeSwitch::new);

	public MessageStandModeSwitch(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageStandModeSwitch payload, ServerPlayerEntity player, PacketSender responseSender) {
		StandBase stand = StandUtil.getType(player);
		IExposedData data = StandUtil.getStandData(player);
		String state = StandUtil.getStandState(player);
		if (stand == null || data == null) {
			return;
		}
		List<String> states = stand.getStates();
		if (states == null || states.isEmpty()) {
			player.sendMessage(Text.translatable("message.huajiage.stand_mode.empty"), false);
			return;
		}
		if (state == null || state.isEmpty()) {
			player.sendMessage(Text.translatable("message.huajiage.stand_mode.empty_null"), false);
			return;
		}
		switchState(player, data, stand, 1);
		data.setStage(StandUtil.getStandStage(player));
		String stateNew = StandUtil.getStandState(player);
		StandStateBase stateBase = toBase(stand.getName(), stateNew);
		if (stateBase != null) {
			data.setHandDisplay(stateBase.isHandPlay());
		}
		// 状态已切换，必须把最新 state 全量回推客户端：
		// 客户端替身渲染器（RenderStandBase.isIdle）读取本地玩家 STAND_DATA.state 判定
		// 闲置/攻击模型。此前从未下发 SyncExposedStandDataMessage，客户端 state 永远卡在
		// 初次同步值，表现为"切回攻击模式仍是闲置抱胸模型、不挥拳"。
		StandUtil.syncStandData(player);
		// 切换进攻击态（非闲置）时播一次起手音，让"切模式"有明确反馈：
		// THE_WORLD → STAND_THE_WORLD_HIT_1，Star Platinum → STAND_STAR_PLATINUM_1。		// 切换回闲置态不播。此处只负责切换瞬间的那一下，连打重击音继续由状态机按冷却播放。
		// THE_WORLD 起手音接入 StateTheWorldDefault 的统一攻击音冷却：切换反馈只响这一声，
		// 且立即接踵而来的命中音会被冷却静默，杜绝"切换+首拳"两响；连按切换键也被冷却兜住不连响。
		if (!ExposedData.States.IDLE.getName().equals(stateNew)) {
			if (StandLoader.THE_WORLD.getName().equals(stand.getName())) {
				StateTheWorldDefault.tryPlayAttackSound(player, SoundLoader.STAND_THE_WORLD_HIT_1, 0.6f,
						StateTheWorldDefault.STAND_THE_WORLD_HIT_1_DURATION_TICKS);
			} else if (StandLoader.STAR_PLATINUM.getName().equals(stand.getName())) {
				// 白金之星切换进攻击态起手音：接入统一攻击音冷却（与 THE_WORLD 同语义），
				// 切换音未播完时紧接着的命中音被静默，杜绝"切换+首拳"双响。
				StateStarPlatinumDefault.tryPlayAttackSound(player, SoundLoader.STAND_STAR_PLATINUM_1, 0.6f,
						StateStarPlatinumDefault.STAND_HIT_1_DURATION_TICKS);
			} else if (StandLoader.HIEROPHANT_GREEN.getName().equals(stand.getName())) {
				// 绿法皇切换进攻击态播专属起手音 shoot_1（与 THE_WORLD/STAR_PLATINUM 起手音语义一致）。
				HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.STAND_HIEROPHANT_GREEN_SHOOT_1, 0.6f);
			} else if ("crazy_diamond".equals(stand.getName())
					|| "huajiager:crazy_diamond".equals(stand.getName())) {
				// 疯狂钻石：治疗模式音效已统一为攻击连打音（stand_crazy_diamond_1~4），
				// 无论切 heal 还是 default 都播连打起手音；repair 音仅保留给技能(capability)。
				// 均走同一音效时长冷却，防止切换瞬间与状态机首拳音双响。
				StandPowerHelper.playStandSoundWithCooldown(player,
						"huajiager:stand_crazy_diamond_" + (player.getRandom().nextInt(4) + 1), 0.6f);
			} else if ("white_snake".equals(stand.getName())
					|| "huajiager:white_snake".equals(stand.getName())) {
				// 白蛇连打替身：状态列表 [default, punch]，仅切进 punch（连击）态播起手音
				// （stand_white_snake_hit_1~3 随机），切回 default 不播。				// 与状态机命中音共用 hit 通道冷却，切换音未播完命中不双响。
				if (ExposedData.States.PUNCH.getName().equals(stateNew)) {
					StandPowerHelper.playStandSoundWithCooldown(player,
							"huajiager:stand_white_snake_hit_" + (player.getRandom().nextInt(3) + 1), 0.6f);
				}
			} else if ("hermit_purple".equals(stand.getName())
					|| "huajiager:hermit_purple".equals(stand.getName())) {
				// 隐者之紫：状态列表为 [default, overdrive]（无闲置态），
				// 仅在切进波纹疾走(overdrive)时播专属波纹音 stand_hermit_purple_wave。				// 切回 default 不播音效。
				if ("overdrive".equals(stateNew)) {
					HuajiSoundPlayer.playToNearbyClient(player, SoundLoader.STAND_HERMIT_PURPLE_WAVE, 0.6f);
				}
			}
		}
		// 状态显示键：自定义替身状态（StandStateCustom）直接使用 JS 声明的 stateKey
		// （如 stand.huajiager.crazy_diamond.heal，与 lang 键一致），避免拼出 lang 中
		// 不存在的 "stand.state.huajiager.heal" 显示原始 key；原生替身状态保持原前缀。
		// 隐者之紫切回默认态：清除波纹疾走专属效果（potion_huaji_overdrive 与 regeneration，
		// 均在 overdrive 态 update 中施加），避免切回默认后残留波纹疾走 buff
		// （speed/strength/jump_boost 两态都刷新，由 default update 无缝接管，无需清）。
		if (("hermit_purple".equals(stand.getName()) || "huajiager:hermit_purple".equals(stand.getName()))
				&& ExposedData.States.DEFAULT.getName().equals(stateNew)) {
			for (String eff : new String[] { "huajiager:potion_huaji_overdrive", "regeneration" }) {
				net.minecraft.entity.effect.StatusEffect se = StandPowerHelper.getPotion(eff);
				if (se != null) {
					player.removeStatusEffect(se);
				}
			}
			// 同步清除波纹疾走蓄力标记（buffTag/buffer）：
			// 防止切回 default 后残留导致 isHermitOverdrive / 波澜联动误判。			// 切进 overdrive 时标记由 hermit_purple_overdrive.js update 每 tick 写入。
			StandHandler charge = StandUtil.getStandHandler(player);
			if (charge != null) {
				charge.setBuffTag("empty");
				charge.setBuffer(0);
			}
		}
		String standStateFormat = "stand.state.huajiager." + stateNew;
		if (stateBase instanceof StandStateCustom custom) {
			StandStateInfo info = custom.getStateInfo();
			if (info != null && info.getName() != null && !info.getName().isEmpty()) {
				standStateFormat = info.getName();
			}
		}
		// 参数直接传 Text.translatable(...) 由客户端按本地语言翻译；此前在服务端
		// getString()（服务端默认 en_us），中文客户端收到的是英文状态名而非中文。
		// 移除升级音：此前「起手音 + ENTITY_PLAYER_LEVELUP」双音叠加，切换一次响两声。		// 切换反馈只保留替身起手音这一声。
		player.sendMessage(
				Text.translatable("message.huajiage.stand_mode.switch", Text.translatable(standStateFormat)),
				false);
	}

	private static void switchState(ServerPlayerEntity player, IExposedData data, StandBase stand, int step) {
		String state = StandUtil.getStandState(player);
		List<String> states = stand.getStates();
		if (states == null || state == null || state.isEmpty()) {
			return;
		}
		int index = states.indexOf(state) + step;
		if (index + 1 <= states.size() && index < states.size()) {
			StandStateBase base = toBase(stand.getName(), states.get(index));
			if (base != null) {
				if (base.getStage() <= data.getStage()) {
					StandUtil.setStandState(player, states.get(index));
				} else {
					switchState(player, data, stand, step + 1);
				}
			}
		} else {
			StandUtil.setStandState(player, states.get(0));
		}
	}

	private static StandStateBase toBase(String stand, String state) {
		Object st = StandStates.getStandState(stand, state);
		return st instanceof StandStateBase b ? b : null;
	}
}
