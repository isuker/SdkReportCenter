package sdk.report;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-7.
 */
public class ReportCenter {
    final String TAG = "SdkReport_" + ReportCenter.class.getSimpleName(); // ReportCenter
    // final String TAG2 = "SdkReport_" + SystemInfo.class.getCanonicalName();　// sdk.report.ReportCenter
    // final String TAG4 = "SdkReport_" + SystemInfo.class.getName();　// sdk.report.ReportCenter

    final int SDK_REPORT_HEART_BEAT_MIN_INTERVAL = 1000;
    final int SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL = (30 * 1000); // 30 seconds report heartbeat
    final int SDK_REPORT_HEART_BEAT_MAX_INTERVAL = (10 * 60 * 1000);
    //----------------------------------------------------------------------
    public static final int SDK_REPORT_PLAY_START_PLAY = 100;
    public static final int SDK_REPORT_PLAY_VIDEO_FIRST_FRAME = 120;
    public static final int SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY = 130;
    public static final int SDK_REPORT_PLAY_HEART_BEAT = 140;
    public static final int SDK_REPORT_PLAY_PAUSE_BEGIN = 150;
    public static final int SDK_REPORT_PLAY_PAUSE_END = 160;
    public static final int SDK_REPORT_PLAY_FAIL = 170;
    public static final int SDK_REPORT_PLAY_STOP_PLAY = 188;
    //------------------------------------------------------
    public static final int SDK_EVENT_REPORT_START_PUBLISH = 200;
    public static final int SDK_EVENT_REPORT_PUBLISH_HEART_BEAT = 210;
    public static final int SDK_EVENT_REPORT_STOP_PUBLISH = 288;

    //-------------------------------------------------------------------------
    public static final int REPORTER_TYPE_PLAY = 1;
    public static final int REPORTER_TYPE_PUBLISH = 2;
    public static final int REPORTER_TYPE_OTHER = 3;
    //==============================================================================================
    private Context activityCtx;
    private TagHeader playHeader = null;
    //---------------------------------------------
    private TagStartPlay startPlay = null;
    private TagVideoFirstFrame vidFirstFrame = null;
    private TagVideoFirstDisplay vidFirstDisplay = null;
    private TagPlayHeartBeat heartBeat = null;
    private TagPauseBegin pauseBegin = null;
    private TagPauseEnd pauseEnd = null;
    private TagPlayFail playFail = null;
    private TagStopPlay stopPlay = null;
    private SystemInfo sysInfo = null;
    private SocketManager socketHandle = null;
    private playHeartBeatThread hbThread = null;
    private ParamMediaInfo mediaPara = null;
    //---------------------------------------------
    private long startPlayMs = 0;
    private String strToken = null;
    private String strStreamId = null;
    private boolean playerRun = false;
    private int heartBeatInterval = SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL;

    // =============================================================================================
    public ReportCenter(Context ctx, int reporter) {
        activityCtx = ctx;
        sysInfo = new SystemInfo(ctx, reporter);
        startPlay = new TagStartPlay(sysInfo);
        vidFirstFrame = new TagVideoFirstFrame(this);
        vidFirstDisplay = new TagVideoFirstDisplay();
        heartBeat = new TagPlayHeartBeat(this);
        pauseBegin = new TagPauseBegin();
        pauseEnd = new TagPauseEnd(sysInfo);
        playFail = new TagPlayFail(sysInfo);
        stopPlay = new TagStopPlay();
        socketHandle = new SocketManager();
        hbThread = new playHeartBeatThread();
        mediaPara = new ParamMediaInfo();
    }

    public ParamMediaInfo getmediaPara() {
        return mediaPara;
    }

    public SystemInfo getSysInfo() {
        return sysInfo;
    }

