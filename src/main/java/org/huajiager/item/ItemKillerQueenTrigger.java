package org.huajiager.item;

import java.util.List;

import org.huajiager.capability.StandHandler;
import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.init.loaders.StandLoader;
import org.huajiager.init.sound.SoundLoader;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 点赞（杀手皇后触发器）， 。
 *
 * 获得：持有杀手皇后且替身激活时攻击生物/挖掘方块，事件 EventKillerQueen 自动发放/更新本物品，
 * NBT 记录被锁定生物 UUID 或方块坐标。右键引爆（消耗 KQ 消耗/10）：
 * - ENTITY：对锁定生物造成 2 格爆炸 + 20+40*stage 爆炸伤害；10% 概率（stage>0 时）直接秒杀。 * - BLOCK：对记录坐标造成 3+stage 格爆炸。 * 两种分支物品本体均消耗 1 个。
 */
public class ItemKillerQueenTrigger extends Item {

	public ItemKillerQueenTrigger() {
		super(new Item.Settings().maxCount(1));
	}

	public static void setData(ItemStack stack, String type, String uuid, double x, double y, double z, String dim) {
		NbtCompound nbt = new NbtCompound();
		nbt.putString(NBT.TYPE.getName(), type);
		nbt.putString(NBT.DIM.getName(), dim);
		TYPE t = TYPE.getType(type);
		if (t == TYPE.BLOCK) {
			nbt.putDouble(NBT.POS_X.getName(), x);
			nbt.putDouble(NBT.POS_Y.getName(), y);
			nbt.putDouble(NBT.POS_Z.getName(), z);
		} else if (t == TYPE.ENTITY) {
			nbt.putString(NBT.UUID.getName(), uuid);
		}
		stack.getOrCreateNbt().put(NBT.NBTs.getName(), nbt);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt().getCompound(NBT.NBTs.getName());
		String type = nbt.getString(NBT.TYPE.getName());
		TYPE t = TYPE.getType(type);
		tooltip.add(Text.translatable("item.killer_queen_trigger:tooltips.type", type));
		if (t == TYPE.BLOCK) {
			tooltip.add(Text.translatable("item.killer_queen_trigger:tooltips.x", nbt.getDouble(NBT.POS_X.getName())));
			tooltip.add(Text.translatable("item.killer_queen_trigger:tooltips.y", nbt.getDouble(NBT.POS_Y.getName())));
			tooltip.add(Text.translatable("item.killer_queen_trigger:tooltips.z", nbt.getDouble(NBT.POS_Z.getName())));
		} else if (t == TYPE.ENTITY) {
			tooltip.add(Text.translatable("item.killer_queen_trigger:tooltips.entity", nbt.getString(NBT.UUID.getName())));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient) {
			return TypedActionResult.success(stack);
		}
		StandBase stand = StandUtil.getType(user);
		StandHandler energy = StandUtil.getStandHandler(user);
		if (stand == null || !StandLoader.KILLER_QUEEN.equals(stand)) {
			return TypedActionResult.success(stack);
		}
		int stage = StandUtil.getStandStage(user);
		int cost = StandLoader.KILLER_QUEEN.getCost() / 10;
		boolean flag = energy != null && energy.canBeCost(cost);
		NbtCompound nbt = stack.getOrCreateNbt().getCompound(NBT.NBTs.getName());
		TYPE t = TYPE.getType(nbt.getString(NBT.TYPE.getName()));
		String dim = nbt.getString(NBT.DIM.getName());
		if (t != null && !dim.equals(world.getRegistryKey().getValue().toString())) {
			user.sendMessage(Text.translatable("message.huajiager.stand_killer_queen_trigger.dim_mismatch"), false);
			return TypedActionResult.success(stack);
		}
		if (t == TYPE.ENTITY && flag) {
			String uuid = nbt.getString(NBT.UUID.getName());
			List<Entity> entities = world.getEntitiesByClass(Entity.class,
					user.getBoundingBox().expand(StandLoader.KILLER_QUEEN.getDistance()), e -> true);
			for (Entity entity : entities) {
				if (entity == user || entity instanceof EntitySheerHeartAttack || entity instanceof EntityStandBase) {
					continue;
				}
				if (entity.getUuid().toString().equals(uuid)) {
					double chance = Math.random() * 100;
					world.playSound(null, user.getBlockPos(), SoundLoader.STAND_KILLER_QUEEN_TRIGGER,
							SoundCategory.PLAYERS, 1f, 1f);
					world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 2f, false,
							World.ExplosionSourceType.NONE);
					entity.damage(user.getDamageSources().explosion(user, user), 20f + stage * 40);
					if (entity instanceof EntitySheerHeartAttack attack) {
						attack.setHealth(0);
					}
					if (stage > 0 && chance < 10 && entity instanceof LivingEntity living) {
						living.setHealth(0);
						living.onDeath(user.getDamageSources().explosion(user, user));
					}
					stack.getOrCreateNbt().remove(NBT.NBTs.getName());
					stack.decrement(1);
					energy.cost(cost);
					break;
				}
			}
		} else if (t == TYPE.BLOCK && flag) {
			world.playSound(null, user.getBlockPos(), SoundLoader.STAND_KILLER_QUEEN_TRIGGER,
					SoundCategory.PLAYERS, 1f, 1f);
			BlockPos pos = new BlockPos((int) nbt.getDouble(NBT.POS_X.getName()),
					(int) nbt.getDouble(NBT.POS_Y.getName()), (int) nbt.getDouble(NBT.POS_Z.getName()));
			world.createExplosion(user, pos.getX(), pos.getY(), pos.getZ(), 3f + stage, false,
					World.ExplosionSourceType.NONE);
			stack.getOrCreateNbt().remove(NBT.NBTs.getName());
			stack.decrement(1);
			energy.cost(cost);
		} else if (!flag) {
			user.sendMessage(Text.translatable("message.huajiager.stand_killer_queen_trigger.cost_lack"), false);
		}
		return TypedActionResult.success(stack);
	}

	public enum NBT {
		NBTs("nbts"), TYPE("type"), UUID("uuid"), DIM("dim"), POS_X("pos_x"), POS_Y("pos_y"), POS_Z("pos_z");
		private final String name;

		NBT(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum TYPE {
		ENTITY("entity"), BLOCK("block");
		private final String name;

		TYPE(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static TYPE getType(String name) {
			for (TYPE type : values()) {
				if (type.getName().equals(name)) {
					return type;
				}
			}
			return null;
		}
	}
}
