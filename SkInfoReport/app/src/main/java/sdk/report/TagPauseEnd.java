package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPauseEnd {
    SystemInfo sysInfo = null;

    public TagPauseEnd(SystemInfo info) {
        sysInfo = info;
    }

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        try {
            jsnObj.put("cpuinfo", sysInfo.getCpuJson()); // get from app local - okok
            jsnObj.put("meminfo", sysInfo.getMemJson()); // get from app local - okok
            jsnObj.put("bandwidth", sysInfo.getSysNet().getAppNetRcvBw()); // get from app local - okok-0413
            jsnObj.put("diskpencent", sysInfo.getSdcardRate());           // get from app local - okok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
}