    // =============================================================================================
    public class playHeartBeatThread extends Thread {
        public void run() {
            int count = 0;
            Log.w(TAG, "thread-play-heartbeat-run");
            while (true) {
                if (!playerRun) {
                    Log.w(TAG, "thread-play-heartbeat-ext");
                    return;
                }

                long startMs = System.currentTimeMillis();
                //Log.w(TAG, "handle-thread-play-heartbeat-bef");
                int runTimes = (count++ % (heartBeatInterval / 1000));
                if (0 == runTimes) {
                    reportData(SDK_REPORT_PLAY_HEART_BEAT, null);
                }

                sysInfo.getSysNet().appNetBindWidth();

                if (!playerRun) {
                    Log.w(TAG, "thread-play-heartbeat-ext");
                    return;
                }

                long costMs = System.currentTimeMillis() - startMs;
                if (costMs >= 1000) {
                    continue;
                }

                long sleepMs = 1000 - costMs;

                try {
                    Thread.sleep(sleepMs);
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

    public void reportData(int type, Object objSrc) {
        final int inType = type;
        final Object objIn = objSrc;
        new Thread() {
            public void run() {
                Log.w(TAG, "report-thread:" + inType);
                playHeader = new TagHeader(sysInfo, strStreamId, strToken);
                JSONObject jsnHeader = playHeader.toJson();
                JSONObject jsnTag = new JSONObject();
                JSONObject jsnSender = new JSONObject();
                ReportData dat = null;

                switch (inType) {
                    // ========player tag<<=========
                    case SDK_REPORT_PLAY_START_PLAY:
                        dat = onReportStartPlay((ParamPlay) objIn, "startplay");
                        break;

                    case SDK_REPORT_PLAY_VIDEO_FIRST_FRAME:
                        dat = onReportVideoFirstFrame((ParamMediaInfo) objIn);
                        break;
                    case SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY:
                        dat = onReportVideoFirstDisplay();
                        break;
                    case SDK_REPORT_PLAY_HEART_BEAT:
                        dat = onReportPlayHeartBeat("playheartbeat");
                        break;
                    case SDK_REPORT_PLAY_PAUSE_BEGIN:
                        dat = onReportPlayPauseBegin();
                        break;
                    case SDK_REPORT_PLAY_PAUSE_END:
                        dat = onReportPlayPauseEnd();
                        break;
                    case SDK_REPORT_PLAY_FAIL:
                        dat = onReportPlayFail((ParamErr) objIn);
                        break;

                    case SDK_REPORT_PLAY_STOP_PLAY:
                        dat = onReportStopPlay("stopplay");
                        break;
                    // ========player tag>>=========

                    // ========publisher tag<<=========
                    case SDK_EVENT_REPORT_START_PUBLISH:
                        dat = onReportStartPlay((ParamPlay) objIn, "publishstart");
                        break;
                    case SDK_EVENT_REPORT_PUBLISH_HEART_BEAT:
                        dat = onReportPlayHeartBeat("publishheartbeat");
                        break;
                    case SDK_EVENT_REPORT_STOP_PUBLISH:
                        dat = onReportStopPlay("publishstop");
                        break;
                    // ========publisher tag<<=========

                    default:
                        return;
                }

                if (null == dat) {
                    Log.w(TAG, "report-thread-get null dat, type:" + inType);
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
                Log.w(TAG, "=============" + inJson);

                if (needUdpSocket(inType)) {
                    //Log.w(TAG, "udp-send-start");
                    //socketHandle.udpSend(inJson);
                    // Log.w(TAG, "udp-send-end");
                } else {
                    //Log.w(TAG, "tcp-send-start");
                    //socketHandle.tcpSend(inJson);
                    //Log.w(TAG, "tcp-send-end");
                }
            }
        }.start();
    }

    public void setMediaInfo(ParamMediaInfo paraMedia) {
        mediaPara = paraMedia;
    }
    // =============================================================================================
    public ReportData onReportStartPlay(ParamPlay paraPlay, String tag) {
        if (playerRun) {
            return null;
        }

        if ((paraPlay.getiHeartBeatIvt() < SDK_REPORT_HEART_BEAT_MIN_INTERVAL)
                || (paraPlay.getiHeartBeatIvt() > SDK_REPORT_HEART_BEAT_MAX_INTERVAL)) {
            paraPlay.setiHeartBeatIvt(SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL);
        }

        strToken = paraPlay.getStrToken();
        strStreamId = paraPlay.getStrStreamId();
        startPlayMs = playHeader.getStartPlayTm();
        heartBeatInterval = paraPlay.getiHeartBeatIvt();

        sysInfo.setStrCdnIp(paraPlay.getStrCdnName());
        sysInfo.setStrPlayUrl(paraPlay.getStrUrl());
        sysInfo.setStrInternetIp(paraPlay.getStrOutIp());

        ReportData data = new ReportData(startPlay.toJson(), tag);
        playerRun = true;
        hbThread.start();

        return data;
    }

    public ReportData onReportVideoFirstFrame(ParamMediaInfo paraMedia) {
        if (!playerRun) {
            return null;
        }
        setMediaInfo(paraMedia);
        return (new ReportData(vidFirstFrame.toJson(), "videofirstframe"));
    }

    public ReportData onReportVideoFirstDisplay() {
        if (!playerRun) {
            return null;
        }
        vidFirstDisplay.setCostMs(startPlayMs);
        return (new ReportData(vidFirstDisplay.toJson(), "videofirstdisplay"));
    }

    public ReportData onReportPlayHeartBeat(String tag) {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(heartBeat.toJson(), tag));
    }

    public ReportData onReportPlayPauseBegin() {
        if (!playerRun) {
            return null;
        }
        //return (new ReportData(pauseBegin.toJson(), "pausebegin"));
        return (new ReportData(pauseEnd.toJson(), "pausebegin"));
    }

    public ReportData onReportPlayPauseEnd() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(pauseEnd.toJson(), "pauseend"));
    }

    public ReportData onReportPlayFail(ParamErr errIn) {
        if (!playerRun) {
            return null;
        }

        playFail.setFailNo(errIn.getiErrNo());
        playFail.setFailMsg(errIn.getStrErr());
        return (new ReportData(playFail.toJson(), "playfail"));
    }

    public ReportData onReportStopPlay(String tag) {
        if (!playerRun) {
            return null;
        }
        stopPlay.setCostMs(startPlayMs);
        playerRun = false;
        return (new ReportData(stopPlay.toJson(), tag));
    }


    // =============================================================================================
    public ReportData onReportStartPublish() {
        if (!playerRun) {
            return null;
        }
        stopPlay.setCostMs(startPlayMs);
        playerRun = false;
        return (new ReportData(stopPlay.toJson(), "publishstart"));
    }

    public ReportData onReportPublishHeartBeat() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(heartBeat.toJson(), "publishheartbeat"));
    }

