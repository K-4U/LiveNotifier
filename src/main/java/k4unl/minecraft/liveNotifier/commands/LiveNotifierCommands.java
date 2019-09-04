package k4unl.minecraft.liveNotifier.commands;


import com.mojang.brigadier.CommandDispatcher;

import k4unl.minecraft.k4lib.commands.CommandsRegistry;
import net.minecraft.command.CommandSource;

public class LiveNotifierCommands extends CommandsRegistry {

	public LiveNotifierCommands(boolean isDedicatedServer, CommandDispatcher<CommandSource> dispatcher) {
		super(isDedicatedServer, dispatcher);
		register(dispatcher, new CommandLiveNotifier());
    }
}
