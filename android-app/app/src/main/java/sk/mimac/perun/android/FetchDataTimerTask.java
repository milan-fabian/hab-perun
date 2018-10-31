package sk.mimac.perun.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import sk.mimac.perun.android.service.ServiceConnector;
import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.model.SensorType;

public class FetchDataTimerTask extends TimerTask {

    private static final String TAG = "FetchDataTimerTask";

    private final Context context;
    private final SensorManager sensorManager;
    private final LocationListener locationListener;

    @SuppressLint("MissingPermission")
    public FetchDataTimerTask(Context context, LocationListener locationListener) {
        this.context = context;
        this.locationListener = locationListener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void run() {
        Log.d(TAG, "Tick");
        SensorListener sensorListener = new SensorListener(sensorManager);
        for (int type : Arrays.asList(Sensor.TYPE_PRESSURE, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER)) {
            Sensor sensor = sensorManager.getDefaultSensor(type);
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        new Thread(() -> CameraUtils.getPicture()).start();

        float batteryTemperature = getBatteryTemperature();
        float batteryLevel = getBatteryLevel();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
        }
        sensorManager.unregisterListener(sensorListener);

        PayloadStatus status = new PayloadStatus();
        status.setTimestamp(System.currentTimeMillis());
        status.setSensors(new ArrayList<>());
        status.getSensors().add(new PayloadStatus.SensorStatus(SensorType.BAT_LVL, null, batteryLevel));
        status.getSensors().add(new PayloadStatus.SensorStatus(SensorType.BAT_TEMP, null, batteryTemperature));
        addLocationData(status.getSensors());
        status.getSensors().addAll(sensorListener.getStatuses());
        ServiceConnector.sendSensorData(status);
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

    private void addLocationData(List<PayloadStatus.SensorStatus> sensors) {
        Location location = locationListener.getLastLocation();
        if (location != null && (System.currentTimeMillis() - location.getTime() < 20_000)) {
            sensors.add(new PayloadStatus.SensorStatus(SensorType.LAT, null, (float) location.getLatitude()));
            sensors.add(new PayloadStatus.SensorStatus(SensorType.LONG, null, (float) location.getLongitude()));
            sensors.add(new PayloadStatus.SensorStatus(SensorType.ALT, null, (float) location.getAltitude()));
            sensors.add(new PayloadStatus.SensorStatus(SensorType.SPEED, null, (float) location.getSpeed()));
            sensors.add(new PayloadStatus.SensorStatus(SensorType.BEAR, null, (float) location.getBearing()));
        }
    }
}
