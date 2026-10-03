package org.huajiager.init.loaders;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 自定义粒子类型注册（huaji_splash：滑稽粒子；emerald_splash：绿宝石水花）。
 *
 * 供 useHuajiSplash 配置使用：开启后用滑稽粒子替换绿宝石水花，
 * 贴图 assets/huajiager/textures/particle/huaji.png。
 * 客户端工厂注册见 HuajiAgeRemasteredClient（ParticleFactoryRegistry）。
 */
public final class ParticleLoader {

	/** 滑稽水花粒子类型（huaji_splash），见 {@link HuajiParticleType}。 */
	public static DefaultParticleType HUAJI_SPLASH;

	/** 绿宝石水花粒子类型（emerald_splash）：法皇攻击态按发射速度外喷的绿色水花。 */
	public static DefaultParticleType EMERALD_SPLASH;

	private ParticleLoader() {
	}

	/** DefaultParticleType 的构造器是 protected，只能通过子类调用。 */
	private static final class HuajiParticleType extends DefaultParticleType {
		HuajiParticleType() {
			super(true);
		}
	}

	public static void register() {
		if (HUAJI_SPLASH != null) {
			return;
		}
		HUAJI_SPLASH = Registry.register(Registries.PARTICLE_TYPE,
				Identifier.of("huajiager", "huaji_splash"),
				new HuajiParticleType());
		EMERALD_SPLASH = Registry.register(Registries.PARTICLE_TYPE,
				Identifier.of("huajiager", "emerald_splash"),
				new HuajiParticleType());
	}
}
