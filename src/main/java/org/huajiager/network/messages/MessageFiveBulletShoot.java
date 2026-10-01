package org.huajiager.network.messages;

import org.huajiager.entity.EntityFivePower;
import org.huajiager.item.ItemBlancedHelmet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * C2S：五五开 BUFF 下左键空挥发射"得"字弹，
 * 。
 * 客户端仅下发左键意图；服务端校验头戴平衡头盔且 lord+open 后，生成
 * EntityFivePower（黑白"得"随机），对齐 MessageFiveBulletShoot.Handler：
 * setDe 随机 + setMaster（玩家 UUID）+ spawn + 播放音效。
 */
public record MessageFiveBulletShoot() implements FabricPacket {

	public static final PacketType<MessageFiveBulletShoot> TYPE = PacketType.create(
			new Identifier("huajiager", "five_bullet_shoot"), MessageFiveBulletShoot::new);

	public MessageFiveBulletShoot(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageFiveBulletShoot payload, ServerPlayerEntity player,
			PacketSender responseSender) {
		World world = player.getWorld();
		if (world.isClient) {
			return;
		}
		ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
		// 对齐 MessageFiveBulletShoot.Handler：服务端零校验（仅保留头戴平衡头盔防滥用），
		// lord/open 状态由客户端 hasKey 语义校验，避免服务端二次拦截导致左键不触发。
		if (!(head.getItem() instanceof ItemBlancedHelmet)) {
			return;
		}
		EntityFivePower entity = new EntityFivePower(world, player);
		entity.setDe(world.random.nextBoolean());
		entity.setMaster(player.getUuidAsString());
		world.spawnEntity(entity);
		world.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}
}
