package sdk.report;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagPauseEnd {
    int mBindWidth;
    int mBwSec3Aver;
    int mBwSec10Aver;
    SystemInfo sysInfo = null;

    public TagPauseEnd(SystemInfo info) {
        sysInfo = info;
    }

    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        int bwsecper = mBindWidth / 128;
        float sdRate = sysInfo.getSdcardRate();

        try {
            jsnObj.put("cpuinfo", sysInfo.getCpuJson());
            jsnObj.put("meminfo", sysInfo.getMemJson());
            jsnObj.put("bandwidth", bwsecper);
            jsnObj.put("bwsec3aver", mBwSec3Aver);
            jsnObj.put("bwsec10aver", mBwSec10Aver);
            jsnObj.put("diskpencent", sdRate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
}
