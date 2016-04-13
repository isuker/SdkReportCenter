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
    private SystemInfo sysInfo = null;
    //==============================================================================================

    public TagStartPlay(SystemInfo sys) {
        sysInfo = sys;
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
            strAppVerCode = sysInfo.getAppVerName();
        }
        return strAppVerCode;
    }

    public String getStrAppVerCode() {
        if (null == strAppVerName) {
            strAppVerName = sysInfo.getAppVerCode();
        }
        return strAppVerName;
    }

    public String getStrPlatform() {
        return "android";
    }


    public JSONObject toJson() {
        JSONObject strCtx = new JSONObject();
        try {
            Log.w("SdkReport_test", "dns-ip:" + sysInfo.getStrDnsIp());
            strCtx.put("pkgname", sysInfo.getStrPkgName());  // get from app local - okok
            strCtx.put("internetip", sysInfo.getStrInternetIp());// input from app server, net out ip address - nono
            strCtx.put("dnsip", sysInfo.getStrDnsIp());      // get from app local, current device used dns ip address - okok
            strCtx.put("cdnip", sysInfo.getStrCdnIp());      // get from app local, rtmp or http connect ip address - nono
            strCtx.put("manufacturer", getStrManufacturer());// get from app local - okok
            strCtx.put("model", getStrModel());              // get from app local - okok
            strCtx.put("product", getStrProduct());          // get from app local - okok
            strCtx.put("board", getStrBoard());              // get from app local - okok
            strCtx.put("cpuarch", getStrCpuArch());          // get from app local - okok
            strCtx.put("osver", getStrOsVer());              // get from app local - okok
            strCtx.put("sdkver", getStrSdkVer());            // get from app local - okok
            strCtx.put("appvername", getStrAppVerName());    // get from app local - okok
            strCtx.put("appvercode", getStrAppVerCode());    // get from app local - okok
            strCtx.put("playurl", sysInfo.getStrPlayUrl());  // input from app server - nono
            strCtx.put("platform", getStrPlatform());        // get from app local - okok

            if (ReportCenter.REPORTER_TYPE_PUBLISH == sysInfo.getReporterType()) {
                strCtx.put("publisher", "hupu");     // get from app local - nono
                strCtx.put("defaultfps", 25);        // get from app local - nono
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

}
