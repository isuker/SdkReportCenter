package sdk.report;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by suker on 16-4-9.
 */
public class SysNetwork {
    private final static String TAG = "SdkReport_" + SysNetwork.class.getSimpleName();
    private final static int NET_BW_AVRY_NUM   = 3;

    private Context activityCtx = null;
    private String mLogHost = null;
    private String hostIpAddr = null;

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private long appNetSndBytes = 0;
    private long appNetRcvBytes = 0;
    private long appNetSndBw = 0;
    private long appNetRcvBw = 0;
    private int aryIdx = 0;

    private long appNetSnd3AryBw[] = new long[NET_BW_AVRY_NUM];
    private long appNetRcv3AryBw[] = new long[NET_BW_AVRY_NUM];
    private String pingMsValue = null;

    public SysNetwork(Context ctx) {
        activityCtx = ctx;
    }


    public String getmLogHost() {
        return mLogHost;
    }

    public void setmLogHost(String mLogHost) {
        this.mLogHost = mLogHost;
    }

    public String getHostIpAddr() {
        return hostIpAddr;
    }

    public void setHostIpAddr(String hostIpAddr) {
        this.hostIpAddr = hostIpAddr;
    }

    public long getLastTotalRxBytes() {
        return lastTotalRxBytes;
    }

