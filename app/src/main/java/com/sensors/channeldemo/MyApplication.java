package com.sensors.channeldemo;

import android.app.Application;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sensorsdata.analytics.android.sdk.SensorsDataAPI.sharedInstance;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initSA();
        try {
            String downloadChannel = null;
            // 获取下载商店的渠道
            downloadChannel = SensorsDataUtils.getApplicationMetaData(this, "DOWNLOAD_CHANNEL");
            JSONObject properties = new JSONObject();
            //这里示例 DownloadChannel 记录下载商店的渠道(下载渠道)。如果需要多个字段来标记渠道包，请按业务实际需要添加。
            properties.put("DownloadChannel", downloadChannel);
            //记录激活事件、渠道追踪，这里激活事件取名为 AppInstall。
            SensorsDataAPI.sharedInstance().trackInstallation("AppInstall", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void initSA() {
//        //   if (Buil)
//        SAConfigOptions saConfigOptions = new SAConfigOptions(SA_SERVER_URL);
//
//        saConfigOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_CLICK|
//                SensorsAnalyticsAutoTrackEventType.APP_START|
//                SensorsAnalyticsAutoTrackEventType.APP_END|
//                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN);
//
//
//        SensorsDataAPI.sharedInstance(this, saConfigOptions);

        sharedInstance(this,
                "https://test-hechun.datasink.sensorsdata.cn/sa?project=yuejianzhong&token=d28b875ed9ac268f",
                SensorsDataAPI.DebugMode.DEBUG_AND_TRACK);

        try {
            // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
            List<SensorsDataAPI.AutoTrackEventType> eventTypeList = new ArrayList<>();
            // $AppStart
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_START);
            // $AppEnd
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_END);
            // $AppViewScreen
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN);
            // $AppClick
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_CLICK);
            sharedInstance().enableAutoTrack(eventTypeList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //构建 SSLSocketFactory 传入实例
//        SSLSocketFactory sf ;

        //将实例传入神策
//        SensorsDataAPI.sharedInstance().setSSLSocketFactory(sf);
//        SensorsDataAPI.sharedInstance().enableLog(true);

//        SensorsDataAPI.sharedInstance().set
    }
}
