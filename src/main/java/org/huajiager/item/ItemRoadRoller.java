package org.huajiager.item;

import org.huajiager.entity.EntityRoadRoller;
import org.huajiager.init.sound.SoundLoader;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 压路机物品，独立编写（语义 ，非照搬第三方移植）。
 *
 * 逻辑：右键在视线方向前方生成 EntityRoadRoller 投掷物，赋予初速与 3D 姿态
 * （rotation/pitch/damage/life），命中目标造成大爆炸并消耗一个物品；非创造模式扣除本体。
 * 客户端本地播 ROAD_ROLLER 音效 + 挥臂动画。
 */
public class ItemRoadRoller extends Item {

	public ItemRoadRoller() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			Vec3d look = user.getRotationVec(1.0f);
			EntityRoadRoller road = new EntityRoadRoller(world, user);
			// pos += look 单位向量 1 格（y 取 eyeY + look.y）；shoot(speed=1, divergence=0)
			road.setPosition(user.getX() + look.x, user.getEyeY() + look.y, user.getZ() + look.z);
			road.setVelocity(look.x, look.y, look.z);
			road.setRotation(MathHelper.wrapDegrees(-user.getYaw()));
			road.setPitch(user.getPitch());
			road.setDamage(10f);
			road.setLife(512f);
			if (!user.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			world.spawnEntity(road);
		}
		world.playSound(null, user.getBlockPos(), SoundLoader.ROAD_ROLLER, SoundCategory.PLAYERS, 2f, 1f);
		user.swingHand(hand);
		return TypedActionResult.success(stack);
	}
}
