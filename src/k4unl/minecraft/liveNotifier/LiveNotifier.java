package k4unl.minecraft.liveNotifier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.*;
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

    @Mod.Instance(value = ModInfo.ID)
    public static LiveNotifier instance;
    private       File         suggestedConfigurationFile;

    public Settings     settings     = new Settings();
    public List<String> liveChannels = new ArrayList<String>();

    private static final ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("ChannelChecker #%d").setDaemon(true).build());

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();
        suggestedConfigurationFile = event.getSuggestedConfigurationFile();
        readChannelsFromFile();
        saveChannelsToFile();
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        EventHelper.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {

        Commands.init(event);
    }


    public void readChannelsFromFile() {
        settings.clear();
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
                while ((line = bReader.readLine()) != null) {
                    json += line;
                }
                reader.close();
                ipStream.close();
                bReader.close();

                settings = gson.fromJson(json, Settings.class);
                if (settings == null) {
                    settings = generateExampleSettings();
                }

                //Log.info("Read from file: " + json);
            } catch (JsonSyntaxException e) {
                settings = generateExampleSettings();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private Settings generateExampleSettings() {
        Settings ret = new Settings();
        ret.setChannels(generateExampleChannel());
        ret.addFilter(".*(ftb).*", true);
        ret.setDelay(10);
        return ret;
    }

    private List<LiveChannel> generateExampleChannel() {
        LiveChannel toAdd = new LiveChannel("K4Unl", StreamingService.TWITCH, true, true, true, true, false);
        List<LiveChannel> ret = new ArrayList<LiveChannel>();

        ret.add(toAdd);
        return ret;
    }

    public void saveChannelsToFile() {
        File dir = suggestedConfigurationFile;
        if (dir != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(settings);
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
            for (LiveChannel channel : settings.getChannels()) {
                boolean isLive = false;
                String game = "";
                String title = "";
                if (channel.getService().equals(StreamingService.TWITCH)) {
                    isLive = Twitch.isLive(channel.getChannelName());
                    if (isLive) {
                        Map<String, Object> streamData = Twitch.getStreamData(channel.getChannelName());
                        game = streamData.get("game").toString();
                        title = ((Map<String, Object>) streamData.get("channel")).get("status").toString();
                    }
                }
                if (channel.getService().equals(StreamingService.BEAM)) {
                    isLive = Beam.isLive(channel.getChannelName());
                    if (isLive) {
                        Map<String, Object> streamData = Beam.getChannelFromOwnerName(channel.getChannelName());
                        game = ((Map<String, Object>) streamData.get("type")).get("name").toString();
                        title = streamData.get("name").toString();
                    }
                }

                if (isLive) {
                    if (!liveChannels.contains(channel.getChannelName())) {
                        //Check if the filter works.
                        boolean doAdd = true;
                        doAdd = checkFilters(title, channel);

                        if(doAdd) {
                            //Started streaming!
                            liveChannels.add(channel.getChannelName());
                            if (channel.isAnnounce()) {
                                //Announce it:
                                ChatComponentText msg = new ChatComponentText("");
                                msg.appendSibling(new ChatComponentText(channel.getChannelName().substring(0, 1).toUpperCase() + channel.getChannelName().substring(1) + " has started streaming on " + channel.getService().getName() + " :: "));

                                if (channel.isAnnounceGame()) {
                                    msg.appendSibling(new ChatComponentText("They are playing " + game + " :: "));
                                }
                                if (channel.isAnnounceTitle()) {
                                    msg.appendSibling(new ChatComponentText("The title is: " + title + " :: "));
                                }

                                if (channel.isAllowLink()) {
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
                    }
                } else {
                    if (liveChannels.contains(channel.getChannelName())) {
                        liveChannels.remove(channel.getChannelName());
                        //No longer streaming.
                    }
                }
            }
        }
    };

    private boolean checkFilters(String title, LiveChannel channel) {
        List<Filter> toCheck;
        if(channel.getOverrideFilter() || settings.getFilters().size() == 0){
            toCheck = channel.getFilters();
        }else{
            toCheck = settings.getFilters();
        }
        for(Filter filter : toCheck){
            if(filter.matches(title)){
                return true;
            }
        }

        return false;
    }

    public void recheckChannels() {
        threadPoolExecutor.submit(channelChecker);
    }
}
