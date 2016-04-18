package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by suker on 16-4-6.
 */
public class TagStopPlay {
    private double playIntervalMin;

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        try {
            jsnObj.put("playDuration", playIntervalMin); // get from stop-start ms, use minute - ok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }

    public void setCostMs(long playMs) {
        double minute = (double) (System.currentTimeMillis() - playMs) / (1000 * 60);
        BigDecimal b = new BigDecimal(minute);
        playIntervalMin = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

//    "playDuration": 0       //播放总时长，单位分钟
}
