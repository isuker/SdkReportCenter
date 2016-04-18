package sdk.report;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

/**
 * Created by suker on 16-4-11.
 */
public class SocketManager {
    private String TAG = "SdkReport_" + SocketManager.class.getSimpleName();
    private DatagramSocket udpSocket = null;
    private InetAddress serverAddress = null;

    private String REPORT_TCP_SERVER = "http://192.168.0.111/";
    private String REPORT_UDP_SERVER = "192.168.0.111";
    private int REPORT_UDP_PORT = 12345;

    private ReportCenter rcCtx = null;
    public SocketManager(ReportCenter rc) {
        rcCtx = rc;
    }

    public void udpSend(String data) {
        if (udpSocket == null) {
            try {
                udpSocket = new DatagramSocket();
            } catch (SocketException e) {
                Log.e(TAG, "new-Datagram-Socket Fail");
                return;
            }
        }

        do {
            if (serverAddress == null) {
                try {
                    serverAddress = InetAddress.getByName(REPORT_UDP_SERVER);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "get-By-Name, port:" + REPORT_UDP_SERVER + ", Fail");
                    break;
                }
            }

            byte[] bytes = data.getBytes();
            if (bytes == null) {
                Log.e(TAG, "data-get-Bytes null");
                break;
            }
            DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, serverAddress,
                    REPORT_UDP_PORT);

            try {
                udpSocket.send(udpPacket);
            } catch (IOException e) {
                Log.e(TAG, "udp-send fail");
                break;
            }
        } while (false);

        udpSocket.close();
        udpSocket = null;
    }

    private boolean firstAuth = false;

    public void getDTOArray(String jsonString){
        JSONArray array = null;
        try {
            JSONTokener jsonParser = new JSONTokener(jsonString);
            JSONObject person = (JSONObject) jsonParser.nextValue();
            int hbItv = person.getInt("heartbeatInterval");
            Log.i(TAG, "================hbItv::"+hbItv);
            rcCtx.getParamPlay().setiHeartBeatIvt(hbItv);
            String strIp = person.getString("internetIp");
            Log.i(TAG, "================internetIp::" + strIp);
            rcCtx.getParamPlay().setStrOutIp(strIp);

            String token = person.getString("token");
            Log.i(TAG, "================token::" + token);
            rcCtx.getParamPlay().setStrToken(token);
        } catch (JSONException ex) {
            // 异常处理代码
        }
    }



    public boolean tcpAuth(String pubKey) {
        Log.i(TAG, "================tcp-auth-bgn");
        HttpClient httpclient = new DefaultHttpClient();

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000); //连接时间
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000); //数据传输时间

        try {
            String serverUrl =  "http://192.168.0.111";;
            Log.i(TAG, "================server-port-url::" + serverUrl);
            HttpGet post = new HttpGet(serverUrl);
            HttpResponse response = httpclient.execute(post);
            StatusLine rspStatus = response.getStatusLine();

            Log.i(TAG, "server response status line:" + rspStatus.toString()
                    + ", reason:" + rspStatus.getReasonPhrase()
                    + ", prover:" + rspStatus.getProtocolVersion().toString()
                    + ", statcode:" + rspStatus.getStatusCode());

            HttpEntity resEntity = response.getEntity();
            InputStreamReader reader = new InputStreamReader(
                    resEntity.getContent(), "utf-8");

            BufferedReader in = new BufferedReader(reader);
            in.toString();

            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }

            getDTOArray(buffer.toString());

            Log.i(TAG, "Report response: " + buffer.toString());
            //{"id":"e113f1ad0cf2cc3171000021","frameRate":15,"bitRate":600000,"resolution":"480*854","codeProfile":"profile","publicKey":"86544f1e54d0b15dbb8bade300d1fe5f","bundle":"com.arenacloud.broadcast","vodBuffer":10,"liveBuffer":3,"antiShake":true,"filter":true,"createAt":"2016-01-12T11:28:54.054Z","updateAt":"2016-01-13T10:42:31.031Z","internetIp":"101.81.52.44","token":"a40847f9-adb0-4671-a283-e46aca7a8778","heartbeatInterval":30,"platform":"android"}


        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "================tcp-send-tag-UnsupportedEncodingException::");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.i(TAG, "================tcp-send-tag-IOException::");
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "================tcp-auth-end");
        return true;
    }

    public void tcpSend(String strIn, String strTag) {
        Log.i(TAG, "================tcp-send-tag-bgn::" + strTag);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000); //连接时间
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000); //数据传输时间

        try {
            HttpEntity entity = new StringEntity(strIn, "utf-8");

            String serverUrl = REPORT_TCP_SERVER+""+strTag;
            Log.i(TAG, "================server-port-url::" + serverUrl);

            HttpPost post = new HttpPost(serverUrl);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(entity);

            HttpResponse response = httpclient.execute(post);

            StatusLine rspStatus = response.getStatusLine();

            Log.i(TAG, "server response status line:" + rspStatus.toString()
                    + ", reason:" + rspStatus.getReasonPhrase()
                    + ", prover:" + rspStatus.getProtocolVersion().toString()
                    + ", statcode:" + rspStatus.getStatusCode());

            HttpEntity resEntity = response.getEntity();
            InputStreamReader reader = new InputStreamReader(
                    resEntity.getContent(), "utf-8");

            BufferedReader in = new BufferedReader(reader);
            in.toString();

            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            Log.i(TAG, "Report response: " + buffer.toString());

        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "================tcp-send-tag-UnsupportedEncodingException::");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "================tcp-send-tag-IOException::");
            e.printStackTrace();
        }
        Log.i(TAG, "================tcp-send-tag-end::" + strTag);
    }
}
