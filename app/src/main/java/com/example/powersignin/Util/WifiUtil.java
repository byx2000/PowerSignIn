package com.example.powersignin.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class WifiUtil
{
    private Activity activity;
    private WifiManager wifiManager;

    public interface WifiHotspotOpenListener
    {
        void succeed(String ssid);
        void failed(String info);
    }

    public interface WifiHotspotCloseListener
    {
        void succeed();
        void failed(String info);
    }

    public WifiUtil(Activity activity)
    {
        this.activity = activity;
        //wifiManager = (WifiManager)activity.getApplicationContext().getSystemService(activity.WIFI_SERVICE);
        wifiManager = (WifiManager)activity.getApplication().getApplicationContext().getSystemService(activity.WIFI_SERVICE);
    }

    //获取本机bssid
    public String getBssid()
    {
        String macAddress = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(activity.WIFI_SERVICE);
                WifiInfo wifiInfo = null;
                if (wifiManager != null) {
                    wifiInfo = wifiManager.getConnectionInfo();
                }
                macAddress = wifiInfo.getMacAddress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iF = interfaces.nextElement();

                    byte[] addr = iF.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        continue;
                    }

                    StringBuilder buf = new StringBuilder();
                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    String mac = buf.toString();
                    Log.d("TAG", "interfaceName=" + iF.getName() + ", mac=" + mac);

                    if (TextUtils.equals(iF.getName(), "wlan0")) {
                        macAddress = mac;
                    }
                    // 适配小米手机
                    if (TextUtils.equals(iF.getName(), "softap0")) {
                        return mac;
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                return macAddress;
            }
        }
        return macAddress;
    }

    //判断是否开启GPS
    public boolean isGpsOpen()
    {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        //GPS卫星定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network)
        {
            return true;
        }

        return false;
    }

    //判断是否开启热点
    public boolean isHotspotOpen()
    {
        Method method = null;
        int i = 0;

        try
        {
            method = wifiManager.getClass().getMethod("getWifiApState");
            method.setAccessible(true);
            i = (Integer) method.invoke(wifiManager);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // 10---正在关闭；11---已关闭；12---正在开启；13---已开启
        return i == 13;
    }

    //Android8.0及以上 打开热点
    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public void openHotspotOreo(final WifiHotspotOpenListener wifiHotspotOpenListener)
    {


        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback()
        {
            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation)
            {
                super.onStarted(reservation);
                String ssid = reservation.getWifiConfiguration().SSID;
                wifiHotspotOpenListener.succeed(ssid);
            }

            @Override
            public void onStopped()
            {
                super.onStopped();
            }

            @Override
            public void onFailed(int reason)
            {
                super.onFailed(reason);
                wifiHotspotOpenListener.failed("wifi hotspot open failed on oreo");
            }
        }, new Handler());
    }*/

    /*public void closeHotspotOreo(WifiHotspotCloseListener wifiHotspotCloseListener)
    {
        try
        {
            @SuppressLint("PrivateApi") Method method = wifiManager.getClass().getDeclaredMethod("cancelLocalOnlyHotspotRequest");
            method.invoke(wifiManager);
        }
        catch (NoSuchMethodException e)
        {
            //Toast.makeText(activity,"没有此方法",Toast.LENGTH_SHORT).show();
            wifiHotspotCloseListener.failed("no such method");
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            //Toast.makeText(activity,"方法无法访问",Toast.LENGTH_SHORT).show();
            wifiHotspotCloseListener.failed("method cannot be access");
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            //Toast.makeText(activity,"方法执行错误",Toast.LENGTH_SHORT).show();
            wifiHotspotCloseListener.failed("method invoke failed");
            e.printStackTrace();
        }

        wifiHotspotCloseListener.succeed();
    }*/

    //Android 8.0以下 打开热点
    /*public void openHotspotPreOreo(WifiHotspotOpenListener wifiHotspotOpenListener)
    {
        if (wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }

        try
        {
            //配置热点属性
            WifiConfiguration config = new WifiConfiguration();

            //构造随机ssid和密码
            String ssid = "AndroidShare_" + (new Random().nextInt(9000) + 1000);
            String password = "000000000000";
            char[] s = password.toCharArray();
            for (int i = 0; i < 12; ++i)
            {
                if (new Random().nextInt(2) == 1)
                {
                    s[i] = (char)(new Random().nextInt(10) + '0');
                    while (s[i] == '1')
                    {
                        s[i] = (char)(new Random().nextInt(10) + '0');
                    }
                }
                else
                {
                    s[i] = (char)(new Random().nextInt(26) + 'a');
                    while (s[i] == 'l')
                    {
                        s[i] = (char)(new Random().nextInt(26) + 'a');
                    }
                }
            }
            password = new String(s);

            config.SSID = ssid;
            config.preSharedKey = password;
            //config.hiddenSSID = true;
            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(4);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            @SuppressLint("PrivateApi") Method method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.setAccessible(true);
            boolean ret = (boolean)method.invoke(wifiManager, config, true);
            if (ret)
            {
                //Toast.makeText(MainActivity.this,"执行成功",Toast.LENGTH_SHORT).show();
                wifiHotspotOpenListener.succeed(ssid);
            }
            else
            {
                //Toast.makeText(activity, "热点创建失败", Toast.LENGTH_SHORT).show();
                wifiHotspotOpenListener.failed("wifi hotspot open failed pre oreo");
            }
        }
        catch (Exception e)
        {
            //Toast.makeText(activity, "异常", Toast.LENGTH_SHORT).show();
            wifiHotspotOpenListener.failed("exception pre oreo");
            e.printStackTrace();
        }
    }*/

    //Android8.0以下 关闭热点
    /*private void closeHotspotPreOreo(WifiHotspotCloseListener wifiHotspotCloseListener)
    {
        try
        {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled",      WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, null, false);
        }
        catch (Exception e)
        {
            //Toast.makeText(activity, "异常", Toast.LENGTH_SHORT).show();
            wifiHotspotCloseListener.failed("exception on oreo");
            e.printStackTrace();
        }

        //Toast.makeText(MainActivity.this, "执行失败", Toast.LENGTH_SHORT).show();
        wifiHotspotCloseListener.succeed();
    }*/

    //打开热点
    /*public void openHotspot(WifiHotspotOpenListener wifiHotspotOpenListener)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (isGpsOpen())
            {
                openHotspotOreo(wifiHotspotOpenListener);
            }
            else
            {
                wifiHotspotOpenListener.failed("Please open GPS");
            }
        }
        else
        {
            openHotspotPreOreo(wifiHotspotOpenListener);
        }
    }*/

    //关闭热点
    /*public void closeHotspot(WifiHotspotCloseListener wifiHotspotCloseListener)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            closeHotspotOreo(wifiHotspotCloseListener);
        }
        else
        {
            closeHotspotPreOreo(wifiHotspotCloseListener);
        }
    }*/

    //判断wifi开关是否开启
    public boolean isWifiEnabled()
    {
        return wifiManager.isWifiEnabled();
    }

    //设置wifi开关
    public void setWifiEnabled(boolean enabled)
    {
        wifiManager.setWifiEnabled(enabled);
    }

    //开始扫描wifi
    public void startScanWifi()
    {
        if (!isWifiEnabled())
        {
            setWifiEnabled(true);
        }

        wifiManager.startScan();
    }

    //获取扫描结果
    public List<ScanResult> getScanResults()
    {
        return wifiManager.getScanResults();
    }

    //判断是否连接网络
    public boolean isNetworkConnected()
    {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null)
        {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
}
