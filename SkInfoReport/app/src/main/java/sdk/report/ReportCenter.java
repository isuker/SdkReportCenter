package sdk.report;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-7.
 */
public class ReportCenter {
    final String TAG = "SdkReport_" + SystemInfo.class.getSimpleName(); // ReportCenter
    // final String TAG2 = "SdkReport_" + SystemInfo.class.getCanonicalName();　// sdk.report.ReportCenter
    // final String TAG4 = "SdkReport_" + SystemInfo.class.getName();　// sdk.report.ReportCenter

    final int SDK_REPORT_HEART_BEAT_MIN_INTERVAL = 1000;
    final int SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL = (30 * 1000); // 30 seconds report heartbeat
    final int SDK_REPORT_HEART_BEAT_MAX_INTERVAL = (10 * 60 * 1000);

    public static final int SDK_REPORT_PLAY_START_PLAY = 10;
    public static final int SDK_REPORT_PLAY_VIDEO_FIRST_FRAME = 20;
    public static final int SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY = 30;
    public static final int SDK_REPORT_PLAY_HEART_BEAT = 40;
    public static final int SDK_REPORT_PLAY_PAUSE_BEGIN = 50;
    public static final int SDK_REPORT_PLAY_PAUSE_END = 60;
    public static final int SDK_REPORT_PLAY_FAIL = 70;
    public static final int SDK_REPORT_PLAY_STOP_PLAY = 88;
    //==============================================================================================
    Context activityCtx;
    TagHeader playHeader = null;

    TagStartPlay startPlay = null;
    TagVideoFirstFrame vidFirstFrame = null;
    TagVideoFirstDisplay vidFirstDisplay = null;
    TagPlayHeartBeat heartBeat = null;
    TagPauseBegin pauseBegin = null;
    TagPauseEnd pauseEnd = null;
    TagPlayFail playFail = null;
    TagStopPlay stopPlay = null;
    SystemInfo sysInfo = null;
    SocketManager socketHandle = null;
    playHeartBeatThread hbThread = null;
    long startPlayMs = 0;
    boolean playerRun = false;
    int heartBeatInterval = SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL;

    // =============================================================================================
    public ReportCenter(Context ctx) {
        activityCtx = ctx;
        sysInfo = new SystemInfo(ctx);
        startPlay = new TagStartPlay(sysInfo);
        vidFirstFrame = new TagVideoFirstFrame();
        vidFirstDisplay = new TagVideoFirstDisplay();
        heartBeat = new TagPlayHeartBeat(sysInfo);
        pauseBegin = new TagPauseBegin();
        pauseEnd = new TagPauseEnd(sysInfo);
        playFail = new TagPlayFail();
        stopPlay = new TagStopPlay();
        socketHandle = new SocketManager();
        hbThread = new playHeartBeatThread();
    }

