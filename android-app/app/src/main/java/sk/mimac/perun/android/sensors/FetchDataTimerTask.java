package sk.mimac.perun.android.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import sk.mimac.perun.android.pipe.PipeCommunicator;
import sk.mimac.perun.android.webclient.ServiceConnector;
import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.model.SensorType;

public class FetchDataTimerTask extends TimerTask {

    private static final String TAG = "FetchDataTimerTask";

    private final Context context;
    private final SensorManager sensorManager;
    private final LocationListener locationListener;
    private final PhoneSignalListener phoneSignalListener;
    private final PipeCommunicator pipeCommunicator;

    private int counter = 0;

    @SuppressLint("MissingPermission")
    public FetchDataTimerTask(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        locationListener = new LocationListener();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        phoneSignalListener = new PhoneSignalListener();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        pipeCommunicator = new PipeCommunicator();
        pipeCommunicator.start();
    }

    @Override
    public void run() {
        Log.d(TAG, "Tick start");
        counter++;
        float batteryLevel = getBatteryLevel();
        long batteryTimestamp = System.currentTimeMillis();
        // Lower power consumption at 15% (process 2 ouf of 3), 10% (process every third) and 5% (process every fifth)
        if ((batteryLevel < 15 && counter % 3 == 0) || (batteryLevel < 10 && counter % 3 != 0)|| (batteryLevel < 5 && counter % 5 != 0)) {
            Log.d(TAG, "Low battery, skipping tick");
            return;
        }

        SensorListener sensorListener = new SensorListener(sensorManager);
        // Lower power consumption at 25%, measure sensor data every second run
        if (batteryLevel > 25 || counter % 2 == 0) {
            for (int type : Arrays.asList(Sensor.TYPE_PRESSURE, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER)) {
                Sensor sensor = sensorManager.getDefaultSensor(type);
                sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        new Thread(() -> CameraUtils.getPicture(counter % 2 == 1)).start();

        try {
            Thread.sleep(550);
        } catch (InterruptedException ignore) {
        }
        sensorManager.unregisterListener(sensorListener);

        PayloadStatus status = new PayloadStatus();
        status.setSensors(new ArrayList<>());
        status.getSensors().add(new PayloadStatus.SensorStatus(batteryTimestamp, SensorType.BAT_LVL, "gn4_bat", batteryLevel));
        status.getSensors().add(new PayloadStatus.SensorStatus(System.currentTimeMillis(), SensorType.TEMP, "gn4_bat", getBatteryTemperature()));
        addLocationData(status.getSensors());
        addSignalStrengthData(status.getSensors());
        status.getSensors().addAll(sensorListener.getStatuses());
        status.getSensors().addAll(pipeCommunicator.getLastStatuses());
        pipeCommunicator.setLastStatuses(status.getSensors());
        ServiceConnector.sendSensorData(status);
        Log.d(TAG, "Tick done");
    }

    public void stop() {
        pipeCommunicator.stop();
    }


    private float getBatteryTemperature() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float temp = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
        Log.d(TAG, "Battery temperature: " + String.valueOf(temp) + " °C");
        return temp;
    }

    private int getBatteryLevel() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        Log.d(TAG, "Battery level: " + level + "%");
        return level;
    }

    private void addSignalStrengthData(List<PayloadStatus.SensorStatus> sensors) {
        Integer signalStrength = phoneSignalListener.getLastStrength();
        if (signalStrength != null) {
            sensors.add(new PayloadStatus.SensorStatus(System.currentTimeMillis(), SensorType.PHN_SGNL, "gn4_gsm", signalStrength));
        }
    }

    private void addLocationData(List<PayloadStatus.SensorStatus> sensors) {
        Location location = locationListener.getLastLocation();
        if (location != null && (System.currentTimeMillis() - location.getTime() < 20_000)) {
            long timestamp = location.getTime();
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.LAT, "gn4_gps", (float) location.getLatitude()));
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.LNG, "gn4_gps", (float) location.getLongitude()));
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.ALT, "gn4_gps", (float) location.getAltitude()));
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.SPD, "gn4_gps", location.getSpeed()));
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.BEAR, "gn4_gps", location.getBearing()));
            sensors.add(new PayloadStatus.SensorStatus(timestamp, SensorType.POS_ACUR, "gn4_gps", location.getAccuracy()));
        }
    }
}
