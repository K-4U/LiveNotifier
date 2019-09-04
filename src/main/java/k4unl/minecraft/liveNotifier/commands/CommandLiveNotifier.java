package k4unl.minecraft.liveNotifier.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import k4unl.minecraft.k4lib.commands.impl.CommandK4OpOnly;
import k4unl.minecraft.k4lib.lib.config.ModInfo;
import k4unl.minecraft.liveNotifier.LiveNotifier;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandLiveNotifier extends CommandK4OpOnly {

	@Override
	public void register(LiteralArgumentBuilder<CommandSource> argumentBuilder) {
		argumentBuilder.then(Commands.literal("version").executes(this::showVersion));
		argumentBuilder.then(Commands.literal("load").executes(this::reload));
		argumentBuilder.then(Commands.literal("check").executes(this::recheckChannels));
		argumentBuilder.then(Commands.literal("clear").executes(this::clear));
	}

	private int clear(CommandContext<CommandSource> context) {
		LiveNotifier.instance.liveChannels.clear();
		context.getSource().sendFeedback(new StringTextComponent("List cleared"), false);
		return 0;
	}

	private int recheckChannels(CommandContext<CommandSource> context) {
		LiveNotifier.instance.recheckChannels();
		context.getSource().sendFeedback(new StringTextComponent("Channels check started"), false);
		return 0;
	}

	private int reload(CommandContext<CommandSource> context) {
		LiveNotifier.instance.readChannelsFromFile();
		context.getSource().sendFeedback(new StringTextComponent(LiveNotifier.instance.settings.getChannels().size() + " channels reloaded from disk"), false);
		return 0;
	}

	private int showVersion(CommandContext<CommandSource> context) {
		context.getSource().sendFeedback(new StringTextComponent("LiveNotifier version " + ModInfo.VERSION), false);
		return 0;
	}

	@Override
	public String getName() {

		return "livenotifier";
	}
}
