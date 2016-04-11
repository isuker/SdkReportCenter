package sdk.report;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by suker on 16-4-6.
 */
public class SystemInfo {
    String TAG = "SdkReport_" + SystemInfo.class.getSimpleName();
    static final int SYS_CPU = 1;
    static final int SYS_MEM = 2;
    static final String MEM_VM_RSS = "VmRSS";
    //==============================================================================================
    Context activityCtx = null;
    SysNetwork sysNet = null;
    //==============================================================================================
    String strInternetIp = null;
    String strDnsIp = null;
    String strCdnIp = null;
    String strPlayUrl = null;
    long mLogMaxMemSize = 0;

    public SysNetwork getSysNet() {
        return sysNet;
    }

    public String getStrInternetIp() {
        return strInternetIp;
    }

    public void setStrInternetIp(String strInternetIp) {
        this.strInternetIp = strInternetIp;
    }

    public String getStrDnsIp() {
        return strDnsIp;
    }

    public void setStrDnsIp(String strDnsIp) {
        this.strDnsIp = strDnsIp;
    }

    public String getStrCdnIp() {
        return strCdnIp;
    }

    public void setStrCdnIp(String strCdnIp) {
        this.strCdnIp = strCdnIp;
    }

    public String getStrPlayUrl() {
        return strPlayUrl;
    }

    public void setStrPlayUrl(String strPlayUrl) {
        this.strPlayUrl = strPlayUrl;
    }
    //==============================================================================================

    class CpuTopPara {
        int cpuPencent;
        int memRss;
        String progressName;
    }

    public SystemInfo(Context ctx) {
        activityCtx = ctx;
        sysNet = new SysNetwork(ctx);
    }

    public String getSysImei() {
        if (null == activityCtx) {
            return "empty-imei";
        }
        TelephonyManager tm = (TelephonyManager) activityCtx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public float getSdcardRate() {
        long[] sdCardInfo = new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long availBlocks = sf.getAvailableBlocks();

            sdCardInfo[0] = bSize * bCount;
            sdCardInfo[1] = bSize * availBlocks;
            float pencent = 0;
            if (bCount > 0) {
                pencent = (float) (bCount - availBlocks) / (float) bCount * 100;
            }
            pencent = ((int) (pencent * 100)) / 100;
            return pencent;
        }
        return 0;
    }


    private JSONObject putCpuMemJson(int type, CpuMemUsage usage, CpuTopPara maxDat) {
        JSONObject cpuValue = new JSONObject();
        int value = maxDat.cpuPencent;
        if (SYS_MEM == type) {
            int maxMemKb = (int) (mLogMaxMemSize / 1024);
            if (0 == maxMemKb) {
                value = 0;
            } else {
                value = (int) (100 * (float) maxDat.memRss / (float) maxMemKb);
                Log.w(TAG, "maxmem:" + maxDat.memRss + ", total:" + maxMemKb + ", bytes:" + mLogMaxMemSize + ", value:" + value);
            }
        }

        try {
            cpuValue.put("selfpencent", usage.appUsage);
            cpuValue.put("selfvalue", usage.appValue);
            cpuValue.put("totalpencent", usage.totalUsage);
            cpuValue.put("maxpencent", value);
            cpuValue.put("maxname", maxDat.progressName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cpuValue;
    }


    public long getSysTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            if (str2 != null) {
                arrayOfString = str2.split("\\s+");
                initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
            }
            localBufferedReader.close();
            return initial_memory;
        } catch (IOException e) {
            return -1;
        }
    }

    public CpuMemUsage getMemTotalUsed() {
        if (activityCtx == null) {
            return null;
        }
        CpuMemUsage memUsage = new CpuMemUsage();
        final ActivityManager am = (ActivityManager) activityCtx.getSystemService(activityCtx.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(info);

        long curSysMem = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.w(TAG, "android sdk version:" + Build.VERSION.SDK_INT + ", error");
            curSysMem = getSysTotalMemory();
        } else {
            curSysMem = info.totalMem;
        }

        mLogMaxMemSize = curSysMem;
        long useMem = curSysMem - info.availMem;
        if (0 == curSysMem) {
            memUsage.totalUsage = 0;
        } else {
            memUsage.totalUsage = 100 * (float) useMem / (float) curSysMem;
        }

        Log.w(TAG, "totalmeme:" + curSysMem + ", avail:" + info.availMem + ", useMem:" + useMem);
        String appUsedMemStr = getPidMemory();
        memUsage.appValue = myAatoi(appUsedMemStr) * 1024;

        if (0 == curSysMem) {
            memUsage.appUsage = 0;
        } else {
            memUsage.appUsage = (float) memUsage.appValue / (float) curSysMem * 100;
        }

        memUsage.appValue = ((int) (memUsage.appValue * 100)) / 100;
        memUsage.totalUsage = ((int) (memUsage.totalUsage * 100)) / 100;
        memUsage.appUsage = ((int) (memUsage.appUsage * 100)) / 100;
        memUsage.logDat("mem");
        return memUsage;
    }


