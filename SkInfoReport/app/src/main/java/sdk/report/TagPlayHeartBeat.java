package sdk.report;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPlayHeartBeat {
    private final static String TAG = "SdkReport_" + SysNetwork.class.getSimpleName();
    private ReportCenter rcCtx = null;

    public TagPlayHeartBeat(ReportCenter rc) {
        rcCtx = rc;
    }

    public JSONObject toJson() {
//        Log.w(TAG, "----run");
        SystemInfo sysInfo = rcCtx.getSysInfo();
        ParamMediaInfo mediaIf = rcCtx.getmediaPara();
        JSONObject strCtx = new JSONObject();

        try {
            strCtx.put("internetip", sysInfo.getStrInternetIp());// input from app server, net out ip address - nono
            strCtx.put("dnsip", sysInfo.getStrDnsIp());         // get from app local, current device used dns ip address - okok
            strCtx.put("cdnip", sysInfo.getStrCdnIp());         // get from app local, rtmp or http connect ip address - nono
            strCtx.put("playurl", sysInfo.getStrPlayUrl());      // input from app server - nono
            strCtx.put("pingms", sysInfo.getSysNet().getPingMs(sysInfo.getStrUrlDomain())); // get from app local - okok
            strCtx.put("bindwidth", sysInfo.getSysNet().getAppNetRcvBw());            // get from app local - okok-0413

            if (ReportCenter.REPORTER_TYPE_PUBLISH == sysInfo.getReporterType()) {
                strCtx.put("width", mediaIf.getWidth());      // get from app local - nono
                strCtx.put("height", mediaIf.getHeight());    // get from app local - nono
                strCtx.put("currentfps", mediaIf.getFps());           // get from app local - nono
                strCtx.put("bitrates", mediaIf.getBitRates());    // get from app local - nono
                strCtx.put("lostfps", 10);        // get from app local - nono
                strCtx.put("sendfail", 0);        // get from app local - nono
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.w(TAG, "----end");
        return strCtx;
    }

}
