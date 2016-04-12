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
            jsnObj.put("cpuinfo", sysInfo.getCpuJson()); // get from app local - okok
            jsnObj.put("meminfo", sysInfo.getMemJson()); // get from app local - okok
            jsnObj.put("bandwidth", bwsecper);           // get from app local - nono
            jsnObj.put("bwsec3aver", mBwSec3Aver);       // get from app local - nono
            jsnObj.put("bwsec10aver", mBwSec10Aver);     // get from app local - nono
            jsnObj.put("diskpencent", sdRate);           // get from app local - okok
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }
}
