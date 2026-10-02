package org.huajiager.client.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

/**
 * 滑稽水花粒子：替代绿宝石水花（HAPPY_VILLAGER）的滑稽脸粒子，
 * 贴图 particle/huaji.png，由 ParticleFactoryRegistry 注册到 ParticleLoader.HUAJI_SPLASH。
 */
public class HuajiSplashParticle extends SpriteBillboardParticle {

	public HuajiSplashParticle(ClientWorld world, double x, double y, double z,
			double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
		super(world, x, y, z);
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.scale = 0.3f + this.random.nextFloat() * 0.15f;
		this.maxAge = 20 + this.random.nextInt(15);
		this.setSpriteForAge(spriteProvider);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}
}
