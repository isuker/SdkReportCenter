package sdk.report;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagHeader {
    SystemInfo sysInfo = null;
    String strImei;
    String strStreamId;
    String strToken;
    long startPlayTm; // ms

    TagHeader(SystemInfo sysIf, String streamId, String token) {
        sysInfo = sysIf;
        strImei = null;
        strStreamId = streamId;
        strToken = token;
        startPlayTm = System.currentTimeMillis();
    }

    public String getStrImei() {
        if (null == strImei) {
            return sysInfo.getSysImei();
        }
        return strImei;
    }

    public String getStrStreamId() {
        return strStreamId;
    }

    public String getStrToken() {
        return strToken;
    }

    public long getStartPlayTm() {
        return startPlayTm;
    }

    public JSONObject toJson() {
        JSONObject strHdr = new JSONObject();
        try {
            strHdr.put("imei", getStrImei());
            strHdr.put("streamid", getStrStreamId());
            strHdr.put("token", getStrToken());
            strHdr.put("devtm", startPlayTm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strHdr;
    }

}
