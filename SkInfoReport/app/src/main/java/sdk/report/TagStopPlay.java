package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagStopPlay {
    private long playIntervalMin;

    public long getPlayIntervalMin() {
        return playIntervalMin;
    }

    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("interval", getPlayIntervalMin()); // get from stop-start ms, use minute - ok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

    public void setCostMs(long playMs) {
        playIntervalMin = (System.currentTimeMillis() - playMs) / 1000 / 60;
    }
}
