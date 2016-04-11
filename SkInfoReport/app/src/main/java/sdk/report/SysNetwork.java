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
    String TAG = "SdkReport_" + SysNetwork.class.getSimpleName();
    Context activityCtx = null;
    String mLogHost = null;
    String hostIpAddr = null;

    long lastTotalRxBytes = 0;
    long lastTimeStamp = 0;
    long appNetSndBytes = 0;
    long appNetRcvBytes = 0;
    int netPingMs = 0;

    public SysNetwork(Context ctx) {
        activityCtx = ctx;
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

    public void showNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        String strSpd = String.valueOf(speed) + " kb/s";
        Log.w(TAG, "speed..........：" + strSpd);

        long mobileTxPkt = TrafficStats.getMobileTxPackets();
        long mobileRxPkt = TrafficStats.getMobileRxPackets();

        long mobileTxBys = TrafficStats.getMobileTxBytes();
        long mobileRxBys = TrafficStats.getMobileRxBytes();

        long totalTxPkt = TrafficStats.getTotalTxPackets();
        long totalRxPkt = TrafficStats.getTotalRxPackets();

        long totalTxBys = TrafficStats.getTotalTxBytes();
        long totalRxBys = TrafficStats.getTotalRxBytes();

        long uidTxBys = TrafficStats.getUidTxBytes(activityCtx.getApplicationInfo().uid);
        long uidRxBys = TrafficStats.getUidRxBytes(activityCtx.getApplicationInfo().uid);
        appNetSndBytes = uidTxBys;
        appNetRcvBytes = uidRxBys;

        long uidTxPkt = TrafficStats.getUidTxPackets(activityCtx.getApplicationInfo().uid);
        long uidRxPkt = TrafficStats.getUidRxPackets(activityCtx.getApplicationInfo().uid);

        Log.w(TAG, "mobileTxPkt..........：" + mobileTxPkt);
        Log.w(TAG, "mobileRxPkt..........：" + mobileRxPkt);
        Log.w(TAG, "mobileTxBys..........：" + mobileTxBys);
        Log.w(TAG, "mobileRxBys..........：" + mobileRxBys);

        Log.w(TAG, "totalTxPkt...........：" + totalTxPkt);
        Log.w(TAG, "totalRxPkt...........：" + totalRxPkt);
        Log.w(TAG, "totalTxBys...........：" + totalTxBys);
        Log.w(TAG, "totalRxBys...........：" + totalRxBys);

        Log.w(TAG, "uidTxBys.............：" + uidTxBys);
        Log.w(TAG, "uidRxBys.............：" + uidRxBys);
        Log.w(TAG, "uidTxPkt.............：" + uidTxPkt);
        Log.w(TAG, "uidRxPkt.............：" + uidRxPkt);
    }

    //===========
    public boolean pingHost(String str) {
        String result = null;
        try {
            String ip = "www.baidu.com";// 除非百度挂了，否则用这个应该没问题~
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);//ping3次
// 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i("TTT", "result content : " + stringBuffer.toString());

// PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
            }

        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Log.i("TTT", "result = " + result);
        }

        return false;

    }


//    public String pingHost(String str){
//        String resault="";
//        Log.w(TAG, "ping-host-run");
//        try {
//            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " +str);
//            int status = p.waitFor();
//            if (status == 0) {
//                // mTextView.setText("success") ;
//                resault="success";
//            }
//            else
//            {
//                resault="faild";
//                // mTextView.setText("fail");
//            }
//        } catch (IOException e) {
//            // mTextView.setText("Fail: IOException"+"\n");
//        } catch (InterruptedException e) {
//            // mTextView.setText("Fail: InterruptedException"+"\n");
//        }
//        Log.w(TAG, "ping-host-ext:"+resault);
//        return resault;
//    }

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

    public int getPingMs() {
        return netPingMs;
    }
}
