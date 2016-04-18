package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPauseEnd {
    private ReportCenter rcCtx = null;

    public TagPauseEnd(ReportCenter rc) {
        rcCtx = rc;
    }

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        SystemInfo sysInfo = rcCtx.getSysInfo();
        ParamPlay playIf = rcCtx.getParamPlay();

        try {
            jsnObj.put("curcount", rcCtx.getCatonCurCount());
            jsnObj.put("totalcount", rcCtx.getCatonTotalCount());
            if (0 != rcCtx.getCatonCurSeconds()) { // caton start
                jsnObj.put("curseconds", rcCtx.getCatonCurSeconds());
                jsnObj.put("curtotalsec", rcCtx.getCatonCurTotalSec());
                jsnObj.put("catontotalsec", rcCtx.getCatonTotalSeconds());
            }

            jsnObj.put("cpuinfo", sysInfo.getCpuJson()); // get from app local - okok
            jsnObj.put("memoryinfo", sysInfo.getMemJson()); // get from app local - okok
            jsnObj.put("dnsIp", sysInfo.getStrDnsIp());
            jsnObj.put("cdnIp", playIf.getStrCdnIp());
            jsnObj.put("internetIp", playIf.getStrOutIp());
            jsnObj.put("pingms", sysInfo.getSysNet().getPingMs(playIf.getStrDomain())); // get from app local - okok
            jsnObj.put("bandwidth", sysInfo.getSysNet().getAppNetRcvBw()); // get from app local - okok-0413
            jsnObj.put("diskfreespace", String.valueOf(sysInfo.getSdcardRate()) + "%");           // get from app local - okok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
}


//"cpuinfo": {
//        "selfpercent": "40%",
//        "maxpercent": "40%",
//        "maxname": "com.xxx.xxx",
//        "totalpercent":" 70%"
//        },
//        "memoryinfo": {
//        "selfpercent": "40%",
//        "maxpercent": "40%",
//        "maxname": "com.xxx.xxx",
//        "totalpercent":" 70%"
//        },
//"dnsIp": "",
//        "sec3aver": 0,
//        "bindwidth": 0,
//        "diskfreespace": "",
//        "cdnIp": "",
//        "sec10aver": 0,
//        "pingms": 0,
//        "internetIp": ""