package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 官方替身模型（ModelStandBase 后代）的 1.20.1 渲染接口。
 *
 * 官方替身模型通过 setRotationAngles(..., power, speed) 驱动手部/偏移动画，
 * 渲染器（RenderStandBase）按替身名取模型实例，若实现本接口则以带动画的
 * renderStand 渲染，否则退回无动画的 render()。power/speed 参照官方调用方式：
 * extraEffect 中 power 固定 1，speed 取 StandLoader.XXX.getSpeed()。
 */
public interface StandAnimatedModel {

	/**
	 * 带动画渲染替身模型。
	 *
	 * @param ageTicks 动画时钟（实体 age，约 20 tick/秒）
	 * @param speed    替身速度系数，官方取 stand.getSpeed()
	 * @param power    动画强度，官方固定传入 1
	 */
	void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, EntityStandBase entity,
			float ageTicks, float speed, float power);
}
