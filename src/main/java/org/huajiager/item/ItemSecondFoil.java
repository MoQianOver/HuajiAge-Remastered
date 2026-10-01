package org.huajiager.item;

import org.huajiager.entity.EntitySecondFoil;
import org.huajiager.init.loaders.DamageLoader;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.util.NBTHelper;
import org.huajiager.stand.entity.EntityStandBase;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 第二卷轴。
 *
 * 使用方法一：左键攻击生物或右键放出弹射物（EntitySecondFoil）击中生物，可使用五次。
 * 使用方法二：将物品丢出，10 秒内不可捡起；落地倒计时结束后——附近有可捡起的玩家则恢复
 * 可拾取由玩家捡走；否则按「6-已使用次数」生成弹射物自动锁定最近生物，并销毁该物品。
 *  EventLoader.SecondHitEvent / ItemTossEvent 在 Fabric 侧由
 * {@link org.huajiager.mixin.PlayerEntityDropMixin}（丢出拦截）与本类内联事件承接。
 */
public class ItemSecondFoil extends Item {

	/** 丢出后不可捡起时长 / 落地倒计时（10 秒），供 PlayerEntityDropMixin 复用。 */
	public static final int BURST_TIME = 200;
	/** 丢出后判定「有玩家可以捡起它」的球形半径。 */
	private static final double PLAYER_PICKUP_RANGE = 32.0;
	/** 丢出二段生效时搜索最近生物的目标半径（对齐 grow(100)）。 */
	private static final double TARGET_SEARCH_RANGE = 100.0;

	public ItemSecondFoil() {
		super(new Item.Settings().maxCount(1).maxDamage(4));
		registerDropLogic();
	}

	/**
	 *  onEntityItemUpdate（落地倒计时） + EventLoader.SecondHit（倒计时归零二段生效）
	 * 链路的 Fabric 等价实现。丢出拦截（不可捡起）见 PlayerEntityDropMixin。
	 */
	private static void registerDropLogic() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (ItemEntity item : world.getEntitiesByType(EntityType.ITEM,
						e -> e.getStack().isOf(ItemLoader.secondFoil))) {
					handleDroppedFoil(world, item);
				}
			}
		});
	}

	/**
	 * 丢出的二向箔落地后开始 10 秒倒计时（对齐：仅 onGround 时递减 burst_time）。
	 * 归零时：附近有可捡起的玩家 → 保持可拾取由玩家捡走（不销毁）。	 * 否则生成「6-已使用次数」个弹射物并销毁物品。
	 */
	private static void handleDroppedFoil(ServerWorld world, ItemEntity item) {
		if (item.isRemoved()) {
			return;
		}
		ItemStack stack = item.getStack();
		NbtCompound nbt = NBTHelper.getTagCompoundSafe(stack);
		if (!nbt.getBoolean("foil_dropped")) {
			return;
		}
		if (!item.isOnGround()) {
			return;
		}
		int t = nbt.getInt("foil_countdown");
		if (t <= 0) {
			return;
		}
		t--;
		nbt.putInt("foil_countdown", t);
		if (t > 0) {
			return;
		}

		// 倒计时归零：先看附近有没有能捡起它的玩家（排除旁观者）
		PlayerEntity near = world.getClosestPlayer(item.getX(), item.getY(), item.getZ(),
				PLAYER_PICKUP_RANGE, true);
		if (near != null) {
			// 有玩家可捡：清标记恢复普通拾取（pickupDelay 200 tick 已自然到期）
			nbt.remove("foil_dropped");
			nbt.remove("foil_countdown");
			return;
		}

		// 无玩家可捡：生成 6-已使用次数 个弹射物，自动锁定最近生物；销毁物品
		int count = stack.getMaxDamage() - stack.getDamage() + 2;
		LivingEntity owner = findPlayerByName(world, nbt.getString("owner"));
		LivingEntity target = findNearestLiving(world, item.getPos(), TARGET_SEARCH_RANGE, owner);
		for (int j = 0; j < count; j++) {
			EntitySecondFoil entity = new EntitySecondFoil(world, owner, target);
			entity.setPosition(item.getX(), item.getY(), item.getZ());
			world.spawnEntity(entity);
		}
		item.discard();
	}

	private static LivingEntity findPlayerByName(World world, String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		for (PlayerEntity p : world.getPlayers()) {
			if (p.getName().getString().equals(name)) {
				return p;
			}
		}
		return null;
	}

	private static LivingEntity findNearestLiving(World world, Vec3d pos, double range, Entity exclude) {
		Box box = new Box(pos.x - range, pos.y - range, pos.z - range,
				pos.x + range, pos.y + range, pos.z + range);
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, box, LivingEntity::isAlive)) {
			// 排除释放者本人与替身展示实体：弹射物不锁定/跟踪释放者或召唤替身
			if (e == exclude || e instanceof EntityStandBase) {
				continue;
			}
			double d = e.squaredDistanceTo(pos);
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		return best;
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		World world = target.getWorld();
		if (!world.isClient) {
			target.damage(DamageLoader.second(target), 1000f);
			target.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
			ItemStack drop = new ItemStack(ItemLoader.expendedView);
			ItemEntity item = new ItemEntity(world, target.getX(), target.getY(), target.getZ(), drop);
			world.spawnEntity(item);
		}
		stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		NbtCompound nbt = NBTHelper.getTagCompoundSafe(stack);
		nbt.putString("owner", entity.getName().getString());
		if (!nbt.contains("burst_time")) {
			nbt.putInt("burst_time", BURST_TIME);
		}
		super.inventoryTick(stack, world, entity, slot, selected);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			// 找 20 格内最近生物作目标（手动遍历，避免 getClosestEntity(null predicate) NPE）
			LivingEntity target = findNearestLiving(world, player.getPos(), 20.0, player);
			EntitySecondFoil entity = new EntitySecondFoil(world, player, target);
			world.spawnEntity(entity);
			stack.damage(1, player, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		// 保留 onLeftClickEntity 的命中即杀语义入口（此处由 postHit 承担）
		return super.useOnEntity(stack, player, entity, hand);
	}
}
