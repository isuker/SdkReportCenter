package isuker.com.skinforeport;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import java.io.IOException;

import sdk.report.ParamErr;
import sdk.report.ParamMediaInfo;
import sdk.report.ParamPlay;
import sdk.report.ReportCenter;

public class MainActivity extends Activity {
    final String TAG = "SdkReport_" + MainActivity.class.getSimpleName(); // ReportCenter
    ReportCenter reportInfo;
    Context mContext;
    String token = "rtmp://www.baidu.com/myapp";

    VideoView videoView;
    String playUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
    // "rtmp://www.baidu.com/myapp"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "main-on-create-run");
        mContext = this;

        Log.w(TAG, "main-on-create-report-start");
        playerReportPlay(ReportCenter.REPORTER_TYPE_PLAY);
        msSleep(100);
        playerReportFirstDisplay();
        msSleep(100);
        playerReportFirstFrame();
        msSleep(100);
        playerReportPauseBegin();
        msSleep(100);
        playerReportPauseEnd();
        msSleep(100);
        playerReportError();
        msSleep(100);

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
//                msSleep(40 * 1000);
        playerReportStop(ReportCenter.REPORTER_TYPE_PLAY);

        Log.w(TAG, "main-on-create-report-end");

    }

    //==suker-2016-0412 add player report >>==========================================================
    //==suker-2016-0412 add player report >>==========================================================
    //==suker-2016-0412 add player report >>==========================================================
    private String getRemoteIpAddress() {
        return "1.2.3.4";
    }

    private String getCdnName() {
        return "www.ws.com";
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

    //==suker-2016-0412 add player report >>==========================================================
    public void setReportHbMs(int setMs) {
        playerReportHbMs = setMs;
    }
    ReportCenter playerReportCtx = null;
    String playerOriUrl = playUrl;
    String playerStreamIdx = "1234";
    int playerReportHbMs = 30 * 1000;

    private void playerReportPlay(int reporter) {
        if (null == playerReportCtx) {
            playerReportCtx = new ReportCenter(mContext, reporter);
        }

        ParamPlay paraPlay = new ParamPlay();
        paraPlay.setStrUrl(playerOriUrl);
        paraPlay.setStrToken(token);
        paraPlay.setStrStreamId(playerStreamIdx);
        paraPlay.setStrOutIp(getRemoteIpAddress());
        paraPlay.setStrCdnName(getCdnName());
        paraPlay.setiHeartBeatIvt(playerReportHbMs);
        int reportMsg = ReportCenter.SDK_REPORT_PLAY_START_PLAY;
        if (ReportCenter.REPORTER_TYPE_PUBLISH == reporter) {
            reportMsg = ReportCenter.SDK_EVENT_REPORT_START_PUBLISH;
        }
        playerReportCtx.reportData(reportMsg, paraPlay);
    }

    private void playerReportFirstFrame() {
        if (null == playerReportCtx) {
            return;
        }

        ParamMediaInfo paraVid = new ParamMediaInfo();
        paraVid.setFrameSize(128000);
        paraVid.setWidth(getVideoWidth());
        paraVid.setHeight(getVideoHeight());
        paraVid.setFps(getVideoFps());
        paraVid.setBitRates(getBitRate());
        playerReportCtx.reportData(ReportCenter.SDK_REPORT_PLAY_VIDEO_FIRST_FRAME, paraVid);
    }

    private void playerReportFirstDisplay() {
        if (null == playerReportCtx) {
            return;
        }
        playerReportCtx.reportData(ReportCenter.SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY, null);
    }

    private void playerReportPauseBegin() {
        if (null == playerReportCtx) {
            return;
        }
        playerReportCtx.reportData(ReportCenter.SDK_REPORT_PLAY_PAUSE_BEGIN, null);
    }

    private void playerReportPauseEnd() {
        if (null == playerReportCtx) {
            return;
        }
        playerReportCtx.reportData(ReportCenter.SDK_REPORT_PLAY_PAUSE_END, null);
    }

    private void playerReportError() {
        if (null == playerReportCtx) {
            return;
        }

        ParamErr playErr = new ParamErr();
        playErr.setiErrNo(getErrorCode());
        playErr.setStrErr(String.valueOf(getErrorCode()));
        playerReportCtx.reportData(ReportCenter.SDK_REPORT_PLAY_FAIL, playErr);

    }

    private void playerReportStop(int reporter) {
        if (null == playerReportCtx) {
            return;
        }
        int reportMsg = ReportCenter.SDK_REPORT_PLAY_STOP_PLAY;
        if (ReportCenter.REPORTER_TYPE_PUBLISH == reporter) {
            reportMsg = ReportCenter.SDK_EVENT_REPORT_STOP_PUBLISH;
        }
        playerReportCtx.reportData(reportMsg, null);
    }

    private void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


//        new Thread() {
//            public void run() {
//                try {
//                    //Process p = Runtime.getRuntime().exec("ping -c 5 -i 0.2 " + "devimages.apple.com");
//                    Log.w(TAG, "main-on-create-start-ping");
//                    Process p = Runtime.getRuntime().exec("ping  -i 0.2 " + "devimages.apple.com");
//                    Log.w(TAG, "main-on-create-stop-ping");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };


//        videoView = (VideoView)this.findViewById(R.id.video_view);
////        new Thread() {
////            public void run() {
//                Log.w(TAG, "main-on-create-video-view-start");
//                Uri uri = Uri.parse(playUrl);
//                //Uri uri = Uri.parse("rtsp://v2.cache2.c.youtube.com/CjgLENy73wIaLwm3JbT_%ED%AF%80%ED%B0%819HqWohMYESARFEIJbXYtZ29vZ2xlSARSB3Jlc3VsdHNg_vSmsbeSyd5JDA==/0/0/0/video.3gp");
////                videoView = (VideoView)this.findViewById(R.id.video_view);
//                videoView.setMediaController(new MediaController(activicyMain));
//                videoView.setVideoURI(uri);
//                videoView.start();
////                videoView.requestFocus();
//                Log.w(TAG, "main-on-create-video-view-end");
////            }
////            };

