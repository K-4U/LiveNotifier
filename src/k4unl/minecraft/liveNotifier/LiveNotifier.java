package k4unl.minecraft.liveNotifier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.liveNotifier.commands.Commands;
import k4unl.minecraft.liveNotifier.events.EventHelper;
import k4unl.minecraft.liveNotifier.lib.*;
import k4unl.minecraft.liveNotifier.lib.config.ModInfo;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Mod(
        modid = ModInfo.ID,
        name = ModInfo.NAME,
        version = ModInfo.VERSION,
        acceptableRemoteVersions = "*"
)

public class LiveNotifier {

    @Instance(value = ModInfo.ID)
    public static LiveNotifier instance;
    private File suggestedConfigurationFile;

    public List<LiveChannel> channels = new ArrayList<LiveChannel>();
    public List<String> liveChannels = new ArrayList<String>();

    private static final ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("ChannelChecker #%d").setDaemon(true).build());

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();
        suggestedConfigurationFile = event.getSuggestedConfigurationFile();
        readChannelsFromFile();
        saveChannelsToFile();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        EventHelper.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {

        Commands.init(event);
    }


    public void readChannelsFromFile() {
        channels.clear();
        File dir = suggestedConfigurationFile;
        if (dir != null) {
            Gson gson = new Gson();
            if (!dir.exists()) {
                try {
                    dir.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileInputStream ipStream = new FileInputStream(dir);
                InputStreamReader reader = new InputStreamReader(ipStream);
                BufferedReader bReader = new BufferedReader(reader);
                String json = "";
                String line;
                while((line = bReader.readLine()) != null){
                    json += line;
                }
                reader.close();
                ipStream.close();
                bReader.close();

                Type myTypeMap = new TypeToken<List<LiveChannel>>() {}.getType();
                channels = gson.fromJson(json, myTypeMap);
                if (channels == null) {
                    channels = generateExampleChannel();
                }

                //Log.info("Read from file: " + json);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private List<LiveChannel> generateExampleChannel() {
        LiveChannel toAdd = new LiveChannel("Channel Name", StreamingService.TWITCH, true, true, true, true);
        List<LiveChannel> ret = new ArrayList<LiveChannel>();

        ret.add(toAdd);
        return ret;
    }

    public void saveChannelsToFile() {
        File dir = suggestedConfigurationFile;
        if (dir != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(channels);
            if (!dir.exists()) {
                try {
                    dir.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                PrintWriter opStream = new PrintWriter(dir);
                opStream.write(json);
                opStream.flush();
                opStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



    private Runnable channelChecker = new Runnable() {
        @Override
        public void run() {
            threadPoolExecutor.remove(this);
            for (LiveChannel channel: channels) {
                boolean isLive = false;
                String game = "";
                String title = "";
                if(channel.getService().equals(StreamingService.TWITCH)) {
                    isLive = Twitch.isLive(channel.getChannelName());
                    if(isLive){
                        Map<String, Object> streamData = Twitch.getStreamData(channel.getChannelName());
                        game = streamData.get("game").toString();
                        title = ((Map<String,Object>)streamData.get("channel")).get("status").toString();
                    }
                }
                if(channel.getService().equals(StreamingService.BEAM)){
                    isLive = Beam.isLive(channel.getChannelName());
                    if(isLive){
                        Map<String, Object> streamData = Beam.getChannelFromOwnerName(channel.getChannelName());
                        game = ((Map<String, Object>) streamData.get("type")).get("name").toString();
                        title = streamData.get("name").toString();
                    }
                }

                if(isLive) {
                    if (!liveChannels.contains(channel.getChannelName())) {
                        //Started streaming!
                        liveChannels.add(channel.getChannelName());
                        if(channel.isAnnounce()) {
                            //Announce it:
                            ChatComponentText msg = new ChatComponentText("");
                            msg.appendSibling(new ChatComponentText(channel.getChannelName().substring(0, 1).toUpperCase() + channel.getChannelName().substring(1) + " has started streaming on " + channel.getService().getName() + " :: "));

                            if(channel.isAnnounceGame()){
                                msg.appendSibling(new ChatComponentText("They are playing " + game + " :: "));
                            }
                            if(channel.isAnnounceTitle()){
                                msg.appendSibling(new ChatComponentText("The title is: " + title + " :: "));
                            }

                            if(channel.isAllowLink()) {
                                msg.appendSibling(new ChatComponentText("Click "));
                                IChatComponent link = new ChatComponentText("here");
                                link.getChatStyle().setColor(EnumChatFormatting.BLUE);
                                link.getChatStyle().setUnderlined(true);
                                link.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, channel.getService().getUrl() + channel.getChannelName()));
                                link.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(channel.getService().getUrl() + channel.getChannelName())));

                                msg.appendSibling(link);
                                msg.appendSibling(new ChatComponentText(" to watch"));
                            }

                            for (WorldServer w : MinecraftServer.getServer().worldServers) {
                                Functions.sendChatMessageServerWide(w, msg);
                            }
                        }
                    }
                }else{
                    if(liveChannels.contains(channel.getChannelName())){
                        liveChannels.remove(channel.getChannelName());
                        //No longer streaming.
                    }
                }
            }
        }
    };


    public void recheckChannels() {
        threadPoolExecutor.submit(channelChecker);
    }
}
