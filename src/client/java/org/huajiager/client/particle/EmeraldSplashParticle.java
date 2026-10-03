package org.huajiager.client.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

/**
 * 绿宝石水花粒子：法皇攻击态从翡翠弹发射点喷出的绿色水花。
 *
 * <p>按传入速度外喷，重力置 0（原版该粒子继承水花粒子，靠发射速度飞出而不是下坠），
 * 颜色 0.62/1.0/0.62、尺寸 0.75、强制最高亮度，均对齐原版水花。
 * 贴图取原版水花 splash_0..3（见 particles/emerald_splash.json）。</p>
 */
public class EmeraldSplashParticle extends SpriteBillboardParticle {

	public EmeraldSplashParticle(ClientWorld world, double x, double y, double z,
			double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
		super(world, x, y, z);
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.red = 0.62f;
		this.green = 1.0f;
		this.blue = 0.62f;
		// 尺寸口径：1.12 的 particleScale 渲染时乘 0.1，1.20.1 的 getSize() 直接返回 scale
		// （单位=格），所以原版的 0.75 在 1.20.1 对应约 0.075 格；取 0.1 保证可见又不糊脸。
		this.scale = 0.1f;
		this.gravityStrength = 0.0f;
		this.maxAge = (int) (8.0 / (this.random.nextFloat() * 0.8f + 0.2f)) + 4;
		this.setSpriteForAge(spriteProvider);
	}

	@Override
	public int getBrightness(float tickDelta) {
		// 对齐原版水花：强制最高亮度（原版覆写 getBrightnessForRender 后仅保留天空光）
		return 0xF000F0;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
}
