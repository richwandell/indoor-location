package sam.richwandell.com.myapplication.db;

import org.json.JSONException;
import org.json.JSONObject;

public class FloorPlan {

    private JSONObject fp;

    private int rotation = 0;

    private boolean rotationParsed = false;

    public FloorPlan(JSONObject fp){
        this.fp = fp;
    }

    public int getRotation(){
        if(rotationParsed) return rotation;
        try{
            JSONObject layoutImage = this.fp.getJSONObject("layout_image");
            rotation = layoutImage.getInt("rotation");
            rotationParsed = true;
            return rotation;
        }catch(JSONException e){
            return 0;
        }
    }

    public String getId(){
        try {
            return this.fp.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFloorPlanName(){
        try {
            JSONObject layoutImage = this.fp.getJSONObject("layout_image");
            return layoutImage.getString("floorplanname");
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
