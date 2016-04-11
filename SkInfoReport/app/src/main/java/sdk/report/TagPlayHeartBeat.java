package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPlayHeartBeat {
    SystemInfo sysInfo = null;

    public TagPlayHeartBeat(SystemInfo sys) {
        sysInfo = sys;
    }


    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            strCtx.put("internetip", sysInfo.getStrInternetIp());
            strCtx.put("dnsip", sysInfo.getStrDnsIp());
            strCtx.put("cdnip", sysInfo.getStrCdnIp());
            strCtx.put("playurl", sysInfo.getStrPlayUrl());
            strCtx.put("pingms", sysInfo.getSysNet().getPingMs());
            strCtx.put("bindwidth", sysInfo.getSysNet().getAppNetSndBytes() * 8);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

}
