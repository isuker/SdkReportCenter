package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPlayFail {
    private int failNo;
    private String failMsg;
    private ReportCenter rcCtx = null;

    public TagPlayFail(ReportCenter rc) {
        rcCtx = rc;
    }

    public int getFailNo() {
        return failNo;
    }

    public void setFailNo(int failNo) {
        this.failNo = failNo;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg;
    }


    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();

        try {
            jsnObj.put("playUrl", rcCtx.getParamPlay().getStrUrl());  // input from app server - nono
            jsnObj.put("failCode", getFailNo());      // get from app local - nono
            jsnObj.put("failMessage", getFailMsg());    // get from app local - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
//"playUrl": "",
//"failCode": "",
//"failMessage": ""
}
