package k4unl.minecraft.liveNotifier.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen Beckers (K-4U)
 */
public class LiveChannel {

    private String           channelName;
    private StreamingService service;
    private boolean      announce       = true;
    private boolean      announceGame   = true;
    private boolean      allowLink      = true;
    private boolean      announceTitle  = true;
    private List<Filter> filters        = new ArrayList<Filter>();
    private boolean      overrideFilter = false;

    public LiveChannel(String channelName, StreamingService service, boolean announce, boolean announceGame, boolean allowLink, boolean announceTitle, boolean overrideFilter) {
        this.channelName = channelName;
        this.service = service;
        this.announce = announce;
        this.announceGame = announceGame;
        this.allowLink = allowLink;
        this.announceTitle = announceTitle;
        this.overrideFilter = overrideFilter;

    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public StreamingService getService() {
        return service;
    }

    public void setService(StreamingService service) {
        this.service = service;
    }

    public boolean isAnnounce() {
        return announce;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    public boolean isAnnounceGame() {
        return announceGame;
    }

    public void setAnnounceGame(boolean announceGame) {
        this.announceGame = announceGame;
    }

    public boolean isAllowLink() {
        return allowLink;
    }

    public void setAllowLink(boolean allowLink) {
        this.allowLink = allowLink;
    }

    public boolean isAnnounceTitle() {
        return announceTitle;
    }

    public void setAnnounceTitle(boolean announceTitle) {
        this.announceTitle = announceTitle;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void addToFilter(String filter, boolean regex){
        this.filters.add(new Filter(filter, regex));
    }

    public boolean getOverrideFilter() {
        return overrideFilter;
    }
}