    // =============================================================================================
    public class playHeartBeatThread extends Thread {
        public void run() {
            Log.w(TAG, "thread-play-heartbeat-run");
            while (true) {
                if (!playerRun) {
                    Log.w(TAG, "thread-play-heartbeat-ext");
                    return;
                }
                Log.w(TAG, "handle-thread-play-heartbeat-bef");
                reportData(SDK_REPORT_PLAY_HEART_BEAT);
                sysInfo.getSysNet().showNetSpeed();

                if (!playerRun) {
                    Log.w(TAG, "thread-play-heartbeat-ext");
                    return;
                }

                try {
                    Thread.sleep(heartBeatInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // =============================================================================================
    public class ReportData {
        JSONObject objJson;
        String strTitle;

        public ReportData(JSONObject jsnObj, String title) {
            objJson = jsnObj;
            strTitle = title;
        }
    }
    // =============================================================================================

    public void reportData(int type) {
        final int inType = type;
        new Thread() {
            public void run() {
//                JSONObject jsnContent = null;
                playHeader = new TagHeader(sysInfo, "12345", "abcdef");
                JSONObject jsnHeader = playHeader.toJson();
                JSONObject jsnTag = new JSONObject();
                JSONObject jsnSender = new JSONObject();
//                String strTitle = null;
                ReportData dat = null;
                switch (inType) {
                    case SDK_REPORT_PLAY_START_PLAY:
                        dat = onReportStartPlay();
                        break;

                    case SDK_REPORT_PLAY_VIDEO_FIRST_FRAME:
                        dat = onReportVideoFirstFrame();
                        break;
                    case SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY:
                        dat = onReportVideoFirstDisplay();
                        break;
                    case SDK_REPORT_PLAY_HEART_BEAT:
                        dat = onReportPlayHeartBeat();
                        break;
                    case SDK_REPORT_PLAY_PAUSE_BEGIN:
                        dat = onReportPlayPauseBegin();
                        break;
                    case SDK_REPORT_PLAY_PAUSE_END:
                        dat = onReportPlayPauseEnd();
                        break;
                    case SDK_REPORT_PLAY_FAIL:
                        dat = onReportPlayFail();
                        break;

                    case SDK_REPORT_PLAY_STOP_PLAY:
                        dat = onReportStopPlay();
                        break;
                    default:
                        return;
                }

                try {
                    jsnTag.put("header", jsnHeader);
                    jsnTag.put("content", dat.objJson);
                    jsnSender.put(dat.strTitle, jsnTag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String inJson = jsnSender.toString();
                Log.w(TAG, "========================================" + inJson);

                if (needUdpSocket(inType)) {
                    Log.w(TAG, "udp-send-start");
                    //socketHandle.udpSend(inJson);
                    Log.w(TAG, "udp-send-end");
                } else {
                    Log.w(TAG, "tcp-send-start");
                    //socketHandle.tcpSend(inJson);
                    Log.w(TAG, "tcp-send-end");
                }
            }
        }.start();
    }

    // =============================================================================================
    public ReportData onReportStartPlay() {
        if (playerRun) {
            return null;
        }
        startPlayMs = playHeader.getStartPlayTm();
        ReportData data = new ReportData(startPlay.toJson(), "startplay");
        playerRun = true;
        hbThread.start();
        return data;
    }

    public ReportData onReportVideoFirstFrame() {
        if (!playerRun) {
            return null;
        }
        vidFirstFrame.setData(34500, 720, 1280, 20, 300 * 1000);
        return (new ReportData(vidFirstFrame.toJson(), "videofirstframe"));
    }

    public ReportData onReportVideoFirstDisplay() {
        if (!playerRun) {
            return null;
        }
        vidFirstDisplay.setCostMs(startPlayMs);
        return (new ReportData(vidFirstDisplay.toJson(), "videofirstdisplay"));
    }

    public ReportData onReportPlayHeartBeat() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(heartBeat.toJson(), "heartbeat"));
    }

    public ReportData onReportPlayPauseBegin() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(pauseBegin.toJson(), "pausebegin"));
    }

    public ReportData onReportPlayPauseEnd() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(pauseEnd.toJson(), "pauseend"));
    }

    public ReportData onReportPlayFail() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(playFail.toJson(), "playfail"));
    }

    public ReportData onReportStopPlay() {
        if (!playerRun) {
            return null;
        }
        stopPlay.setCostMs(startPlayMs);
        playerRun = false;
        return (new ReportData(stopPlay.toJson(), "stopplay"));
    }

    // =============================================================================================
    private boolean needUdpSocket(int type) {
        switch (type) {
            case SDK_REPORT_PLAY_START_PLAY:
            case SDK_REPORT_PLAY_VIDEO_FIRST_FRAME:
            case SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY:
            case SDK_REPORT_PLAY_FAIL:
            case SDK_REPORT_PLAY_STOP_PLAY:
                return false;

            case SDK_REPORT_PLAY_HEART_BEAT:
            case SDK_REPORT_PLAY_PAUSE_BEGIN:
            case SDK_REPORT_PLAY_PAUSE_END:
            default:
                break;
        }
        return true;
    }

    // =============================================================================================
    public void initPlay(String url, String inIp, int heartBeatItv) {
        if ((heartBeatItv < SDK_REPORT_HEART_BEAT_MIN_INTERVAL)
                || (heartBeatItv > SDK_REPORT_HEART_BEAT_MAX_INTERVAL)) {
            heartBeatItv = SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL;
        }
        heartBeatInterval = heartBeatItv;

        if (null == sysInfo) {
            sysInfo = new SystemInfo(activityCtx);
        }
        sysInfo.setStrPlayUrl(url);
        sysInfo.setStrInternetIp(inIp);
        // dns-ip, cdn-ip local get
    }


}
