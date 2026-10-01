package org.huajiager.client.init.sound;

import org.huajiager.api.IStandState;
import org.huajiager.capability.IExposedData;
import org.huajiager.stand.StandStates;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

/**
 * 跟随实体的循环音效（client source set，独立编写），
 * 。
 *
 * 继承 MovingSound（随实体更新位置、实体死亡停止、替身在场音量联动）。 * 改实现 MovingSoundInstance：tick() 周期刷新位置（xPos/yPos/zPos），
 * SoundManager 按 isDone() 回收 done 的实例。
 *
 * 替身音量联动（对齐 update）：
 *  - 目标为替身展示实体（EntityStandBase）且当前为循环音效时，取用户替身状态。 *  - 状态存在但非 isSoundLoop 循环态 → 音量压到 0。 *  - 状态为循环态 → 音量 0.7。 *  - 状态为空（替身未在场/已消失）→ 停止播放。
 */
public class HuajiMovingSound extends MovingSoundInstance {

    /** 被跟踪的目标实体（对齐公开字段 ENTITY）。 */
    public final Entity ENTITY;

    /** 播放完成标志：Fabric MovingSoundInstance 无 setDonePlaying，以 done + isDone() 实现停止。 */
    private boolean done;

    public HuajiMovingSound(Entity target, SoundEvent sound, SoundCategory category) {
        // Fabric 1.20.1 MovingSoundInstance 构造需 net.minecraft.util.math.random.Random（节奏抖动用）
        super(sound, category, Random.create());
        this.ENTITY = target;
        this.update();
    }

    public HuajiMovingSound setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    public HuajiMovingSound setLoop() {
        this.repeat = true;
        return this;
    }

    public void stop() {
        this.done = true;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void tick() {
        this.x = ENTITY.getX();
        this.y = ENTITY.getY();
        this.z = ENTITY.getZ();
        if (ENTITY.isRemoved()) {
            this.done = true;
            return;
        }
        if (ENTITY instanceof EntityStandBase standEntity) {
            LivingEntity user = standEntity.getUser();
            if (user != null) {
                IExposedData data = StandUtil.getStandData(user);
                IStandState state = data == null ? null
                        : StandStates.getStandState(data.getStand(), data.getState());
                if (this.repeat && state != null) {
                    this.volume = state.isSoundLoop() ? 0.7f : 0.0f;
                }
                if (state == null) {
                    this.done = true;
                }
            }
        }
    }

    /** 对齐构造行为：构造完成后立即刷新一次位置与替身联动判定。 */
    private void update() {
        this.tick();
    }
}
