package k4unl.minecraft.liveNotifier.events;


import k4unl.minecraft.liveNotifier.LiveNotifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author Koen Beckers (K-4U)
 */
public class EventHelper {

    private int ticksPassed = 0;

    public static void init(){
        MinecraftForge.EVENT_BUS.register(new EventHelper());
    }

    @SubscribeEvent
    public void tickPlayer(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            if(event.side.isServer()){
                ticksPassed++;
                if(ticksPassed == 20 * 60 * LiveNotifier.instance.settings.getDelay()){
                    ticksPassed = 0;
                    LiveNotifier.instance.recheckChannels();
                }
            }
        }
    }
}
