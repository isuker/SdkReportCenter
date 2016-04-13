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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * Created by suker on 16-4-11.
 */
public class SocketManager {
    private String TAG = "SdkReport_" + SocketManager.class.getSimpleName();
    private DatagramSocket udpSocket = null;
    private InetAddress serverAddress = null;
    private String REPORT_TCP_SERVER = "http://a.b.com/report";
    private String REPORT_UDP_SERVER = "a.b.com";
    private int REPORT_UDP_PORT = 12345;

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

    public void tcpSend(String strIn) {
        HttpClient httpclient = new DefaultHttpClient();

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000); //连接时间
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000); //数据传输时间

        try {
            HttpEntity entity = new StringEntity(strIn, "utf-8");
            HttpPost post = new HttpPost(REPORT_TCP_SERVER);
            post.addHeader("Content-Type", "text/xml");
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
