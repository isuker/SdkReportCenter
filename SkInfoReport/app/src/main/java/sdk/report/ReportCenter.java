package sdk.report;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suker on 16-4-7.
 */
public class ReportCenter {
    private final String TAG = "SdkReport_" + ReportCenter.class.getSimpleName(); // ReportCenter

    private final int SDK_REPORT_HEART_BEAT_MIN_INTERVAL = 1000;
    private final int SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL = (30 * 1000); // 30 seconds report heartbeat
    private final int SDK_REPORT_HEART_BEAT_MAX_INTERVAL = (10 * 60 * 1000);

    //----------------------------------------------------------------------
    public static final int SDK_REPORT_MSG_START = 100;
    public static final int SDK_REPORT_MSG_HEART_BEAT = 140;
    public static final int SDK_REPORT_MSG_STOP = 188;
    //------------------------------------------
    public static final int SDK_REPORT_MSG_PLAY_VIDEO_FIRST_FRAME = 220;
    public static final int SDK_REPORT_MSG_PLAY_VIDEO_FIRST_DISPLAY = 230;
    public static final int SDK_REPORT_MSG_PLAY_PAUSE_BEGIN = 250;
    public static final int SDK_REPORT_MSG_PLAY_PAUSE_END = 260;
    public static final int SDK_REPORT_MSG_PLAY_FAIL = 270;
    //-------------------------------------------------------------------------
    public static final int SDK_REPORTER_TYPE_PLAY = 1;
    public static final int SDK_REPORTER_TYPE_PUBLISH = 2;
    public static final int SDK_REPORTER_TYPE_OTHER = 3;
    //==============================================================================================
    private Context activityCtx;
    private TagHeader playHeader = null;
    //---------------------------------------------
    private TagStartPlay startPlay = null;
    private TagVideoFirstFrame vidFirstFrame = null;
    private TagVideoFirstDisplay vidFirstDisplay = null;
    private TagPlayHeartBeat heartBeat = null;
    private TagPauseEnd pauseEnd = null;
    private TagPlayFail playFail = null;
    private TagStopPlay stopPlay = null;
    //-------------------------------
    private SystemInfo sysInfo = null;
    private SocketManager socketHandle = null;
    private playHeartBeatThread hbThread = null;
    private ParamMediaInfo mediaPara = null;
    private ParamPlay playPara = null;
    //---------------------------------------------
    private long usrStartTime = 0; // ms
    private boolean playerRun = false;
    private boolean playerFrameCome = false;
    private ReportCenter reportSelfCtx = null;
    private int heartBeatInterval = SDK_REPORT_HEART_BEAT_DEFAULT_INTERVAL;
    private int reporterType = ReportCenter.SDK_REPORTER_TYPE_PLAY; // 1: client, 2:server
    private Handler threadHandler = null;

    //---------------------------------------------
    private int catonCurCount = 0;   // current play caton count
    private long  catonCurStart = 0;  // current play caton start caton second
    private int catonCurSeconds = 0;  // current play caton how many seconds
    private int catonCurTotalSec = 0; // current  url play caton total seconds

    private static int catonTotalCount = 0;   // usr not exit app, total play caton count
    private static int catonTotalSeconds = 0; // usr not exit app, total play caton seconds

    //---------------------------------------------

    // =============================================================================================
    public ReportCenter(Context ctx, int reporter) {
        Log.w(TAG, "report-center-run");
        activityCtx = ctx;
        reportSelfCtx = this;
        reporterType = reporter;
        sysInfo = new SystemInfo(ctx, this);
        startPlay = new TagStartPlay(this);
        vidFirstFrame = new TagVideoFirstFrame(this);
        vidFirstDisplay = new TagVideoFirstDisplay();
        heartBeat = new TagPlayHeartBeat(this);
        pauseEnd = new TagPauseEnd(this);
        playFail = new TagPlayFail(this);
        stopPlay = new TagStopPlay();
        socketHandle = new SocketManager(this);
        hbThread = new playHeartBeatThread();
        playPara = new ParamPlay();

        WorkerThread wt = new WorkerThread();
        wt.start();
        Log.w(TAG, "report-center-ext");
    }

    public ParamMediaInfo getmediaPara() {
        return mediaPara;
    }

    public SystemInfo getSysInfo() {
        return sysInfo;
    }

    public ParamPlay getParamPlay() {
        return playPara;
    }

    public int getReporterType() {
        return reporterType;
    }

    public boolean isPlayerFrameCome() {
        return playerFrameCome;
    }

    public void setPlayerFrameCome(boolean playerFrameCome) {
        this.playerFrameCome = playerFrameCome;
        vidFirstDisplay.setCostMs(usrStartTime);
    }

    public int getCatonCurCount() {
        return catonCurCount;
    }

    public void setCatonCurCount(int catonCurCount) {
        this.catonCurCount = catonCurCount;
    }