    String getPidMemory() {
        int pid = android.os.Process.myPid();
        String str1 = "/proc/" + pid + "/status";
        String str2 = "";
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((str2 = localBufferedReader.readLine()) != null) {
                int posHead = str2.indexOf(MEM_VM_RSS);
                if (posHead >= 0) {
                    int posEnd = str2.indexOf("kB");
                    if (-1 == posEnd) {
                        posEnd = str2.indexOf("kb");
                        if (-1 == posEnd) {
                            posEnd = str2.indexOf("KB");
                            if (-1 == posEnd) {
                                posEnd = str2.indexOf("Kb");
                            }
                        }
                    }

                    //out.println(MEM_VM_RSS + ", len:" + MEM_VM_RSS.length() + ", " + str2 + ", pos start:" + posHead + ", end:" + posEnd);
                    if (-1 == posEnd) {
                        return "0";
                    }
                    String strNumAll = str2.substring(posHead + MEM_VM_RSS.length(), posEnd);
                    int countId = 0;
                    while (countId < strNumAll.length()) {
                        if (strNumAll.charAt(countId) >= '0' && strNumAll.charAt(countId) <= '9') {
                            break;
                        }
                        countId++;
                    }
                    String strOnlyNum = strNumAll.substring(countId);
                    return strOnlyNum;
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return str2;
    }


    //==============================================================================================
    public class CpuTimeSt {
        public float idleTm;
        public float totalTm;
        public float allTm;
    }

    public class CpuMemUsage {
        public float appValue;
        public float totalUsage;
        public float appUsage;

        public void logDat(String tag) {
            Log.w(TAG, tag + ", app-value:" + appValue + ", total-usage:" + totalUsage + ", app-usage:" + appUsage);
        }
    }

    public CpuMemUsage getProcessCpuRate() {
        CpuTimeSt totalCpuTm1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();

        try {
            Thread.sleep(360);
        } catch (Exception e) {
        }

        CpuMemUsage cpuUsage = new CpuMemUsage();
        CpuTimeSt totalCpuTm2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();

        float cpuUsed = (totalCpuTm2.totalTm - totalCpuTm1.totalTm);
        float cpuTotal = (totalCpuTm2.allTm - totalCpuTm1.allTm);
        cpuUsage.appValue = (processCpuTime2 - processCpuTime1);

        if (0 == cpuTotal) {
            cpuUsage.totalUsage = 0;
            cpuUsage.appUsage = 0;
        } else {
            cpuUsage.totalUsage = (100 * cpuUsed / cpuTotal);
            cpuUsage.appUsage = (100 * cpuUsage.appValue / cpuTotal);
        }

        cpuUsage.appValue = ((int) (cpuUsage.appValue * 100)) / 100;
        cpuUsage.totalUsage = ((int) (cpuUsage.totalUsage * 100)) / 100;
        cpuUsage.appUsage = ((int) (cpuUsage.appUsage * 100)) / 100;
        if ((cpuUsage.appUsage < 1) || (cpuUsage.totalUsage < 1)) {
            Log.w(TAG, "***************************error-app-usage:" + cpuUsage.appUsage
                    + ", total-usage:" + cpuUsage.totalUsage + ", app-value:" + cpuUsage.appValue + ", total-cpu:" + cpuTotal + ", cpu-used:" + cpuUsed);
        }

        cpuUsage.logDat("cpu");
        return cpuUsage;
    }

    public CpuTimeSt getTotalCpuTime() {
        String[] cpuInfos = null;
        CpuTimeSt ttCpu = new CpuTimeSt();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ttCpu.idleTm = Long.parseLong(cpuInfos[5]);
        ttCpu.totalTm = Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        ttCpu.allTm = ttCpu.idleTm + ttCpu.totalTm;
        return ttCpu;
    }

    public long getAppCpuTime() {
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    //===================================================================================================

    public int myAatoi(String s) {
        int r = 0;

        boolean isMinus = false;

        if (s.length() == 0) {
            return 0;
        }

        int idx = 0;
        if (s.charAt(0) == '-') {
            idx++;
            isMinus = true;
        } else if (s.charAt(0) == '+') {
            idx++;
        }

        if (s.length() <= idx) {
            return 0;
        }

        for (int i = idx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                break;
            }
            int t = c - '0';
            r = r * 10 + t;
        }

        if (isMinus) {
            r = -r;
        }

        return r;
    }

    //==============================================================================================
    private CpuTopPara getTopCpuMem(String type) {
        String[] cpuInfos = null;
        final Process m_process;
        String title = "[" + type + "]";
        //Log.w(TAG, title + "run top bgn================================");
        CpuTopPara cpuPara = new CpuTopPara();
        try {
            //Log.w(TAG, title + "run top cmd bgn");
            m_process = Runtime.getRuntime().exec("/system/bin/top -n 1 -d 0.2 -m 1 -s " + type);  // top -m 10    /// /system/bin/top -n 1;
            //Log.w(TAG, title + "run top cmd end");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(m_process.getInputStream()), 8192);
            //Log.w(TAG, title + "run create buffer end");
            String str2;
            boolean startTag = false;
            String[] cpuSlipt = new String[20];
            while ((str2 = bufferedReader.readLine()) != null) {
                if (startTag) {
                    int count = 0;
                    //Log.w(TAG, title + "read top:" + str2);
                    cpuInfos = str2.split(" ");
                    int i = 0;
                    for (i = 0; i < cpuInfos.length; i++) {
                        if (cpuInfos[i].length() <= 0) {
                            continue;
                        }
                        if (' ' == cpuInfos[i].charAt(0)) {
                            continue;
                        }
                        //Log.w(TAG, title + ", id:" + i + ", cpuInfos:" + cpuInfos[i]);
                        if (count < cpuSlipt.length) {
                            cpuSlipt[count] = cpuInfos[i];
                        }
                        count++;
                    }
                    //Log.w(TAG, title + ", id:" + i + ", total:" + count);
                    if (count > 7) {
                        String strCpuPencent = cpuSlipt[2];
                        cpuPara.cpuPencent = myAatoi(strCpuPencent);

                        String strMemRssKb = cpuSlipt[6];
                        cpuPara.memRss = myAatoi(strMemRssKb);

                        cpuPara.progressName = cpuSlipt[count - 1];
                        Log.w(TAG, title + ", progress cpu pencent:" + strCpuPencent + ", mem rss kb:" + strMemRssKb + ", progress name:" + cpuPara.progressName + ", cpu:" + cpuPara.cpuPencent + ", rss:" + cpuPara.memRss);
                    }
                }
                int pos = str2.indexOf("Name");
                if (pos > 0) {
                    startTag = true;
                }
            }
            //Log.w(TAG, title + "run read buffer");
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.w(TAG, title + "run top end================================");
        return cpuPara;
    }


    public JSONObject getCpuJson() {
        CpuMemUsage cpuCurRate = getProcessCpuRate();
        CpuTopPara topCpu = getTopCpuMem("cpu");

        if ((null == cpuCurRate) || (null == topCpu)) {
            return null;
        }
        return putCpuMemJson(SYS_CPU, cpuCurRate, topCpu);
    }

    public JSONObject getMemJson() {
        CpuMemUsage memCurUsage = getMemTotalUsed();
        CpuTopPara topMem = getTopCpuMem("rss");
        if ((null == memCurUsage) || (null == topMem)) {
            return null;
        }
        return putCpuMemJson(SYS_MEM, memCurUsage, topMem);
    }


    //==============================================================================================
    public String getAppVerName() {
        try {
            if (null == activityCtx) {
                return "appvername";
            }
            String pkName = activityCtx.getPackageName();
            String versionName = activityCtx.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            return versionName;
        } catch (Exception e) {
        }
        return "appvername";
    }

    public String getAppVerCode() {
        try {
            if (null == activityCtx) {
                return "appvercode";
            }
            String pkName = activityCtx.getPackageName();
            int versionCode = activityCtx.getPackageManager()
                    .getPackageInfo(pkName, 0).versionCode;
            return String.valueOf(versionCode);
        } catch (Exception e) {
        }
        return "appvercode";
    }
}
