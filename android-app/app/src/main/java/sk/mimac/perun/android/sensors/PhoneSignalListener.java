package sk.mimac.perun.android.sensors;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

public class PhoneSignalListener extends PhoneStateListener {

    private static final String TAG = PhoneSignalListener.class.getSimpleName();
    private static final int MAX_SIGNAL_DBM_VALUE = 31;
    private static final int UNKNOWN_CODE = 99;

    private Integer lastStrength;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        Log.d(TAG, "Phone signal strength: " + signalStrength.getGsmSignalStrength());
        if (signalStrength.getGsmSignalStrength() == UNKNOWN_CODE) {
            lastStrength = 0;
        } else {
            lastStrength = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
        }
    }

    public Integer getLastStrength() {
        return lastStrength;
    }

    private int calculateSignalStrengthInPercent(int signalStrength) {
        return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE * 100);
    }

}
