package sk.mimac.perun.android;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;

import sk.mimac.perun.android.sensors.FetchDataTimerTask;
import sk.mimac.perun.android.webclient.ServiceConnector;

public class MainService extends Service {

    private static final String TAG = "MainService";

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private Timer timer;
    private FetchDataTimerTask fetchDataTimerTask;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initializeServiceConnector(prefs);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hab-perun:service");
        wakeLock.acquire();


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "hab-perun:service");
        wifiLock.acquire();

        fetchDataTimerTask = new FetchDataTimerTask(this);
        timer = new Timer();
        timer.schedule(fetchDataTimerTask, 1000, Integer.parseInt(prefs.getString("fetch_data_interval", "25")) * 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service");
        timer.cancel();
        fetchDataTimerTask.stop();
        wakeLock.release();
        wifiLock.release();
        super.onDestroy();
        Intent broadcastIntent = new Intent("sk.mimac.perun.android.RestartService");
        sendBroadcast(broadcastIntent);
        timer.cancel();
    }

    private void initializeServiceConnector(SharedPreferences prefs) {
        String username = prefs.getString("server_username", null);
        String password = prefs.getString("server_password", null);
        String url = prefs.getString("server_url", null);
        if (url != null && username != null && password != null) {
            ServiceConnector.setUrls(Arrays.asList(url.split("\\n")));
            ServiceConnector.setCredentials(Base64.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
        }
    }
}
