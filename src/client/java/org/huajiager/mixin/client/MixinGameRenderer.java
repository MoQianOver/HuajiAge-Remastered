package org.huajiager.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 暴露 vanilla {@link GameRenderer# loadPostProcessor(Identifier)}（package-private）。
 *
 * 对应 vanilla 后处理加载机制
 * {@code EntityRenderer.loadShader / stopUseShader} 机制。
 * 通过 @Invoker 在渲染线程加载/切换 vanilla 后处理滤镜，加载后 GameRenderer 会在
 * renderWorld 之后、主 framebuffer 重绑定之前的后处理阶段自动绘制，保证滤镜可见。
 */
@Mixin(GameRenderer.class)
public interface MixinGameRenderer {

	@Invoker("loadPostProcessor")
	void invokeLoadPostProcessor(Identifier id);
}
