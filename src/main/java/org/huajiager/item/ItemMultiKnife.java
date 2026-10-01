package org.huajiager.item;

import org.huajiager.entity.EntityMultiKnife;
import org.huajiager.init.loaders.ItemLoader;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 多刃飞刀（多刀）。
 *
 *  addPropertyOverride("light") 对应模型 multi_knife.json 的 light 谓词
 * （0 -> multi_knife_n / 1 -> multi_knife_shiny），Fabric 侧在客户端
 * HuajiAgeRemasteredClient 中以 ModelPredicateProviderRegistry 注册。
 * 右键发射已的 EntityMultiKnife 实体（工程 src/main/java/org/huajiager/entity）。
 *
 * 配置项 ConfigHuaji.Stands.knifeHeight（发射高度修正）未接入配置系统，
 * 此处以常量 KNIFE_HEIGHT 代替并取默认值 0.1F。
 */
public class ItemMultiKnife extends Item {

	/** 发光（light）模式 NBT 标签， NBT.IS_LIGHT 语义。 */
	private static final String TAG_LIGHT = "light";

	/**  ConfigHuaji.Stands.knifeHeight 默认值。 */
	private static final float KNIFE_HEIGHT = 0.1F;

	public ItemMultiKnife() {
		super(new Item.Settings().maxDamage(900)); // maxDamage() 内部已自动置 maxCount=1
	}

	/** 是否处于发光模式。 */
	public static boolean getLight(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean(TAG_LIGHT);
	}

	public static ItemStack setLight(ItemStack stack, boolean isLight) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(TAG_LIGHT, isLight);
		return stack;
	}

	@Override
	public Text getName(ItemStack stack) {
		if (getLight(stack)) {
			return Text.translatable("item.huajiager.multi_knife.shiny");
		}
		return super.getName(stack);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getStackInHand(hand);
		if (!world.isClient) {
			EntityMultiKnife knife = new EntityMultiKnife(world, player);
			// 实体构造器已按玩家朝向 setVelocity(1.5 倍视线方向) 完成发射，此处仅复刻
			// shoot 后的位置偏移：沿视线方向前移 1 格、高度按 knifeHeight 修正
			Vec3d v1 = player.getRotationVector();
			float fn = MathHelper.sqrt((float) (v1.x * v1.x + v1.y * v1.y + v1.z * v1.z));
			knife.setPosition(knife.getX() + v1.x / fn,
					knife.getY() + KNIFE_HEIGHT + 0.1F + v1.y / fn,
					knife.getZ() + v1.z / fn);
			knife.setRotation(MathHelper.wrapDegrees(-player.getYaw()));
			knife.setKnifePitch(player.getPitch());
			knife.setLife(360F);
			knife.setLight(getLight(itemstack));
			if (!player.getAbilities().creativeMode) {
				itemstack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
			}
			world.spawnEntity(knife);
		}
		player.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
		player.swingHand(hand);
		return TypedActionResult.success(itemstack);
	}

	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		//  getIsRepairable：只要滑稽锭存在即可修复（语义为任意材料可修）
		return ingredient.isOf(ItemLoader.huajiIngot);
	}
}
