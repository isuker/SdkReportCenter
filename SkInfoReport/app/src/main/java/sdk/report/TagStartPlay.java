package sdk.report;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagStartPlay {
    private String strAppVerName;
    private String strAppVerCode;
    private ReportCenter rcCtx = null;
    //==============================================================================================

    public TagStartPlay(ReportCenter rc) {
        rcCtx = rc;
    }

    public String getStrManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getStrModel() {
        return Build.MODEL;
    }

    public String getStrProduct() {
        return Build.PRODUCT;
    }

    public String getStrBoard() {
        return Build.BOARD;
    }

    public String getStrCpuArch() {
        return Build.CPU_ABI;
    }

    public String getStrOsVer() {
        return Build.VERSION.RELEASE;
    }

    public String getStrSdkVer() {
        return Build.VERSION.SDK;
    }

    public String getStrAppVerName() {
        if (null == strAppVerCode) {
            strAppVerCode = rcCtx.getSysInfo().getAppVerName();
        }
        return strAppVerCode;
    }

    public String getStrAppVerCode() {
        if (null == strAppVerName) {
            strAppVerName = rcCtx.getSysInfo().getAppVerCode();
        }
        return strAppVerName;
    }


    public JSONObject toJson() {
        JSONObject jsnObj = new JSONObject();
        SystemInfo sysInfo = rcCtx.getSysInfo();
        ParamPlay playIf = rcCtx.getParamPlay();
        String usrOptTag = "playUrl";

        try {
            Log.w("SdkReport_test", "dns-ip:" + sysInfo.getStrDnsIp());

            jsnObj.put("product", getStrProduct());          // get from app local - okok
            jsnObj.put("model", getStrModel());              // get from app local - okok
            jsnObj.put("osVersion", getStrOsVer());              // get from app local - okok
            jsnObj.put("manufacturer", getStrManufacturer());// get from app local - okok
            jsnObj.put("cpuarch", getStrCpuArch());          // get from app local - okok
            jsnObj.put("cdnIp", playIf.getStrCdnIp());      // get from app local, rtmp or http connect ip address - nono
            jsnObj.put("dnsIp", sysInfo.getStrDnsIp());      // get from app local, current device used dns ip address - okok
            jsnObj.put("board", getStrBoard());              // get from app local - okok
            jsnObj.put("internetIp", playIf.getStrOutIp());// input from app server, net out ip address - nono
            jsnObj.put("sdkVersion", getStrSdkVer());            // get from app local - okok
            jsnObj.put("appVersion", getStrAppVerName() + "__" + getStrAppVerCode());    // get from app local - okok

            if (ReportCenter.SDK_REPORTER_TYPE_PUBLISH == rcCtx.getReporterType()) {
                usrOptTag = "pushUrl";
                jsnObj.put("pushName", "hupu");     // get from app local - nono
                jsnObj.put("defaultfps", 25);        // get from app local - nono
            }
            jsnObj.put(usrOptTag, playIf.getStrUrl());  // input from app server - nono
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsnObj;
    }

}
