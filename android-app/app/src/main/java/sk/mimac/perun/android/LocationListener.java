package sk.mimac.perun.android;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class LocationListener implements android.location.LocationListener {

    private static final String TAG = "LocationListener";

    private Location lastLocation;

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.getTime() + ", " + location.toString());
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
