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

    public String getPlayUrl() {
        return playUrl;
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
            strCtx.put("playurl", getPlayUrl());
            strCtx.put("failno", getFailNo());
            strCtx.put("failmsg", getFailMsg());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

}
