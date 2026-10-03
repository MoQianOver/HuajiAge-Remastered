package org.huajiager.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.ModelPart;

/**
 * ModelPart 的 children 访问器。
 *
 * 借用原版实体模型作为替身造型时，需要按部件名（head / left_arm 等）把骨骼暴露给动画脚本，
 * 而 children 是私有字段且没有公开 getter；改用 @Accessor 由 Mixin 生成实现，
 * 字段名在构建时随映射 remap，避免按 Yarn 字段名反射在发行版里失效。
 */
@Mixin(ModelPart.class)
public interface ModelPartAccessor {

	@Accessor("children")
	Map<String, ModelPart> huajiager$getChildren();
}
