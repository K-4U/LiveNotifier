package k4unl.minecraft.twitchLive;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import k4unl.minecraft.twitchLive.lib.Log;
import k4unl.minecraft.twitchLive.lib.config.ModInfo;

@Mod(
	modid = ModInfo.ID,
	name = ModInfo.NAME,
	version = ModInfo.VERSION,
	acceptableRemoteVersions="*"
)


public class TwitchLive {
	@Instance(value=ModInfo.ID)
	public static TwitchLive instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Log.init();
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event){		
	}
	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		
	}
	
	@EventHandler
	public void onServerStart(FMLServerStartingEvent event) {
	}
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event){
	}
	
	@EventHandler
	public void serverStop(FMLServerStoppingEvent event){
	}
}
