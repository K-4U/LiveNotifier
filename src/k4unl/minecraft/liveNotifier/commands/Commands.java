package k4unl.minecraft.liveNotifier.commands;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class Commands {
    public static void init(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandLiveNotifier());
    }
}
