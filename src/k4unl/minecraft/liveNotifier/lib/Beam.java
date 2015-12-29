package k4unl.minecraft.liveNotifier.lib;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Beam {
    private static final String beamUrl = "https://beam.pro";
    private static final String apiUrl = "/api/v1";

    private static Gson nGson = new Gson();
    private static Type typeListMapStringObject = new TypeToken<List<Map<String, Object>>>() {
    }.getType();
    private static Type typeMapStringObject = new TypeToken<Map<String, Object>>() {
    }.getType();

    private static String generateURL(String... args) {
        String url = beamUrl + apiUrl;
        for (String arg : args) {
            url += "/" + arg;
        }
        return url;
    }

    private static String fetchJson(String url) {
        String jsonEnd = "";
        try {

            URL toFetch = new URL(url);
            Log.info("Fetching " + toFetch.toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(toFetch.openStream()));

            String line;
            while ((line = in.readLine()) != null) {
                jsonEnd += line;
                Log.info(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonEnd;
    }

    private static Map<String, Object> getMap(String url) {
        String json = fetchJson(url);

        return nGson.fromJson(json, typeMapStringObject);
    }

    private static List<Map<String, Object>> getList(String url) {
        String json = fetchJson(url);

        return nGson.fromJson(json, typeListMapStringObject);
    }

    private static List<Map<String, Object>> getListWithArgs(String url, Map<String, String> args) {
        List<String> end = new ArrayList<String>();
        for (Map.Entry<String, String> arg : args.entrySet()) {
            end.add(arg.getKey() + "=" + arg.getValue());
        }
        String json = fetchJson(url + "?" + Joiner.on("&").join(end));

        return nGson.fromJson(json, typeListMapStringObject);
    }


    private static Map<String, Object> getChannelFromId(int id) {
        Map<String, Object> channel = getMap(generateURL("channels", id + ""));

        return channel;
    }

    private static int getChannelIdFromOwnerId(int ownerId) {
        Map<String, Object> user = getUserFromId(ownerId);

        return (int) Math.floor(Double.valueOf(((Map<String, Object>) user.get("channel")).get("id").toString()));
    }

    private static Map<String, Object> getChannelFromOwnerId(int ownerId) {

        return getChannelFromId(getChannelIdFromOwnerId(ownerId));
    }

    private static Map<String, Object> getUserFromId(int id) {

        return getMap(generateURL("users", String.valueOf(id)));
    }

    public static Map<String, Object> getChannelFromOwnerName(String username) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("query", username);

        List<Map<String, Object>> search = getListWithArgs(generateURL("users", "search"), args);
        if (search == null) {
            return null;
        }
        Map<String, Object> firstResult = search.get(0);
        int userId = (int) Math.floor(Double.valueOf(firstResult.get("id").toString()));


        return getChannelFromOwnerId(userId);
    }

    public static boolean isLive(String channelName) {
        Map<String, Object> channel = getChannelFromOwnerName(channelName);

        Log.info("Fetched channel object");
        return Boolean.valueOf(channel.get("online").toString());
    }
}
