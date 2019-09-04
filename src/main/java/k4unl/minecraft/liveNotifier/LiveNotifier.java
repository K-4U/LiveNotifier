package k4unl.minecraft.liveNotifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import k4unl.minecraft.k4lib.commands.CommandsRegistry;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.liveNotifier.commands.LiveNotifierCommands;
import k4unl.minecraft.liveNotifier.events.EventHelper;
import k4unl.minecraft.liveNotifier.lib.Beam;
import k4unl.minecraft.liveNotifier.lib.Filter;
import k4unl.minecraft.liveNotifier.lib.LiveChannel;
import k4unl.minecraft.liveNotifier.lib.Log;
import k4unl.minecraft.liveNotifier.lib.Settings;
import k4unl.minecraft.liveNotifier.lib.StreamingService;
import k4unl.minecraft.liveNotifier.lib.Twitch;
import k4unl.minecraft.liveNotifier.lib.config.ModInfo;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ModInfo.ID)
public class LiveNotifier {

	private static final ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("ChannelChecker #%d").setDaemon(true).build());
	public static LiveNotifier instance;
	public Settings settings = new Settings();
	public List<String> liveChannels = new ArrayList<>();
	private Path suggestedConfigurationFile;
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

						if (doAdd) {
							//Started streaming!
							liveChannels.add(channel.getChannelName());
							if (channel.isAnnounce()) {
								//Announce it:
								StringTextComponent msg = new StringTextComponent("");
								msg.appendSibling(new StringTextComponent(channel.getChannelName().substring(0, 1).toUpperCase() + channel.getChannelName().substring(1) + " has started streaming on " + channel.getService().getName() + " :: "));

								if (channel.isAnnounceGame()) {
									msg.appendSibling(new StringTextComponent("They are playing " + game + " :: "));
								}
								if (channel.isAnnounceTitle()) {
									msg.appendSibling(new StringTextComponent("The title is: " + title + " :: "));
								}

								if (channel.isAllowLink()) {
									msg.appendSibling(new StringTextComponent("Click "));
									ITextComponent link = new StringTextComponent("here");
									link.getStyle().setColor(TextFormatting.BLUE);
									link.getStyle().setUnderlined(true);
									link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, channel.getService().getUrl() + channel.getChannelName()));
									link.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(channel.getService().getUrl() + channel.getChannelName())));

									msg.appendSibling(link);
									msg.appendSibling(new StringTextComponent(" to watch"));
								}

								Functions.sendChatMessageServerWide(msg);
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

	public LiveNotifier() {
		LiveNotifier.instance = this;
		Log.init();

		suggestedConfigurationFile = FMLPaths.CONFIGDIR.get();
		readChannelsFromFile();
		saveChannelsToFile();

		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		EventHelper.init();
		MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
	}

	@SubscribeEvent
	public void onServerStart(FMLServerStartedEvent event) {

		boolean b = event.getServer() instanceof DedicatedServer;
		CommandsRegistry commandsRegistry = new LiveNotifierCommands(b, event.getServer().getCommandManager().getDispatcher());
	}

	public void readChannelsFromFile() {
		settings.clear();
		File dir = new File(suggestedConfigurationFile.toString() + "/" + ModInfo.ID + ".json");
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
		File dir = new File(suggestedConfigurationFile.toString() + "/" + ModInfo.ID + ".json");
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

	private boolean checkFilters(String title, LiveChannel channel) {
		List<Filter> toCheck;
		if (channel.getOverrideFilter() || settings.getFilters().size() == 0) {
			toCheck = channel.getFilters();
		} else {
			toCheck = settings.getFilters();
		}
		for (Filter filter : toCheck) {
			if (filter.matches(title)) {
				return true;
			}
		}

		return false;
	}

	public void recheckChannels() {
		threadPoolExecutor.submit(channelChecker);
	}
}