    public long getCatonCurStart() {
        return catonCurStart;
    }

    public void setCatonCurStart(long catonCurStart) {
        this.catonCurStart = catonCurStart;
    }

    public int getCatonCurSeconds() {
        return catonCurSeconds;
    }

    public void setCatonCurSeconds(int catonCurSeconds) {
        this.catonCurSeconds = catonCurSeconds;
    }

    public int getCatonCurTotalSec() {
        return catonCurTotalSec;
    }

    public void setCatonCurTotalSec(int catonCurTotalSec) {
        this.catonCurTotalSec = catonCurTotalSec;
    }

    public int getCatonTotalCount() {
        return catonTotalCount;
    }

    public void setCatonTotalCount(int catonTotalCount) {
        this.catonTotalCount = catonTotalCount;
    }

    public int getCatonTotalSeconds() {
        return catonTotalSeconds;
    }

    public void setCatonTotalSeconds(int catonTotalSeconds) {
        this.catonTotalSeconds = catonTotalSeconds;
    }


    public void initCurPlayDat() {
        playerFrameCome = false;
        catonCurCount = 0;
        catonCurSeconds = 0;
        catonCurTotalSec = 0;
    }

    // =============================================================================================
    public void postMsg(int type, long curMs, Object objSrc) {
        if (null == threadHandler) {
            Log.i(TAG, "thread-handler-null");
            return;
        }

        Message msg = threadHandler.obtainMessage();
        msg.what = type;
        msg.arg1 = (int) curMs;
        msg.obj = objSrc;
        threadHandler.sendMessage(msg);
    }

