package org.huajiager.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

import com.mojang.serialization.Codec;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.capability.ExposedData;
import org.huajiager.capability.IExposedData;
import org.huajiager.capability.StandHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体数据附着层。
 * 替代 的 getEntityData() 附加数据。
 */
public final class Attachments {

    /**
     * 通用实体标记数据（运行时瞬态量，如时停计数、替身标记等）。
     */
    public static final AttachmentType<Map<String, Object>> ENTITY_DATA =
            AttachmentRegistry.createDefaulted(
                    new Identifier(HuajiAgeRemastered.MOD_ID, "entity_data"),
                    HashMap::new);

    /**
     * 替身能量（蓄力）处理器，替代 的 CapabilityStandHandler，以 attachment 挂载到实体。
     * <p>持久化 attachment（persistent）：charge/max 落盘，退出重进世界后精神力保留。     * buffer/buffTag 等瞬态量不入 Codec，时停中退出重进不会残留时停状态。</p>
     */
    @SuppressWarnings("unchecked")
    public static final AttachmentType<StandHandler> STAND_HANDLER =
            (AttachmentType<StandHandler>) (AttachmentType<?>)
                    AttachmentRegistry.<StandHandler>builder()
                            .initializer(StandHandler::new)
                            .persistent(StandHandler.CODEC)
                            .buildAndRegister(
                                    new Identifier(HuajiAgeRemastered.MOD_ID, "stand_handler"));

    /**
     * 时停冻结持久化标记：时停冻结写入了实体原生 NoGravity=true（随区块持久化），
     * 但 TIME_STOP 计数存于非持久化 ENTITY_DATA，退出重进后计数丢失、释放逻辑
     * （onTimeStop 归零）不再触发，实体将永久悬浮。该标记随区块落盘，实体加载时
     * 若为 true 则由 EventTimeStop 统一清理冻结残留。
     */
    public static final AttachmentType<Boolean> TIME_STOP_FROZEN =
            AttachmentRegistry.<Boolean>builder()
                    .initializer(() -> Boolean.FALSE)
                    .persistent(Codec.BOOL)
                    .buildAndRegister(
                            new Identifier(HuajiAgeRemastered.MOD_ID, "time_stop_frozen"));

    /**
     * 白蛇心智剥夺标记（disc_deprive），替代非持久化 ENTITY_DATA 中的同名 key：
     * 必须随玩家数据落盘，玩家死亡重生后仍持续凋零/掉血（方案二），直到被夺者用
     * 自己的心智碟或替身碟右键解除；EntityData不持久化，此为对齐的增强。
     * <p>必须带 copyOnDeath：Fabric data-attachment 的 COPY_FROM 转移中，死亡重生
     * （isDeath=true）只转移 copyOnDeath 的 attachment；仅 persistent 会在重生时丢失，
     * 导致被夺惩罚不继承（本次修复）。</p>
     */
    public static final AttachmentType<Boolean> DISC_DEPRIVE =
            AttachmentRegistry.<Boolean>builder()
                    .initializer(() -> Boolean.FALSE)
                    .persistent(Codec.BOOL)
                    .copyOnDeath()
                    .buildAndRegister(
                            new Identifier(HuajiAgeRemastered.MOD_ID, "disc_deprive"));

    /**
     * 替身玩家的外露附属数据，替代 的 CapabilityLoader.EXPOSED_DATA（IExposedData），
     * 保存替身名 / 阶段 / 触发状态 / 状态机 / 模型。
     * <p>持久化 attachment（persistent）：玩家重进世界 / 重启服务器后替身数据不丢失，
     * 不会出现"每次重进都能重新用一次觉醒箭"的假丢失。</p>
     * <p>带 initializer：客户端 local player 尚未收到同步包时也能通过
     * getAttachedOrCreate 拿到默认实例并写入同步数据（持久化不受影响）。</p>
     */
    @SuppressWarnings("unchecked")
    public static final AttachmentType<IExposedData> STAND_DATA =
            (AttachmentType<IExposedData>) (AttachmentType<?>)
                    AttachmentRegistry.<ExposedData>builder()
                            .initializer(ExposedData::new)
                            .persistent(ExposedData.CODEC)
                            .buildAndRegister(
                                    new Identifier(HuajiAgeRemastered.MOD_ID, "stand_data"));

    private Attachments() {
    }
}
