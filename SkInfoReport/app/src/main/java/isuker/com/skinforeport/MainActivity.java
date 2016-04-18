package isuker.com.skinforeport;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import java.io.IOException;

import sdk.report.ParamErr;
import sdk.report.ParamMediaInfo;
import sdk.report.ParamUser;
import sdk.report.ReportCenter;

public class MainActivity extends Activity {
    final String TAG = "SdkReport_" + MainActivity.class.getSimpleName(); // ReportCenter
    Context mContext;
//    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "main-on-create-run");
        mContext = this;

//        PakageInfoProvider provider = new PakageInfoProvider(this);
//        provider.getAppInfo();
//        return;
        Log.w(TAG, "main-on-create-report-start");
        playerReportInit(ReportCenter.SDK_REPORTER_TYPE_PLAY);
        playerReportSetStartTime();
        msSleep(100);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_START);
        msSleep(100);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_VIDEO_FIRST_FRAME);
        msSleep(20);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_VIDEO_FIRST_DISPLAY);
        msSleep(100);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_BEGIN);
        msSleep(1000);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_END);
        msSleep(2000);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_BEGIN);
        msSleep(1000);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_END);
        msSleep(100);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_PLAY_FAIL);

        int count = 0;
        while (true) {
            try {
                Log.w(TAG, "main-on-create-start-ping");
                Process p = Runtime.getRuntime().exec("ping -c 5 -i 0.2 " + "devimages.apple.com");
//                p = Runtime.getRuntime().exec("wget http://www.xuebuyuan.com/1726264.html");
//                p = Runtime.getRuntime().exec("wget http://blog.csdn.net/heng615975867/article/details/22812671");
                Log.w(TAG, "main-on-create-stop-ping");
            } catch (IOException e) {
                e.printStackTrace();
            }
            msSleep(1 * 1000);
            if (count++ > 50) {
                break;
            }
        }
        msSleep(40 * 1000);
        playerReportMsg(ReportCenter.SDK_REPORT_MSG_STOP);
        Log.w(TAG, "main-on-create-report-end");

    }

    private String getRemoteIpAddress() {
        return "1.2.3.4";
    }

    private int getVideoWidth() {
        return 720;
    }

    private int getVideoHeight() {
        return 1280;
    }

    private int getVideoFps() {
        return 25;
    }

    private int getBitRate() {
        return 300 * 1000;
    }

    private int getErrorCode() {
        return 1000;
    }

    //==suker-2016-0412 add player report <<==========================================================
    //==============================================================================================
    private ReportCenter playReportCtx = null;
    String playerOriUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
    String token = "rtmp://www.baidu.com/myapp";
    private String playerStreamUrl = "1234";

    private void playerReportInit(int reporter) {
        if (null == playReportCtx) {
            playReportCtx = new ReportCenter(mContext, reporter);
        }
        Log.w(TAG, "report-msg-init-handler:" + playReportCtx);
    }

//    private void playerReportRelease() {
//        Log.w(TAG, "report-msg-release-handler:" + playReportCtx);
//        playReportCtx = null;
//    }

    private void playerReportSetStartTime() {
        Log.w(TAG, "report-msg-start-set-time-handler:" + playReportCtx);
        if (null == playReportCtx) {
            return;
        }
        playReportCtx.setUserStartTime(System.currentTimeMillis());
    }

    private void playerReportMsg(int msgId) {
        Log.w(TAG, "report-msg-do-handler:" + playReportCtx + ", msg:" + msgId);
        if (null == playReportCtx) {
            Log.w(TAG, "report-msg-error-ctx==null, msgid:" + msgId);
            return;
        }

        long curMs = System.currentTimeMillis();
        switch (msgId) {
            case ReportCenter.SDK_REPORT_MSG_START:
                playReportCtx.initCurPlayDat();
                ParamUser userDat = new ParamUser();
                userDat.setStrInUrl(playerOriUrl);
                userDat.setStrStreamUrl(playerStreamUrl);
                userDat.setStrConnectIp(getRemoteIpAddress());
                playReportCtx.postMsg(msgId, curMs, userDat);
                break;

            case ReportCenter.SDK_REPORT_MSG_PLAY_VIDEO_FIRST_FRAME:
                playReportCtx.setConnectRmtIp(getRemoteIpAddress());
                ParamMediaInfo paraVid = new ParamMediaInfo();
                paraVid.setFrameSize(128000);
                paraVid.setWidth(getVideoWidth());
                paraVid.setHeight(getVideoHeight());
                paraVid.setFps(getVideoFps());
                paraVid.setBitRates(getBitRate());
                Log.w(TAG, "First-Frame-framesize:" + paraVid.getFrameSize() + ", reso:" + paraVid.getWidth() + "*" + paraVid.getHeight() + ", fps:" + paraVid.getFps() + ", bt:" + paraVid.getBitRates());
                playReportCtx.postMsg(msgId, curMs, paraVid);
                break;

            case ReportCenter.SDK_REPORT_MSG_PLAY_VIDEO_FIRST_DISPLAY:
                playReportCtx.setPlayerFrameCome(true);
            case ReportCenter.SDK_REPORT_MSG_HEART_BEAT:
            case ReportCenter.SDK_REPORT_MSG_STOP:
                playReportCtx.postMsg(msgId, curMs, null);
                break;

            case ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_BEGIN:
            case ReportCenter.SDK_REPORT_MSG_PLAY_PAUSE_END:
                if (playReportCtx.isPlayerFrameCome()) {
                    playReportCtx.postMsg(msgId, curMs, null);
                }
                break;


            case ReportCenter.SDK_REPORT_MSG_PLAY_FAIL:
                ParamErr playErr = new ParamErr();
                playErr.setiErrNo(getErrorCode());
                playErr.setStrErr(String.valueOf(getErrorCode()));
                playReportCtx.postMsg(msgId, curMs, playErr);
                break;
            default:
                break;
        }
    }
    //==suker-2016-0412 add player report >>==========================================================

    private void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

