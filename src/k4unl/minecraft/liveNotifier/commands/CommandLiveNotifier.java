package k4unl.minecraft.liveNotifier.commands;

import k4unl.minecraft.k4lib.commands.CommandK4OpOnly;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.k4lib.lib.config.ModInfo;
import k4unl.minecraft.liveNotifier.LiveNotifier;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class CommandLiveNotifier extends CommandK4OpOnly {

    public CommandLiveNotifier() {

        aliases.add("ln");
    }

    @Override
    public String getName() {

        return "livenotifier";
    }

    @Override
    public String getUsage(ICommandSender p_71518_1_) {

        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("version")) {
                sender.sendMessage(new TextComponentString("LiveNotifier version " + ModInfo.VERSION));
            } else if (args[0].equalsIgnoreCase("load")) {
                if (sender.getName().equals("Server")) {
                    LiveNotifier.instance.readChannelsFromFile();
                    sender.sendMessage(new TextComponentString(LiveNotifier.instance.settings.getChannels().size() + " channels reloaded from disk"));
                } else {
                    if (Functions.isPlayerOpped(((EntityPlayer) sender).getGameProfile())) {
                        LiveNotifier.instance.readChannelsFromFile();
                        sender.sendMessage(new TextComponentString(LiveNotifier.instance.settings.getChannels().size() + " channels reloaded from disk"));
                    }
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                LiveNotifier.instance.recheckChannels();
                sender.sendMessage(new TextComponentString("Channels check started"));
            } else if (args[0].equalsIgnoreCase("clear")) {
                LiveNotifier.instance.liveChannels.clear();
                sender.sendMessage(new TextComponentString("List cleared"));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {

        List<String> ret = new ArrayList<>();

        if (args.length == 1) {
            ret.add("version");
            ret.add("save");
            ret.add("load");
        }

        return ret;
    }
}
