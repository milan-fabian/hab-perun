package sk.mimac.perun.android.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.model.SensorType;

public class SensorListener implements SensorEventListener {

    private static final String TAG = "SensorListener";

    private final SensorManager sensorManager;
    private List<PayloadStatus.SensorStatus> statuses = new ArrayList<>();

    public SensorListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG,  "Sensor '" + event.sensor.getStringType() + "' measured: " + Arrays.toString(event.values));
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:
                statuses.add(new PayloadStatus.SensorStatus(SensorType.PRESSURE, "phone_pressure", event.values[0]));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                statuses.add(new PayloadStatus.SensorStatus(SensorType.MAG_X, "phone_mag", event.values[0]));
                statuses.add(new PayloadStatus.SensorStatus(SensorType.MAG_Y, "phone_mag", event.values[1]));
                statuses.add(new PayloadStatus.SensorStatus(SensorType.MAG_Z, "phone_mag", event.values[2]));
                break;
            case Sensor.TYPE_ACCELEROMETER:
                statuses.add(new PayloadStatus.SensorStatus(SensorType.ACC_X, "phone_acc", event.values[0]));
                statuses.add(new PayloadStatus.SensorStatus(SensorType.ACC_Y, "phone_acc", event.values[1]));
                statuses.add(new PayloadStatus.SensorStatus(SensorType.ACC_Z, "phone_acc", event.values[2]));
                break;
        }
        sensorManager.unregisterListener(this, event.sensor);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public List<PayloadStatus.SensorStatus> getStatuses() {
        return statuses;
    }
}