    // =============================================================================================
    private class playHeartBeatThread extends Thread {
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
                    postMsg(SDK_REPORT_MSG_HEART_BEAT, 0, null);
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
                msSleep(sleepMs);
            }
        }
    }

    // =============================================================================================
    class WorkerThread extends Thread {
        public void run() {
            Log.w(TAG, "worker-thread-handler-run");
            Looper.prepare();
            Log.w(TAG, "worker-thread-handler-prepare-end");
            threadHandler = new Handler() {
                public void handleMessage(Message msg) {
                    Log.i(TAG, "receive new message:" + msg.what + ", arg1:" + msg.arg1);
                    onReportMsg(msg);
                }
            };
            Log.w(TAG, "worker-thread-handler-loop-bef");
            Looper.loop();
            Log.w(TAG, "worker-thread-handler-exit");
        }
    }

    // =============================================================================================
    private class ReportData {
        JSONObject objJson;
        String strTitle;

        public ReportData(JSONObject jsnObj, String title) {
            objJson = jsnObj;
            strTitle = title;
        }
    }
    // =============================================================================================

    private void onReportMsg(Message msg) {
        int inType = msg.what;
        Object objIn = msg.obj;

        if (inType == SDK_REPORT_MSG_START) {
            ParamUser userIn = (ParamUser) objIn;
            playPara.setStrUrl(userIn.getStrInUrl());
            playPara.setStrStreamUrl(userIn.getStrStreamUrl());
            playPara.setStrCdnIp(userIn.getStrConnectIp());
            boolean bRet = socketHandle.tcpAuth("aa");
            Log.w(TAG, "report-tcpAuth:" + bRet);
            if (!bRet) {
                return;
            }
        }

        Log.w(TAG, "report-thread-inmsg:" + msg.what + ", new:" + inType);
        playHeader = new TagHeader(reportSelfCtx, "bbbbb", playPara.getStrToken());
        JSONObject jsnHeader = playHeader.toJson();
        JSONObject jsnSender = new JSONObject();
        ReportData dat = null;
        String reportTag = null;

        switch (inType) {
            // ========player tag<<=========
            case SDK_REPORT_MSG_START: {
                reportTag = "startplaying";
                if (ReportCenter.SDK_REPORTER_TYPE_PUBLISH == reporterType) {
                    reportTag = "startpush";
                }
                dat = onReportStart((ParamUser) objIn, reportTag);
            }
            break;

            case SDK_REPORT_MSG_PLAY_VIDEO_FIRST_FRAME:
                dat = onReportVideoFirstFrame((ParamMediaInfo) objIn);
                break;
            case SDK_REPORT_MSG_PLAY_VIDEO_FIRST_DISPLAY:
                dat = onReportVideoFirstDisplay();
                break;
            case SDK_REPORT_MSG_HEART_BEAT: {
                reportTag = "playHeartbeat";
                if (ReportCenter.SDK_REPORTER_TYPE_PUBLISH == reporterType) {
                    reportTag = "pushHeartbeat";
                }
                dat = onReportHeartBeat(reportTag);
            }
            break;
            case SDK_REPORT_MSG_PLAY_PAUSE_BEGIN:
                dat = onReportPlayPauseBegin();
                break;
            case SDK_REPORT_MSG_PLAY_PAUSE_END:
                dat = onReportPlayPauseEnd();
                break;
            case SDK_REPORT_MSG_PLAY_FAIL:
                dat = onReportPlayFail((ParamErr) objIn);
                break;

            case SDK_REPORT_MSG_STOP: {
                reportTag = "stopplaying";
                if (ReportCenter.SDK_REPORTER_TYPE_PUBLISH == reporterType) {
                    reportTag = "stoppush";
                }
                dat = onReportStop(reportTag);
            }
            break;
            default:
                return;
        }

        if (null == dat) {
            Log.w(TAG, "report-thread-get null dat, type:" + inType);
            return;
        }

        try {
            if (needUdpSocket(inType)) {
                jsnSender.put("method", dat.strTitle);
            }

            jsnSender.put("head", jsnHeader);
            jsnSender.put("body", dat.objJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String inJson = jsnSender.toString();
        Log.w(TAG, "=============" + inJson);

        if (needUdpSocket(inType)) {
            //Log.w(TAG, "udp-send-start");
            socketHandle.udpSend(inJson);
            // Log.w(TAG, "udp-send-end");
        } else {
            //Log.w(TAG, "tcp-send-start");
            socketHandle.tcpSend(inJson, dat.strTitle);
            //Log.w(TAG, "tcp-send-end");
        }
    }

    public void setUserStartTime(long startTm) {
        this.usrStartTime = startTm;
    }

    public void setConnectRmtIp(String strIp) {
        playPara.setStrCdnIp(strIp);
    }

    private void setMediaInfo(ParamMediaInfo paraMedia) {
        mediaPara = paraMedia;
    }

    // =============================================================================================
    private ReportData onReportStart(ParamUser paraPlay, String tag) {
        if (playerRun) {
            return null;
        }

        ReportData data = new ReportData(startPlay.toJson(), tag);
        playerRun = true;
        hbThread.start();

        return data;
    }

    private ReportData onReportVideoFirstFrame(ParamMediaInfo paraMedia) {
        if (!playerRun) {
            return null;
        }
        setMediaInfo(paraMedia);
        return (new ReportData(vidFirstFrame.toJson(), "firstframearrives"));
    }

    private ReportData onReportVideoFirstDisplay() {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(vidFirstDisplay.toJson(), "firstframepicture"));
    }

    private ReportData onReportHeartBeat(String tag) {
        if (!playerRun) {
            return null;
        }
        return (new ReportData(heartBeat.toJson(), tag));
    }

    private ReportData onReportPlayPauseBegin() {
        if (!playerRun) {
            return null;
        }

        catonCurCount++;
        catonCurStart = System.currentTimeMillis();
        catonCurSeconds = 0;
        catonTotalCount++;
        //return (new ReportData(pauseBegin.toJson(), "pausebegin"));
        return (new ReportData(pauseEnd.toJson(), "playCatonStart"));
    }

    private ReportData onReportPlayPauseEnd() {
        if (!playerRun) {
            return null;
        }

        catonCurSeconds = (int) (System.currentTimeMillis() - catonCurStart)/1000;
        catonCurTotalSec += catonCurSeconds;
        catonTotalSeconds += catonCurSeconds;

        return (new ReportData(pauseEnd.toJson(), "playCatonEnd"));
    }

    private ReportData onReportPlayFail(ParamErr errIn) {
        if (!playerRun) {
            return null;
        }

        playFail.setFailNo(errIn.getiErrNo());
        playFail.setFailMsg(errIn.getStrErr());
        return (new ReportData(playFail.toJson(), "playerror"));
    }

    private ReportData onReportStop(String tag) {
        if (!playerRun) {
            return null;
        }
        stopPlay.setCostMs(usrStartTime);
        playerRun = false;
        return (new ReportData(stopPlay.toJson(), tag));
    }

    // =============================================================================================
    private boolean needUdpSocket(int type) {
        switch (type) {
            case SDK_REPORT_MSG_HEART_BEAT:
            case SDK_REPORT_MSG_PLAY_PAUSE_BEGIN:
            case SDK_REPORT_MSG_PLAY_PAUSE_END:
            default:
                return true;

            case SDK_REPORT_MSG_START:
            case SDK_REPORT_MSG_PLAY_VIDEO_FIRST_FRAME:
            case SDK_REPORT_MSG_PLAY_VIDEO_FIRST_DISPLAY:
            case SDK_REPORT_MSG_PLAY_FAIL:
            case SDK_REPORT_MSG_STOP:
                return false;
        }
    }

    private final static int SLEEP_MS_ITV = 50;

    public void msSleep(long ms) {
        int times = (int) ms / SLEEP_MS_ITV;
        if (ms < SLEEP_MS_ITV) {
            times = 1;
        }

        while (playerRun && (times-- > 0)) {
            try {
                Thread.sleep(SLEEP_MS_ITV);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

