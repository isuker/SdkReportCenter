package sdk.report;

import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-6.
 */
public class TagStartPlay {
    String strAppVerName;
    String strAppVerCode;
    SystemInfo sysInfo = null;
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
            strCtx.put("internetip", sysInfo.getStrInternetIp());
            strCtx.put("dnsip", sysInfo.getStrDnsIp());
            strCtx.put("cdnip", sysInfo.getStrCdnIp());
            strCtx.put("manufacturer", getStrManufacturer());
            strCtx.put("model", getStrModel());
            strCtx.put("product", getStrProduct());
            strCtx.put("board", getStrBoard());
            strCtx.put("cpuarch", getStrCpuArch());
            strCtx.put("osver", getStrOsVer());
            strCtx.put("sdkver", getStrSdkVer());
            strCtx.put("appvername", getStrAppVerName());
            strCtx.put("appvercode", getStrAppVerCode());
            strCtx.put("playurl", sysInfo.getStrPlayUrl());
            strCtx.put("platform", getStrPlatform());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strCtx;
    }

}
