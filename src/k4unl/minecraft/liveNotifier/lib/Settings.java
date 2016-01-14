package k4unl.minecraft.liveNotifier.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen Beckers (K-4U)
 */
public class Settings {

    private List<LiveChannel> channels = new ArrayList<LiveChannel>();
    private List<String>      filters  = new ArrayList<String>();
    private int               delay    = 10;

    public List<LiveChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<LiveChannel> channels) {
        this.channels = channels;
    }

    public void addFilter(String filter) {
        if (!filter.equals(""))
            filters.add(filter);

    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void clear() {
        filters.clear();
        channels.clear();
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
