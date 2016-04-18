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
        ParamPlay playIf = rcCtx.getParamPlay();
        ParamMediaInfo mediaIf = rcCtx.getmediaPara();
        JSONObject jsnObj = new JSONObject();
        String usrOptTag = "playUrl";

        try {
            jsnObj.put("internetIp", playIf.getStrOutIp());// input from app server, net out ip address - nono
            jsnObj.put("dnsIp", sysInfo.getStrDnsIp());         // get from app local, current device used dns ip address - okok
            jsnObj.put("cdnIp", playIf.getStrCdnIp());         // get from app local, rtmp or http connect ip address - nono
            jsnObj.put("pingms", sysInfo.getSysNet().getPingMs(playIf.getStrDomain())); // get from app local - okok
            jsnObj.put("bindwidth", sysInfo.getSysNet().getAppNetRcvBw());            // get from app local - okok-0413

            if (ReportCenter.SDK_REPORTER_TYPE_PUBLISH == rcCtx.getReporterType()) {
                //strCtx.put("framesize", mediaIf.getWidth());      // get from app local - nono
                jsnObj.put("diskfreespace", String.valueOf(sysInfo.getSdcardRate())+"%");      // get from app local - nono
                jsnObj.put("width", mediaIf.getWidth());      // get from app local - nono
                jsnObj.put("height", mediaIf.getHeight());    // get from app local - nono
                jsnObj.put("currentfps", mediaIf.getFps());           // get from app local - nono
                jsnObj.put("bitrates", mediaIf.getBitRates());    // get from app local - nono
                jsnObj.put("lostfps", 10);        // get from app local - nono
                jsnObj.put("sendfailnum", 0);        // get from app local - nono
                usrOptTag = "pushUrl";
            }
            jsnObj.put(usrOptTag, playIf.getStrUrl());      // input from app server - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.w(TAG, "----end");
        return jsnObj;
    }
}
