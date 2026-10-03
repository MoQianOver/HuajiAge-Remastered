package org.huajiager.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;

/**
 * PostEffectProcessor 的 passes 访问器。
 *
 * 时停滤镜的动态 uniform 必须逐 pass 下发，而 passes 是私有字段且没有公开 getter；
 * 改用 @Accessor 由 Mixin 生成实现，字段名在构建时随映射 remap，
 * 避免按 Yarn 字段名反射在发行版（intermediary）里找不到字段而静默失效。
 */
@Mixin(PostEffectProcessor.class)
public interface PostEffectProcessorAccessor {

	@Accessor("passes")
	List<PostEffectPass> huajiager$getPasses();
}
