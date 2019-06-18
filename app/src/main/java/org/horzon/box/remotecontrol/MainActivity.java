package org.horzon.box.remotecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

public class MainActivity extends Activity {


    //private WifiManager wifiManager;
    private static final String TAG = "MainActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        CatchScreenService.screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        CatchScreenService.screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
        CatchScreenService.pixelformat = display.getPixelFormat();

/*
        wifiManager.setWifiEnabled(false);

        try {
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "HZ_Remote-"+ 2356;

            //配置热点的密码
            apConfig.preSharedKey = "12342356";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            method.invoke(wifiManager, apConfig, true);
        }catch (Exception e){
            Log.e(TAG, "onCreate: ", e);
        }
*/



        Intent serviceIntent = new Intent(this, CatchScreenService.class);

        startService(serviceIntent);

        finish();
    }


}
