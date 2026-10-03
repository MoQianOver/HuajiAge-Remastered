package org.huajiager.compat.jade;

import org.huajiager.block.HuajiBlender;
import org.huajiager.block.HuajiPolyfurnace;
import org.huajiager.stand.StandUtil;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.stand.instance.StandBase;
import org.huajiager.tileentity.TileEntityHuajiBlender;
import org.huajiager.tileentity.TileEntityHuajiPolyfurnace;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade 联动：查看方块时显示机器进度，查看替身实体时显示替身名 / 状态 / 操控者。
 *
 * <p>进度由 {@link MachineDataProvider} 在服务端写入 NBT、客户端读取显示；
 * 该 provider 注册在 jade 入口点上，类必须留在 common 源集，否则专用服务器加载
 * jade 入口点时会因缺类而报错。Jade 未安装时无人读取该入口点，本类不会被加载。</p>
 */
@WailaPlugin("huajiager")
public class HuajiJadePlugin implements IWailaPlugin {

	private static final Identifier MACHINE_DATA = Identifier.of("huajiager", "machine_data");
	private static final Identifier MACHINE_TOOLTIP = Identifier.of("huajiager", "machine_tooltip");
	private static final Identifier STAND_ENTITY_TOOLTIP = Identifier.of("huajiager", "stand_entity_tooltip");

	@Override
	public void register(IWailaCommonRegistration registration) {
		// 机器进度在服务端算好再下发，避免客户端读不到 BlockEntity 状态
		registration.registerBlockDataProvider(new MachineDataProvider(), TileEntityHuajiBlender.class);
		registration.registerBlockDataProvider(new MachineDataProvider(), TileEntityHuajiPolyfurnace.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(new MachineTooltipProvider(), HuajiBlender.class);
		registration.registerBlockComponent(new MachineTooltipProvider(), HuajiPolyfurnace.class);
		registration.registerEntityComponent(new StandEntityTooltipProvider(), EntityStandBase.class);
	}

	/** 把机器当前进度与储量写进下发 NBT。 */
	public static class MachineDataProvider implements IServerDataProvider<BlockAccessor> {

		@Override
		public void appendServerData(NbtCompound data, BlockAccessor accessor) {
			BlockEntity be = accessor.getBlockEntity();
			if (be instanceof TileEntityHuajiBlender blender) {
				data.putInt("progress", blender.processingProgress);
				data.putInt("total", blender.processingTotalTime);
			} else if (be instanceof TileEntityHuajiPolyfurnace poly) {
				data.putInt("progress", poly.processingProgress);
				data.putInt("total", poly.processingTotalTime);
				data.putInt("pool", poly.getPool());
				data.putInt("poolMax", TileEntityHuajiPolyfurnace.getTotalPoint());
				data.putInt("energy", poly.getEnergyCurrent());
				data.putInt("energyMax", poly.getEnergyMax());
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE_DATA;
		}
	}

	/** 机器信息：处理进度百分比，终极熔炼炉额外显示无尽之星点数与能量。 */
	public static class MachineTooltipProvider implements IBlockComponentProvider {

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			NbtCompound data = accessor.getServerData();
			int total = data.getInt("total");
			if (total > 0) {
				int progress = data.getInt("progress");
				tooltip.add(Text.translatable("jade.huajiager.progress",
						Math.min(100, progress * 100 / total)));
			}
			if (data.contains("pool")) {
				tooltip.add(Text.translatable("jade.huajiager.poly_pool",
						data.getInt("pool"), data.getInt("poolMax")));
			}
			if (data.contains("energy")) {
				tooltip.add(Text.translatable("jade.huajiager.energy",
						data.getInt("energy"), data.getInt("energyMax")));
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE_TOOLTIP;
		}
	}

	/** 替身实体信息：替身名 / 当前状态 / 操控者。 */
	public static class StandEntityTooltipProvider implements IEntityComponentProvider {

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			if (!(accessor.getEntity() instanceof EntityStandBase standEntity)) {
				return;
			}
			StandBase stand = standEntity.getStand();
			if (stand != null) {
				tooltip.add(Text.translatable("jade.huajiager.stand",
						Text.translatable(StandUtil.getLocalName(stand))));
			}
			String state = standEntity.getStandStateName();
			if (state != null && !state.isEmpty()) {
				tooltip.add(Text.translatable("jade.huajiager.stand.state", state));
			}
			LivingEntity user = standEntity.getUser();
			if (user != null) {
				tooltip.add(Text.translatable("jade.huajiager.stand.user", user.getName().getString()));
			}
		}

		@Override
		public Identifier getUid() {
			return STAND_ENTITY_TOOLTIP;
		}
	}
}
