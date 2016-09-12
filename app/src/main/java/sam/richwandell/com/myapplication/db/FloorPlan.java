package sam.richwandell.com.myapplication.db;

import org.json.JSONException;
import org.json.JSONObject;

public class FloorPlan {

    JSONObject fp;

    public FloorPlan(JSONObject fp){
        this.fp = fp;
    }

    public int getId(){
        try {
            return this.fp.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getFloorPlanName(){
        try {
            return this.fp.getString("floorplanname");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName(){
        try {
            return this.fp.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString(){
        return this.fp.toString();
    }
}
