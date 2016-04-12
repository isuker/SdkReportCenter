package sdk.report;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPlayHeartBeat {
    final static String TAG = "SdkReport_" + SysNetwork.class.getSimpleName();
    SystemInfo sysInfo = null;

    public TagPlayHeartBeat(SystemInfo sys) {
        sysInfo = sys;
    }


    public JSONObject toJson() {
//        Log.w(TAG, "----run");
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("internetip", sysInfo.getStrInternetIp());// input from app server, net out ip address - nono
            strCtx.put("dnsip", sysInfo.getStrDnsIp());         // get from app local, current device used dns ip address - okok
            strCtx.put("cdnip", sysInfo.getStrCdnIp());         // get from app local, rtmp or http connect ip address - nono
            strCtx.put("playurl", sysInfo.getStrPlayUrl());      // input from app server - nono
            strCtx.put("pingms", sysInfo.getSysNet().getPingMs(sysInfo.getStrUrlDomain())); // get from app local - okok
            strCtx.put("bindwidth", sysInfo.getSysNet().getAppNetSndBytes() * 8);            // get from app local - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.w(TAG, "----end");
        return strCtx;
    }

}
