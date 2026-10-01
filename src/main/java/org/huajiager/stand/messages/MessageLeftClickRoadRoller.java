package org.huajiager.stand.messages;

import java.util.List;

import org.huajiager.entity.EntityRoadRoller;
import org.huajiager.init.HuajiConstant;
import org.huajiager.util.NBTHelper;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S：空手左击推压路机（DIO 时停连击计数），
 * 。
 * 无载荷，服务端检索玩家周围 20 格内的压路机实体并累计连击数。
 */
public record MessageLeftClickRoadRoller() implements FabricPacket {

	public static final PacketType<MessageLeftClickRoadRoller> TYPE = PacketType.create(
			new Identifier("huajiager", "left_click_road_roller"), MessageLeftClickRoadRoller::new);

	public MessageLeftClickRoadRoller(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public static void handle(MessageLeftClickRoadRoller payload, ServerPlayerEntity player,
			PacketSender responseSender) {
		List<EntityRoadRoller> road = player.getWorld().getEntitiesByClass(EntityRoadRoller.class,
				player.getBoundingBox().expand(20), e -> true);
		// 时停标记由 EventTimeStop/TimeStopHelper 以 Integer 存储（The.world 倒计时 tick 数），
		// 原实现误用 getEntityBoolean 读取（attachment 内存里是 Integer，instanceof Boolean 恒 false），
		// 导致空手左击"推压路机连续击"在时停中永远不生效、dio_push 恒为 0。改为按 tick 数判断。
		if (road != null
				&& NBTHelper.getEntityInteger(player, HuajiConstant.Tags.THE_WORLD) > 0) {
			for (EntityRoadRoller roller : road) {
				int a = NBTHelper.getEntityInteger(roller, "huajiage.dio_push");
				NBTHelper.setEntityInteger(roller, "huajiage.dio_push", a + 2);
			}
		}
	}
}
