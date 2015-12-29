package k4unl.minecraft.liveNotifier.lib;

/**
 * @author Koen Beckers (K-4U)
 */
public enum StreamingService {
    TWITCH("http://www.twitch.tv/"), BEAM("http://www.beam.pro/");

    private String url;

    StreamingService(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getName(){
        return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
    }
}
