package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPlayFail {
    private String playUrl;
    private int failNo;
    private String failMsg;
    SystemInfo sysInfo = null;

    public TagPlayFail(SystemInfo sys) {
        sysInfo = sys;
    }

    public int getFailNo() {
        return failNo;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("playurl", sysInfo.getStrPlayUrl());  // input from app server - nono
            strCtx.put("failno", getFailNo());      // get from app local - nono
            strCtx.put("failmsg", getFailMsg());    // get from app local - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

}
