package k4unl.minecraft.liveNotifier.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Twitch {
    private static final String twitchAPI = "https://api.twitch.tv/kraken/";
    private static String twitchClientId = "";
    private static Gson nGson = new Gson();
    private static Type typeMapStringObject = new TypeToken<Map<String, Object>>() {}.getType();
    
    static {
        //Quickly fetch the client ID from the website of K-4U:
        Log.info("Fetching twitch Client ID from K-4U.eu");
        try{
            URL toFetch = new URL("http://www.k-4u.eu/twitchid.txt");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(toFetch.openStream()));
            
            String line;
            while((line = in.readLine()) != null){
                twitchClientId += line;
            }
            Log.info("Fetched Twitch ID: " + twitchClientId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Map<String, Object> getChannelData(String channelName){
        String json = fetchJson("channels/" + channelName);

        return nGson.fromJson(json, typeMapStringObject);
    }


    private static String fetchJson(String url){
        String jsonEnd = "";
        try {

            URL toFetch = new URL(twitchAPI + url);
            //Log.info("Fetching " + toFetch.toString());
            URLConnection urlConnection = toFetch.openConnection();
            urlConnection.addRequestProperty("Client-ID", twitchClientId);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            
            String line;
            while((line = in.readLine()) != null){
                jsonEnd += line;
                //Log.info(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketException e){
            Log.error("Twitch is not playing nice: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonEnd;
    }



    public static boolean isLive(String channelName) {
        String json = fetchJson("streams/" + channelName);
        Object streamData = ((Map<String, Object>)nGson.fromJson(json, typeMapStringObject)).get("stream");
        if(streamData == null){
            return false;
        }
        return true;
    }

    public static Map<String,Object> getStreamData(String channelName) {
        String json = fetchJson("streams/" + channelName);

        return (Map<String, Object>) ((Map<String, Object>)nGson.fromJson(json, typeMapStringObject)).get("stream");
    }
}
