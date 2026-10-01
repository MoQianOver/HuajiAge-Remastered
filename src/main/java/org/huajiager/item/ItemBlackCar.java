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
 * 黑色高级轿车。
 *
 * 右键在视线方向生成 EntityRoadRoller 投掷物，rollType 置为 CAR，
 * 伤害 50、寿命 512，客户端播 ORGA_RIDER 音效并挥动手臂；非创造消耗 1 个。
 */
public class ItemBlackCar extends Item {

	public ItemBlackCar() {
		super(new Item.Settings().maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			Vec3d look = user.getRotationVec(1.0f);
			EntityRoadRoller road = new EntityRoadRoller(world, user);
			// pos += look 单位向量（y 额外 +0.1）；shoot(speed=1, divergence=0)
			road.setPosition(user.getX() + look.x, user.getEyeY() + 0.1f + look.y, user.getZ() + look.z);
			road.setVelocity(look.x, look.y, look.z);
			road.setRotation(MathHelper.wrapDegrees(-user.getYaw()));
			road.setPitch(user.getPitch());
			road.setRollType(EntityRoadRoller.enumTYPE.CAR.getName());
			road.setDamage(50f);
			road.setLife(512f);
			if (!user.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			world.spawnEntity(road);
		}
		world.playSound(null, user.getBlockPos(), SoundLoader.ORGA_RIDER, SoundCategory.PLAYERS, 2f, 1f);
		user.swingHand(hand);
		return TypedActionResult.success(stack);
	}
}