    public ReportData onReportStopPublish() {
        if (!playerRun) {
            return null;
        }
        stopPlay.setCostMs(startPlayMs);
        playerRun = false;
        return (new ReportData(stopPlay.toJson(), "publishstop"));
    }

    // =============================================================================================
    private boolean needUdpSocket(int type) {
        switch (type) {
            // ========player tag<<=========
            case SDK_REPORT_PLAY_START_PLAY:
            case SDK_REPORT_PLAY_VIDEO_FIRST_FRAME:
            case SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY:
            case SDK_REPORT_PLAY_FAIL:
            case SDK_REPORT_PLAY_STOP_PLAY:
                // ========player tag>>=========

                // ========publisher tag<<=========
            case SDK_EVENT_REPORT_START_PUBLISH:
            case SDK_EVENT_REPORT_STOP_PUBLISH:
                // ========publisher tag<<=========

                return false;
            // ========player tag<<=========
            case SDK_REPORT_PLAY_HEART_BEAT:
            case SDK_REPORT_PLAY_PAUSE_BEGIN:
            case SDK_REPORT_PLAY_PAUSE_END:
                // ========player tag>>=========
                // ========publisher tag<<=========
            case SDK_EVENT_REPORT_PUBLISH_HEART_BEAT:
                // ========publisher tag<<=========
            default:
                break;
        }
        return true;
    }

}