    public void setLastTotalRxBytes(long lastTotalRxBytes) {
        this.lastTotalRxBytes = lastTotalRxBytes;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public long getAppNetSndBytes() {
        return appNetSndBytes;
    }

    public void setAppNetSndBytes(long appNetSndBytes) {
        this.appNetSndBytes = appNetSndBytes;
    }

    public long getAppNetRcvBytes() {
        return appNetRcvBytes;
    }

    public void setAppNetRcvBytes(long appNetRcvBytes) {
        this.appNetRcvBytes = appNetRcvBytes;
    }

    public long getAppNetSndBw() {
        return appNetSndBw;
    }

    public void setAppNetSndBw(long appNetSndBw) {
        this.appNetSndBw = appNetSndBw;
    }

    public long getAppNetRcvBw() {
        return appNetRcvBw;
    }

    public void setAppNetRcvBw(long appNetRcvBw) {
        this.appNetRcvBw = appNetRcvBw;
    }

    public String getPingMs(String urlDomain) {
        return pingValue(urlDomain);
    }


    private boolean networkConnected() {
        if (null == activityCtx) {
            return false;
        }

        ConnectivityManager conMan = (ConnectivityManager) activityCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == conMan) {
            return false;
        }

        NetworkInfo info = conMan.getActiveNetworkInfo();
        if (null == info) {
            Log.w(TAG, "net not connected");
            return false;
        }

//mobile 3G Data Network
        NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        Log.w(TAG, "net connect state:" + info.getState() + "mobile-state:" + mobile.toString());
        if (NetworkInfo.State.CONNECTED == mobile) {
            return true;
        }

//wifi
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        Log.w(TAG, "wifi-state:" + wifi.toString());
        if (NetworkInfo.State.CONNECTED == wifi) {
            return true;
        }
        return false;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public String getLocalIpFromGprs() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.w(TAG, "WifiPreference IpAddress:" + ex.toString());
        }
        return null;
    }


    public String getHostIp() {
        if (!networkConnected()) {
            return "0";
        }
        new Thread() {
            public void run() {
                InetAddress address = null;
                try {
                    address = InetAddress.getByName(mLogHost);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (null == address) {
                    Log.w(TAG, "get address fail");
                    hostIpAddr = "0";
                    return;
                }
                hostIpAddr = address.getHostAddress();
            }
        }.start();

        Log.w(TAG, "get address wait");
        long bgnMs = System.currentTimeMillis();
        while (null == hostIpAddr) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ((System.currentTimeMillis() - bgnMs) > 2000) {
                hostIpAddr = "1";
                break;
            }
        }
        Log.w(TAG, "get address end");
        return hostIpAddr;
    }


    //==============================================================================================
    //====net bitrate count=========================================================================

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(activityCtx.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
    }

    private long caluNetBw(long table[])
    {
        int id;
        long sum=0;
        for (id = 0; id < NET_BW_AVRY_NUM; id++)
        {
            sum += table[id];
        }
        return sum;
    }

    public void appNetBindWidth() {
        long uidTxBys = TrafficStats.getUidTxBytes(activityCtx.getApplicationInfo().uid);
        long uidRxBys = TrafficStats.getUidRxBytes(activityCtx.getApplicationInfo().uid);

        long appSndBw = (uidTxBys-appNetSndBytes)*8;
        long appRcvBw = (uidRxBys-appNetRcvBytes)*8;

        int curId = aryIdx % NET_BW_AVRY_NUM;
        appNetSnd3AryBw[curId]=appSndBw;
        appNetRcv3AryBw[curId]=appRcvBw;

        appNetSndBw = caluNetBw(appNetSnd3AryBw)/NET_BW_AVRY_NUM;
        appNetRcvBw = caluNetBw(appNetRcv3AryBw)/NET_BW_AVRY_NUM;

        aryIdx++;

//        Log.w(TAG, "uidTxBys-Bw(bits).......：" + appSndBw+", aver3:"+appNetSndBw+", (Bytes):"+appSndBw/8+", aver3:"+appNetSndBw/8);
//        Log.w(TAG, "uidRxBys-Bw(bits).......：" + appRcvBw+", aver3:"+appNetRcvBw+", (Bytes):"+appRcvBw/8+", aver3:"+appNetRcvBw/8);
        appNetSndBytes = uidTxBys;
        appNetRcvBytes = uidRxBys;
    }


    public String getSysDnsIp() {
        Log.w(TAG, "run-getdns-bgn");
        String strOut = "";

        try {
            final Process runPrcs = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runPrcs.getInputStream()), 8192);
            String str2;
            while ((str2 = bufferedReader.readLine()) != null) {
                String[] dnsIpAry = str2.split(" ");
                if (-1 == dnsIpAry[0].indexOf("net.dns")) {
                    continue;
                }

                for (int i = 0; i < dnsIpAry.length; i++) {
                    if ((dnsIpAry[i].indexOf("[1") != -1)
                            || (dnsIpAry[i].indexOf("[2") != -1)) {
                        strOut += dnsIpAry[i].substring(1, dnsIpAry[i].length() - 1) + ",";
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "run-getdns-end================================:" + strOut);
        return strOut;
    }


    private String pingValue(final String domain) {
        Log.w(TAG, "run-getping-bgn:"+domain);
        long bgnMs = System.currentTimeMillis();
        pingMsValue=null;

        new Thread() {
            public void run() {
                //Log.w(TAG, "run-getping-run");
                String result = null;
                try {
                    String ip = domain;
                    Process p = Runtime.getRuntime().exec("ping -c 3 -i 0.2 " + ip);
                    //Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);
                    InputStream input = p.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    StringBuffer stringBuffer = new StringBuffer();
                    String content = "";

                    while ((content = in.readLine()) != null) {
                        stringBuffer.append(content);
                        if (-1 == content.indexOf("rtt")) {
                            continue;
                        }

                        // rtt min/avg/max/mdev = 6.888/10.222/14.562/2.726 ms
                        String[] splitAry = content.split("=");
                        if (splitAry.length > 1) {
                            String[] itemAry = splitAry[1].split("/");
                            for (int j = 0; j < itemAry.length; j++) {
                               // Log.i(TAG, "+++++++++++++++++++++++ping-:" + 1 + ", j:" + j + ":" + itemAry[j]);
                            }
                            if (itemAry.length > 1) {
                                Log.i(TAG, "+++++++++++++++++++++++ping rtt:" + content + ", aver:" + itemAry[1]);
                                pingMsValue = itemAry[1];
                            }
                        }
                    }

                    int status = p.waitFor();
                    if (status == 0) {
                        result = "successful~";
                    } else {
                        result = "failed~ cannot reach the IP address";
                    }
                } catch (IOException e) {
                    result = "failed~ IOException";
                } catch (InterruptedException e) {
                    result = "failed~ InterruptedException";
                } finally {
                    Log.i(TAG, "result = " + result);
                }
            }
        }.start();
        //Log.w(TAG, "run-getping-end");
        //Log.w(TAG, "get ping-value wait");
        while (null == pingMsValue) {
            msSleep(20);
            if ((System.currentTimeMillis() - bgnMs) > 2000) {
                pingMsValue = ">2000MS";
                break;
            }
        }
        long costMs = System.currentTimeMillis()-bgnMs;
        Log.w(TAG, "run-getping-end:"+pingMsValue+", cost-ms:"+costMs);
        return pingMsValue;
    }

    private void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // min/avg/max/mdev = 10.578/12.489/16.958/2.283 ms
}



