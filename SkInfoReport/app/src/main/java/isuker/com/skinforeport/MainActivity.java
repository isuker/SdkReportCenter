package isuker.com.skinforeport;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;

import sdk.report.ReportCenter;

public class MainActivity extends Activity {
    final String TAG = "SdkReport_" + MainActivity.class.getSimpleName(); // ReportCenter
    ReportCenter reportInfo;
    Context activicyMain;
    VideoView videoView;
    String playUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
    // "rtmp://www.baidu.com/myapp"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "main-on-create-run");
        activicyMain = this;


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

//        new Thread() {
//            public void run() {
                Log.w(TAG, "main-on-create-report-start");
                reportInfo = new ReportCenter(activicyMain);
                reportInfo.initPlay(playUrl, "1.2.3.4", 3 * 1000); // 30*1000
                //reportInfo.initPlay("http://www.baidu.com/a.m3u8", "1.2.3.4", 3 * 1000);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_START_PLAY);
                msSleep(100);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_VIDEO_FIRST_FRAME);
                msSleep(100);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_VIDEO_FIRST_DISPLAY);
                msSleep(100);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_PAUSE_BEGIN);
                msSleep(100);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_PAUSE_END);
                msSleep(100);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_FAIL);

                int count = 0;
                while (true)
                {
                    try {
                        Log.w(TAG, "main-on-create-start-ping");
                        Process p = Runtime.getRuntime().exec("ping -c 5 -i 0.2 " + "devimages.apple.com");
                        p = Runtime.getRuntime().exec("wget http://www.xuebuyuan.com/1726264.html");
                        p = Runtime.getRuntime().exec("wget http://blog.csdn.net/heng615975867/article/details/22812671");
                        Log.w(TAG, "main-on-create-stop-ping");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    msSleep(1 * 1000);
                    if (count ++ > 50)
                    {
                        break;
                    }
                }
//                msSleep(40 * 1000);
                reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_STOP_PLAY);
                Log.w(TAG, "main-on-create-report-end");
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

    }

    private void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
