package k4unl.minecraft.liveNotifier.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen Beckers (K-4U)
 */
public class Settings {

    private List<LiveChannel> channels = new ArrayList<LiveChannel>();
    private List<Filter>      filters  = new ArrayList<Filter>();
    private int               delay    = 10;

    public List<LiveChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<LiveChannel> channels) {
        this.channels = channels;
    }

    public void addFilter(String filter, boolean regex) {
        if (!filter.equals("")) {
            filters.add(new Filter(filter, regex));
        }
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
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