//
//    public void showNetSpeed() {
//        long nowTotalRxBytes = getTotalRxBytes();
//        long nowTimeStamp = System.currentTimeMillis();
//        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));
//
//        lastTimeStamp = nowTimeStamp;
//        lastTotalRxBytes = nowTotalRxBytes;
//
//        String strSpd = String.valueOf(speed) + " kb/s";
//        Log.w(TAG, "speed..........：" + strSpd);
//
//        long mobileTxPkt = TrafficStats.getMobileTxPackets();
//        long mobileRxPkt = TrafficStats.getMobileRxPackets();
//
//        long mobileTxBys = TrafficStats.getMobileTxBytes();
//        long mobileRxBys = TrafficStats.getMobileRxBytes();
//
//        long totalTxPkt = TrafficStats.getTotalTxPackets();
//        long totalRxPkt = TrafficStats.getTotalRxPackets();
//
//        long totalTxBys = TrafficStats.getTotalTxBytes();
//        long totalRxBys = TrafficStats.getTotalRxBytes();
//
//        long uidTxBys = TrafficStats.getUidTxBytes(activityCtx.getApplicationInfo().uid);
//        long uidRxBys = TrafficStats.getUidRxBytes(activityCtx.getApplicationInfo().uid);
//
//        long uidTxPkt = TrafficStats.getUidTxPackets(activityCtx.getApplicationInfo().uid);
//        long uidRxPkt = TrafficStats.getUidRxPackets(activityCtx.getApplicationInfo().uid);
//
//
//        long appSndBw = (uidTxBys-appNetSndBytes)*8;
//        long appRcvBw = (uidRxBys-appNetRcvBytes)*8;
//
//        int curId = aryIdx % NET_BW_AVRY_NUM;
//        appNetSnd3AryBw[curId]=appSndBw;
//        appNetRcv3AryBw[curId]=appRcvBw;
//
//        appNetSndBw = caluNetBw(appNetSnd3AryBw)/NET_BW_AVRY_NUM;
//        appNetRcvBw = caluNetBw(appNetRcv3AryBw)/NET_BW_AVRY_NUM;
//
//        aryIdx++;
//        Log.w(TAG, "mobileTxPkt..........：" + mobileTxPkt);
//        Log.w(TAG, "mobileRxPkt..........：" + mobileRxPkt);
//        Log.w(TAG, "mobileTxBys..........：" + mobileTxBys);
//        Log.w(TAG, "mobileRxBys..........：" + mobileRxBys);
//
//        Log.w(TAG, "totalTxPkt...........：" + totalTxPkt);
//        Log.w(TAG, "totalRxPkt...........：" + totalRxPkt);
//        Log.w(TAG, "totalTxBys...........：" + totalTxBys);
//        Log.w(TAG, "totalRxBys...........：" + totalRxBys);
//
//        Log.w(TAG, "uidTxBys................：" + uidTxBys);
//        Log.w(TAG, "uidRxBys................：" + uidRxBys);
//        Log.w(TAG, "uidTxBys-Bw(bits).......：" + appSndBw+", aver3"+appNetSndBw+", (Bytes):"+appSndBw/8+", aver3:"+appNetSndBw/8);
//        Log.w(TAG, "uidRxBys-Bw(bits).......：" + appRcvBw+", aver3"+appNetRcvBw+", (Bytes):"+appRcvBw/8+", aver3:"+appNetRcvBw/8);
//        Log.w(TAG, "uidTxPkt.............：" + uidTxPkt);
//        Log.w(TAG, "uidRxPkt.............：" + uidRxPkt);
//        appNetSndBytes = uidTxBys;
//        appNetRcvBytes = uidRxBys;
//    }

//            Process localProcess = null;
//            try {
//                localProcess = Runtime.getRuntime().exec("getprop net.dns1");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String os=System.getProperty("os.name");
//            String ip =System.getProperty("os.ip");
//            strDnsIp = localProcess.toString()+", "+os+", "+ip;
//strDnsIp = System.getProperty("net.dns1") + ", " + System.getProperty("net.dns2") + ", " + System.getProperty("net.dns3");


//                    Log.w(TAG, "total-num:" + dnsIpAry.length);
//                    for (i = 0; i < dnsIpAry.length; i++) {
//                        if (dnsIpAry[i].length() <= 0) {
//                            continue;
//                        }
//                        if(dnsIpAry[i].indexOf("net.dns")!=-1){
//
//                            Log.w(TAG, ", id:" + i + ", tag:" + dnsIpAry[i]);
//                            Log.w(TAG, ", id:" + i + ", tag:" + dnsIpAry[i+1]);
//                            //strOut+=dnsIpAry[i]+dnsIpAry[i+1]+", ";
//                        }
//                        if (' ' == dnsIpAry[i].charAt(0)) {
//                            continue;
//                        }
//                        Log.w(TAG, ", id:" + i + ", tag:" + dnsIpAry[i]);
//                        if (count < cpuSlipt.length) {
//                            cpuSlipt[count] = dnsIpAry[i];
//                        }
//                        strOut+=dnsIpAry[i];
//                        count++;
//                    }
//                    //Log.w(TAG, title + ", id:" + i + ", total:" + count);
//                    if (count > 7) {
//                        String strCpuPencent = cpuSlipt[2];
//                        cpuPara.cpuPencent = myAatoi(strCpuPencent);
//
//                        String strMemRssKb = cpuSlipt[6];
//                        cpuPara.memRss = myAatoi(strMemRssKb);
//
//                        cpuPara.progressName = cpuSlipt[count - 1];
//                        Log.w(TAG, title + ", progress cpu pencent:" + strCpuPencent + ", mem rss kb:" + strMemRssKb + ", progress name:" + cpuPara.progressName + ", cpu:" + cpuPara.cpuPencent + ", rss:" + cpuPara.memRss);
//                    }
//                int pos = str2.indexOf("Name");
//                if (pos > 0) {
//                    startTag = true;
//                }


