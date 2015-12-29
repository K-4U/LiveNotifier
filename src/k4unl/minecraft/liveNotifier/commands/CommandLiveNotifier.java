package k4unl.minecraft.liveNotifier.commands;

import k4unl.minecraft.k4lib.commands.CommandK4OpOnly;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.k4lib.lib.config.ModInfo;
import k4unl.minecraft.liveNotifier.LiveNotifier;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

public class CommandLiveNotifier extends CommandK4OpOnly {

    public CommandLiveNotifier() {
        aliases.add("ln");
    }

    @Override
    public String getCommandName() {

        return "livenotifier";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {

        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("version")) {
                sender.addChatMessage(new ChatComponentText("LiveNotifier version " + ModInfo.VERSION));
            } else if (args[0].equalsIgnoreCase("save")) {
                if (sender instanceof DedicatedServer) {
                    LiveNotifier.instance.saveChannelsToFile();
                } else {
                    if (Functions.isPlayerOpped(((EntityPlayer) sender).getGameProfile())) {
                        LiveNotifier.instance.saveChannelsToFile();
                    }
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                if (sender instanceof DedicatedServer) {
                    LiveNotifier.instance.readChannelsFromFile();
                    sender.addChatMessage(new ChatComponentText("Channels reloaded from disk"));
                } else {
                    if (Functions.isPlayerOpped(((EntityPlayer) sender).getGameProfile())) {
                        LiveNotifier.instance.readChannelsFromFile();
                        sender.addChatMessage(new ChatComponentText("Channels reloaded from disk"));
                    }
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                LiveNotifier.instance.recheckChannels();
            } else if (args[0].equalsIgnoreCase("clear")) {
                LiveNotifier.instance.liveChannels.clear();
            }
        }
    }


    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {

        List<String> ret = new ArrayList<String>();

        if (args.length == 1) {
            ret.add("version");
            ret.add("save");
            ret.add("load");
        }

        return ret;
    }
}
