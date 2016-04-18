package sdk.report;

import android.util.Log;

/**
 * Created by suker on 16-4-13.
 */
public class ParamPlay {
    private String TAG = "SdkReport_" + ParamPlay.class.getSimpleName();

    private String strUrl;
    private String strDomain;
    private String strToken;
    private String strOutIp;
    private String strStreamUrl;
    private String strCdnIp;
    private int iHeartBeatIvt;


    public String getStrUrl() {
        return strUrl;
    }

    public void setStrUrl(String strUrl) {
        this.strUrl = strUrl;
    }

    public String getStrToken() {
        return strToken;
    }

    public void setStrToken(String strToken) {
        this.strToken = strToken;
    }

    public String getStrOutIp() {
        return strOutIp;
    }

    public void setStrOutIp(String strOutIp) {
        this.strOutIp = strOutIp;
    }

    public String getStrStreamUrl() {
        return strStreamUrl;
    }

    public void setStrStreamUrl(String strStreamUrl) {
        this.strStreamUrl = strStreamUrl;
    }

    public String getStrCdnIp() {
        return strCdnIp;
    }

    public void setStrCdnIp(String strCdnName) {
        this.strCdnIp = strCdnIp;
    }

    public int getiHeartBeatIvt() {
        return iHeartBeatIvt;
    }

    public void setiHeartBeatIvt(int iHeartBeatIvt) {
        this.iHeartBeatIvt = iHeartBeatIvt;
    }

    public String getStrDomain() {
        if (null == strDomain) {
            setStrDomain(strUrl);
        }
        return strDomain;
    }

    public void setStrDomain(String strUrl) {
        String domainStart = null;
        //Log.i(TAG, "+++++++++++++++++++++++set-play-url:" + strPlayUrl);
        int hdrPos = strUrl.indexOf("://");
        //int hdrPos = strPlayUrl.indexOf("%3A%2F%2F");
        int endPos = 0;

        if (-1 == hdrPos) {
            return;
        }
        Log.i(TAG, "+++++++++++++++++++++++domain-start:" + hdrPos);
        domainStart = strUrl.substring(hdrPos + 3);
        endPos = domainStart.indexOf("/");
        //endPos = domainStart.indexOf("%2F");
        if (-1 != endPos) {
            this.strDomain = (domainStart.substring(0, endPos));
        }
        Log.i(TAG, "+++++++++++++++++++++++domain-name:" + strDomain);
    }

}
