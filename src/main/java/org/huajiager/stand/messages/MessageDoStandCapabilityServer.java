package org.huajiager.stand.messages;

import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.custom.StandCustom;
import org.huajiager.stand.instance.StandBase;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * C2S：客户端请求触发替身能力。
 *
 * 经 SimpleNetworkWrapper(StandNetWorkHandler) SERVER 侧注册，Handler 内
 * 取玩家 ExposedData 后调用 StandLoader.getStand(...).doStandCapability(player)。
 * Fabric 1.20.1 无 CustomPayload/PayloadTypeRegistry（1.20.2+ API），改用
 * FabricPacket（PacketType + PacketByteBuf write/read），ServerPlayNetworking 注册。
 *
 * <p>载荷携带技能消耗 cost：技能链已从「PerfromSkill 单独扣费 + 本包触发」双包合并为
 * 单包，服务端在同一 handler 内完成「能量校验 -> 扣费 -> doStandCapability」。
 * 此前双包存在处理顺序竞态：PerfromSkill 先到会扣光能量，本包后到校验失败被拦截，
 * 导致技能提示（客户端本地消息）已出但能力从未触发（KQ 小车不生成即此问题）。</p>
 */
public record MessageDoStandCapabilityServer(int cost) implements FabricPacket {
	public static final PacketType<MessageDoStandCapabilityServer> TYPE = PacketType.create(
			new Identifier("huajiager", "stand_capability_server"), MessageDoStandCapabilityServer::new);

	public MessageDoStandCapabilityServer(PacketByteBuf buf) {
		this(buf.readInt());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(cost);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageDoStandCapabilityServer payload, ServerPlayerEntity player, PacketSender responseSender) {
		// 「已有替身」判定统一走 StandUtil.getType != null（对 null/空串/"empty" 三重归一），
		// 与 ItemArrowStand / ItemOrgaRunning 保持同一语义；原实现
		// !data.getStand().equals(StandLoader.EMPTY) 紧随默认字符串形态，形态变化即误判。
		// 客户端已放开 isTriggered 前置（见 EventStandKey.performSkill），此处补服务端
		// 强校验：仅当替身处于召唤（isTriggered）状态才触发主动技，防止未召唤时白扣费/白放技能。
		IExposedData data = StandUtil.getStandData(player);
		if (data == null || !data.isTriggered()) {
			return;
		}
		StandBase stand = StandUtil.getType(player);
		if (stand == null) {
			return;
		}
		// 能量校验与扣费统一在本 handler 内完成（载荷携带 cost），不再依赖
		// MessagePerfromSkill 先行扣费——双包存在顺序竞态：扣费包先到会把能量扣光，
		// 本包后到校验失败导致能力从不触发（KQ 小车不生成即此问题）。
		StandHandler charge = StandUtil.getStandHandler(player);
		if (charge == null || !charge.canBeCost(payload.cost())) {
			player.sendMessage(Text.translatable("message.huajiager.stand_skill.cost_lack"), false);
			return;
		}
		charge.cost(payload.cost());
		boolean triggered = true;
		if (stand instanceof StandCustom) {
			// 自定义替身：JS capability 返回 false 表示未真正触发技能
			// （如隐者之紫没拿相机只提示 need_camera），退还本次精神力，
			// 避免"提示需要相机却仍扣费"（用户反馈 bug）。
			triggered = ((StandCustom) stand).doStandCapabilityResult(player);
		} else {
			stand.doStandCapability(player);
		}
		if (!triggered) {
			charge.charge(payload.cost());
			return;
		}
		// 技能触发消息统一在服务端能量校验通过、能力真正触发后发送（S2C 直发宿主）。
		// 此前客户端按技能键本地即发消息，服务端精神力校验失败（不足提示、能力未触发）
		// 提前 return 时消息照显，与"精神力不足"表现矛盾（用户反馈 bug）。
		// 前缀必须是 "stand.huajiager.skill."（与 zh_cn/en_us lang 键一致），
		// 由客户端原拼装逻辑平移至此（stand.getName() 的 ":" 替换为 "."）。
		// JS 自定义替身（StandCustom）不走统一 .start 文案：状态 JS 的 capability
		// 自行发送各自技能文案（如隐者之紫念写 / 波纹疾走 run），此处跳过防止
		// "精神力不足时仍显示 Hermit Purple 及波纹" 的双份/错位提示。
		String skillKey = "stand.huajiager.skill."
				+ stand.getName().replace(":", ".") + ".start";
		if (!(stand instanceof StandCustom)) {
			player.sendMessage(Text.translatable(skillKey), false);
		}
		// 技能成功触发确认（S2C 单播宿主）：镇魂曲 BGM/歌词只在服务端能量校验通过、
		// 能力真正触发后才启动。此前客户端按技能键本地即播歌词，能量不足时服务端
		// 校验失败（本 handler 提前 return）能力未触发，歌词仍照播（用户反馈 bug）。
		// 复用 MessageDoStandPowerClient（载荷 playerName+standName），客户端 handler
		// 对 ORGA_REQUIEM 且实体为本机玩家时调 EventStandKey.triggerRequiemBgm()；
		// 女仆替身同样借本确认包驱动客户端技能语音（客户端 handler 按替身名分发）。
		// MAID_STAND 是编译期字符串常量，引用它不会加载车万相关的 compat 类。
		String firedStand = stand.getName();
		if (StandLoader.ORGA_REQUIEM.getName().equals(firedStand)
				|| org.huajiager.compat.tlm.MaidBallHelper.MAID_STAND.equals(firedStand)) {
			ServerPlayNetworking.send(player,
					new MessageDoStandPowerClient(player.getGameProfile().getName(), firedStand));
		}
	}
}
