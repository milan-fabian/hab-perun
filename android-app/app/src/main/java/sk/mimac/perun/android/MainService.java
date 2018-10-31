package sk.mimac.perun.android;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Timer;

import sk.mimac.perun.android.service.ServiceConnector;

public class MainService extends Service {

    private static final String TAG = "MainService";
    private static final int TIMER_DELAY = 30_000;

    private LocationManager locationManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Timer timer;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");

        ServiceConnector.setUrl("http://192.168.0.12");
        ServiceConnector.setCredentials("dGVzdDp0ZXN0");

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hab-perun");
        LocationListener locationListener = new LocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

        wakeLock.acquire();
        timer = new Timer();
        timer.schedule(new FetchDataTimerTask(this, locationListener), 1000, TIMER_DELAY);
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
        wakeLock.release();
        super.onDestroy();
        Intent broadcastIntent = new Intent("sk.mimac.perun.android.RestartService");
        sendBroadcast(broadcastIntent);
        timer.cancel();
    }



}
