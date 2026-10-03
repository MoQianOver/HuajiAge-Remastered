package org.huajiager.client;

import java.util.Random;

import org.huajiager.attachment.Attachments;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;
import org.huajiager.client.event.EventStandKey;
import org.huajiager.client.event.EventTimeStopView;
import org.huajiager.init.HuajiConstant;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.stand.messages.MessageDioBreadTimeStop;
import org.huajiager.stand.messages.MessageDioHitClient;
import org.huajiager.stand.messages.MessageDoStandPowerClient;
import org.huajiager.stand.messages.MessageParticleGenerator;
import org.huajiager.stand.messages.MessageTimeStopFilterSync;
import org.huajiager.stand.messages.SyncExposedStandDataMessage;
import org.huajiager.stand.messages.SyncStandChargeMessage;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 客户端网络包处理器注册中心（client source set）。
 * 收敛 落点在 HuajiAgeRemasteredClient 的 S2C 接收器，并按 逐批补入
 * MessageParticleGenerator / MessageDioHitClient / SyncStandChargeMessage /
 * SyncExposedStandDataMessage 的客户端处理逻辑。
 */
public final class ClientPacketHandlers {

	private static final Random RANDOM = new Random();

	private ClientPacketHandlers() {
	}

	public static void register() {
		// S2C：接收替身能力客户端广播，驱动 doStandCapabilityClient（技能特效/音效）
		ClientPlayNetworking.registerGlobalReceiver(MessageDoStandPowerClient.TYPE, (payload, player, responseSender) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.execute(() -> {
				World world = mc.world;
				if (world == null) {
					return;
				}
				LivingEntity entity = world.getPlayers().stream()
						.filter(p -> p.getGameProfile().getName().equals(payload.playerName()))
						.findFirst().orElse(null);
				StandBase stand = StandLoader.getStand(payload.standName());
				if (entity != null && stand != null) {
					stand.doStandCapabilityClient(world, entity);
					// ORGA_REQUIEM 技能成功确认（服务端精神力校验通过并 doStandCapability 后
					// 单播宿主）：本地收到后启动 BGM+歌词倒计时；能量不足时收不到本包，歌词不再误出。
					if (StandLoader.ORGA_REQUIEM.getName().equals(payload.standName()) && entity == mc.player) {
						EventStandKey.triggerRequiemBgm();
					}
				}
			});
		});

