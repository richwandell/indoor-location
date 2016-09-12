package sam.richwandell.com.myapplication.upnp;


public interface UPnPListener {
    void onDiscover(UPnP.HeaderParser.UPnPDevice device);
}
