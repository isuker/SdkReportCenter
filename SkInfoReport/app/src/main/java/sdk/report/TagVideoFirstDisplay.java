package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagVideoFirstDisplay {
    private long firstDisplayCostMs;

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        try {
            jsnObj.put("loadingDuration", firstDisplayCostMs);    // get from app local - okok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }

    public void setCostMs(long playMs) {
        firstDisplayCostMs = (System.currentTimeMillis() - playMs);
    }

//    "loadingDuration": 20   //加载时长，单位秒
}
