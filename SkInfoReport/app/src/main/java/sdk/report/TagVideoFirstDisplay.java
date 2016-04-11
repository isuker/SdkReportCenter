package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagVideoFirstDisplay {
    long firstDisplayCostMs;

    public long getFirstDisplayCostMs() {
        return firstDisplayCostMs;
    }

    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("firstdisplaycostms", getFirstDisplayCostMs());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

    public void setCostMs(long playMs) {
        firstDisplayCostMs = (System.currentTimeMillis() - playMs);
    }
}
