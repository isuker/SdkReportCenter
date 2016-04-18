package isuker.com.skinforeport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by suker on 16-4-14.
 */
public class PakageInfoProvider {
    private static final String tag = "GetappinfoActivity";
    private Context context;
//    private List<AppInfo> appInfos;
//    private AppInfo appInfo;

    public PakageInfoProvider(Context context) {
        super();
        this.context = context;
    }

    public void getAppInfo() {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo packageInfo : pakageinfos) {
            String appname = context.getString(R.string.app_name);
            String resappname = context.getResources().getString(R.string.app_name);
            String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
            Log.i(tag, "context-appname:" + appname + ", res:" + resappname);
            Log.i(tag, "str_name:" + str_name + ", packageInfo.versionName:" + packageInfo.versionName + ", packageInfo.packageName:" + packageInfo.packageName);
        }
    }
}