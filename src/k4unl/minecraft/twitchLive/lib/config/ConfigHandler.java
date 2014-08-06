package k4unl.minecraft.twitchLive.lib.config;

import k4unl.minecraft.twitchLive.lib.config.Config;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
	private static Configuration config;
	
	public static void init(File configFile){
		config = new Configuration(configFile);
		
		Config.loadConfigOptions(config);
		
		if(config.hasChanged()){
			config.save();
		}
	}
	
}
