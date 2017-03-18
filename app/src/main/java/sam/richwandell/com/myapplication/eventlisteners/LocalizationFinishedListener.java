package sam.richwandell.com.myapplication.eventlisteners;

import org.json.JSONObject;

public interface LocalizationFinishedListener {
    void onWifiLocalizationFinished(JSONObject wifiResult);
}
