package sdk.report;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagHeader {
    private ReportCenter rcCtx = null;
    private SystemInfo sysInfo = null;
    private String strImei;

    TagHeader(ReportCenter rc, String streamId, String token) {
        rcCtx = rc;
        sysInfo = rc.getSysInfo();
        strImei = null;
    }

    public String getStrImei() {
        if (null == strImei) {
            return sysInfo.getSysImei();
        }
        return strImei;
    }

    public String getStrPlatform() {
        return "android";
    }

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();

        try {
            jsnObj.put("appName", sysInfo.getStrAppName());  // get from local - ok
            jsnObj.put("platform", getStrPlatform());        // get from app local - okok
            jsnObj.put("streamUrl", rcCtx.getParamPlay().getStrStreamUrl());     // get from server-input - nono
            jsnObj.put("deviceNo", getStrImei());         // get from local - ok
            jsnObj.put("token", rcCtx.getParamPlay().getStrToken());           // get from server-input - nono
            jsnObj.put("bundleIdentifier", sysInfo.getStrPkgName());         // get from local - ok
            jsnObj.put("time", System.currentTimeMillis()/1000);              // get from local - ok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }

}

//"appName": "",           //APP名称
//"platform": "",          //平台 ios/android
//"time": 1460608542,      //秒时间戳
//"streamId": "",          //流ID
//"deviceNo": "",          //设备号
//"token": "",             //访问token
//"bundleIdentifier": ""   //应用绑定包名称