		// S2C：通用粒子生成（MessageParticleGenerator.Handler）
		ClientPlayNetworking.registerGlobalReceiver(MessageParticleGenerator.TYPE, (payload, player, responseSender) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.execute(() -> {
				if (mc.world == null) {
					return;
				}
				processParticle((ClientWorld) mc.world, payload);
			});
		});

		// S2C：时停开场音（Dio 面包 / THE_WORLD / 白金之星，服务端随机选中音效并连同
		// 发动者坐标广播给附近玩家）。客户端以声源坐标播放，距离自然衰减。		// 不再依赖服务端世界广播，历史实测广播对发动者本人不可达。
		ClientPlayNetworking.registerGlobalReceiver(MessageDioBreadTimeStop.TYPE, (payload, player, responseSender) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.execute(() -> {
				if (mc.world == null) {
					return;
				}
				Identifier id = Identifier.tryParse(payload.soundId());
				if (id == null) {
					return;
				}
				SoundEvent sound = Registries.SOUND_EVENT.get(id);
				if (sound != null) {
					mc.world.playSound(payload.x(), payload.y(), payload.z(), sound,
							SoundCategory.PLAYERS, 5f, 1f, false);
				}
				// 记录本次时停来源（the_world=面包/世界发动，star_platinum=白金之星技能），
				// 供 EventTimeStopView 在时停结束时按来源分发结束音效。
				EventTimeStopView.setTimeStopSource(payload.source());
			});
		});

		// S2C：DIO 时停命中表现（熔岩粒子 + DIO_HIT / DIO_FLAG 音效，
		//  MessageDioHitClient.Handler）
		ClientPlayNetworking.registerGlobalReceiver(MessageDioHitClient.TYPE, (payload, player, responseSender) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.execute(() -> {
				if (mc.world == null) {
					return;
				}
				processDioHit((ClientWorld) mc.world, payload);
			});
		});

		// S2C：替身能量实时同步，写入本机玩家 StandHandler attachment
		// 注意：STAND_HANDLER 为 createDefaulted 注册，客户端本地玩家实体在收到首个
		// 同步包前从未被显式 set 过，getAttached 会返回 null 导致同步被静默丢弃
		//（遮罩/滤镜/时停尾音全部依赖 buffTag/buffer，表现为服务端冻结生效但客户端无表现）。
		// 必须用 getAttachedOrCreate 才能在首次同步时把默认 StandHandler 挂上并写入值。
		ClientPlayNetworking.registerGlobalReceiver(SyncStandChargeMessage.TYPE, (payload, player, responseSender) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			PlayerEntity local = mc.player;
			if (local == null) {
				return;
			}
			StandHandler charge = local.getAttachedOrCreate(Attachments.STAND_HANDLER);
			charge.setChargeValue(payload.charge());
			charge.setMaxValue(payload.max());
			// 能量回充只同步 charge/max，绝不覆盖 buffer/buffTag（技能状态由各自专属
			// 链路单播驱动）。否则常驻回充包（chargeOnly=true）会把客户端时停状态周期
			// 性打回未激活，导致滤镜/齿轮/反色一闪一闪。
			if (!payload.chargeOnly()) {
				charge.setBuffer(payload.buffer());
				charge.setBuffTag(payload.buffTag());
			}
		});

		// S2C：时停滤镜同步（发动者之外的受影响玩家：被冻结玩家 + 镇魂曲豁免玩家）。
		// 服务端 onTheWorld 每 tick 广播剩余 tick，只写本地 StandHandler 的
		// buffer/buffTag 驱动 EventTimeStopView / TimeStopPostShader，不触碰
		// charge/max（区别于 SyncStandChargeMessage 的能量语义，避免污染对方能量）。
		// remaining<=0 时 buffTag 置空，滤镜自动卸载（与发动者 syncEater 收尾对称）。
		ClientPlayNetworking.registerGlobalReceiver(MessageTimeStopFilterSync.TYPE,
				(payload, player, responseSender) -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					PlayerEntity local = mc.player;
					if (local == null) {
						return;
					}
					StandHandler charge = local.getAttachedOrCreate(Attachments.STAND_HANDLER);
					int remaining = payload.remaining();
					charge.setBuffer(remaining);
					charge.setBuffTag(remaining > 0 ? HuajiConstant.BuffTags.TIME_STOP : "");
				});

		// S2C：替身外露数据同步，写入目标玩家 ExposedData attachment
		ClientPlayNetworking.registerGlobalReceiver(SyncExposedStandDataMessage.TYPE,
				(payload, player, responseSender) -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					PlayerEntity self = mc.player;
					if (self == null) {
						return;
					}
					PlayerEntity target;
					if (payload.isUser()) {
						target = self;
					} else {
						target = null;
						for (PlayerEntity p : self.getWorld().getPlayers()) {
							if (p.getName().getString().equals(payload.user())) {
								target = p;
								break;
							}
						}
					}
					if (target == null) {
						return;
					}
					// 用 getAttachedOrCreate：STAND_DATA 为 persistent attachment，客户端
					// local player 在收到首个同步包前从未被 set 过，getAttached 返回 null
					// 会导致同步静默丢弃（EventStandKey 判定替身失败、召唤键无响应）。
					IExposedData data = target.getAttachedOrCreate(Attachments.STAND_DATA);
					if (data != null) {
						data.setStand(payload.stand());
						data.setStage(payload.stage());
						data.setTrigger(payload.trigger());
						data.setHandDisplay(payload.hand());
						data.setState(payload.state());
						data.setModel(payload.model());
						// 镇魂曲的鞘翅循环音不在这里单独启停：它已由召唤时创建的替身循环音层
						// 承担（音量按当前状态 soundLoop 门控，default 与 fly 都是循环态），
						// 两处都播会叠成双份鞘翅声。
					}
				});
	}

	private static void processParticle(ClientWorld world, MessageParticleGenerator payload) {
		Vec3d pos = payload.pos();
		int number = payload.count();
		double horizontalSpread = payload.speed();
		int type = payload.distance();
		if (type != 1) {
			return;
		}
		ParticleEffect particle = resolveParticle(payload.particle());
		if (particle == null) {
			return;
		}
		for (int i = 0; i < number; ++i) {
			double spawnX = pos.x + (2 * RANDOM.nextDouble() - 1) * horizontalSpread;
			double spawnY = pos.y + (2 * RANDOM.nextDouble() - 1) * horizontalSpread;
			double spawnZ = pos.z + (2 * RANDOM.nextDouble() - 1) * horizontalSpread;
			world.addParticle(particle, spawnX, spawnY, spawnZ, 0, 0, 0);
		}
	}

	private static void processDioHit(ClientWorld world, MessageDioHitClient payload) {
		Vec3d pos = payload.pos();
		boolean flag = payload.flag();
		final int NUMBER_OF_PARTICLES = 50;
		final double HORIZONTAL_SPREAD = 2;
		for (int i = 0; i < NUMBER_OF_PARTICLES; ++i) {
			double spawnX = pos.x + (2 * RANDOM.nextDouble() - 1) * HORIZONTAL_SPREAD;
			double spawnY = pos.y + (2 * RANDOM.nextDouble() - 1) * HORIZONTAL_SPREAD;
			double spawnZ = pos.z + (2 * RANDOM.nextDouble() - 1) * HORIZONTAL_SPREAD;
			world.addParticle(ParticleTypes.LAVA, spawnX, spawnY, spawnZ, 0, 0, 0);
		}
		world.playSound(pos.x, pos.y, pos.z,
				flag ? SoundLoader.DIO_HIT : SoundLoader.DIO_FLAG, SoundCategory.VOICE, 2f, 1f, false);
	}

	private static ParticleEffect resolveParticle(String name) {
		Identifier id = Identifier.tryParse(name);
		if (id == null) {
			return ParticleTypes.FIREWORK;
		}
		var type = Registries.PARTICLE_TYPE.get(id);
		if (type instanceof ParticleEffect effect) {
			return effect;
		}
		return ParticleTypes.FIREWORK;
	}
}
