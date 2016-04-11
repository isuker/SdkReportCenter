package isuker.com.skinforeport;

import android.app.Activity;
import android.os.Bundle;

import sdk.report.ReportCenter;

public class MainActivity extends Activity {
    ReportCenter reportInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reportInfo = new ReportCenter(this);
        reportInfo.initPlay("rtmp://a.b.c.d/myapp", "1.2.3.4", 3 * 1000); // 30*1000
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
        msSleep(12 * 1000);
        reportInfo.reportData(ReportCenter.SDK_REPORT_PLAY_STOP_PLAY);
    }

    private void msSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
