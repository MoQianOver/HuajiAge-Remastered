package org.huajiager.command;

import org.huajiager.init.loaders.StandLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * 替身数据重载命令。
 *
 * 命令名 reloadStand，权限 0，执行 StandLoader.reloadStands()。
 * 以 Fabric 1.20.1 Brigadier 重写，并经 {@link #register} 在
 * CommandRegistrationCallback 中挂载（见主入口 onInitialize）。
 *
 * 命令语义保持一致：服务器端调用 StandLoader.reloadStands() 重载全部替身，
 * 权限等级 0（所有玩家可执行），无子命令。
 */
public final class CommandStandReload {

	private CommandStandReload() {
	}

	/**
	 * 注册 /reloadStand 命令到给定 dispatcher（命令语义见类头）。
	 */
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(net.minecraft.server.command.CommandManager.literal("reloadStand")
				.requires(source -> source.hasPermissionLevel(0))
				.executes(CommandStandReload::executeReload));
	}

	private static int executeReload(CommandContext<ServerCommandSource> context) {
		StandLoader.reloadStands();
		context.getSource().sendFeedback(
				() -> Text.literal("HUAJI Age: stand data reloaded (" + StandLoader.STAND_LIST.size() + " stands)."),
				false);
		return 1;
	}
}
