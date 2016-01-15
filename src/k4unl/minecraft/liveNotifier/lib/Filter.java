package k4unl.minecraft.liveNotifier.lib;

/**
 * @author Koen Beckers (K-4U)
 */
public class Filter {
    private boolean regex;
    private String filter;

    public Filter(String filter, boolean regex) {
        this.regex = regex;
        this.filter = filter;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean matches(String toCompare){
        if(this.isRegex()){
            return toCompare.toLowerCase().matches(this.getFilter());
        }else{
            return toCompare.toLowerCase().contains(this.getFilter().toLowerCase());
        }
    }
}
