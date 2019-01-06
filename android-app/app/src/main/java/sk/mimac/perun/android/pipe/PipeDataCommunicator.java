package sk.mimac.perun.android.pipe;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.model.SensorType;

public class PipeDataCommunicator implements Runnable {

    private static final String TAG = PipeDataCommunicator.class.getSimpleName();

    private final DecimalFormat decimalFormat = new DecimalFormat("#.#####", new DecimalFormatSymbols(Locale.ENGLISH));
    private final int pipePort;

    private boolean shouldRun = true;
    private List<PayloadStatus.SensorStatus> receivedStatuses;
    private List<PayloadStatus.SensorStatus> sendStatuses;
    private ServerSocket serverSocket;

    public PipeDataCommunicator(int pipePort) {
        this.pipePort = pipePort;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(pipePort);
            while (shouldRun) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream())) {
                    Log.d(TAG, "Got socket connection");

                    String inputLine = reader.readLine();
                    Log.d(TAG, "Got line: \"" + inputLine + "\"");
                    receivedStatuses = parseLine(inputLine);

                    String outputLine = prepareLine();
                    Log.d(TAG, "Sending line: \"" + outputLine + "\"");
                    writer.append(outputLine);
                } catch (Exception ex) {
                    Log.e(TAG, "Can't read/write data", ex);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Unexpected exception", ex);
        }
    }

    public List<PayloadStatus.SensorStatus> getReceivedStatuses() {
        List<PayloadStatus.SensorStatus> temp = receivedStatuses;
        if (temp == null) {
            return Collections.emptyList();
        } else {
            receivedStatuses = null;
            return temp;
        }
    }

    public void setSendStatuses(List<PayloadStatus.SensorStatus> sendStatuses) {
        this.sendStatuses = sendStatuses;
    }

    public void stop() {
        shouldRun = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
        }
    }

    private List<PayloadStatus.SensorStatus> parseLine(String line) {
        List<PayloadStatus.SensorStatus> result = new ArrayList<>();
        String[] parts = line.split(",");
        long timestamp = System.currentTimeMillis();

        if (!parts[3].equals("0.00000")) {
            result.add(new PayloadStatus.SensorStatus(timestamp, SensorType.LAT, "ublox", Float.parseFloat(parts[3])));
        }
        if (!parts[4].equals("0.00000")) {
            result.add(new PayloadStatus.SensorStatus(timestamp, SensorType.LONG, "ublox", Float.parseFloat(parts[4])));
        }
        if (!parts[5].equals("00000")) {
            result.add(new PayloadStatus.SensorStatus(timestamp, SensorType.ALT, "ublox", Float.parseFloat(parts[5])));
        }
        return result;
    }

    private String prepareLine() {
        List<PayloadStatus.SensorStatus> tempLastStatuses = sendStatuses;

        Float pressure = null;
        Float batteryLevel = null;
        Float lat = null;
        Float lon = null;
        Float alt = null;

        for (PayloadStatus.SensorStatus sensorStatus : tempLastStatuses) {
            if (sensorStatus.getName().equals("phone_pressure") && sensorStatus.getType() == SensorType.PRESSURE) {
                pressure = sensorStatus.getValue();
            } else if (sensorStatus.getName().equals("phone_bat") && sensorStatus.getType() == SensorType.BAT_LVL) {
                batteryLevel = sensorStatus.getValue();
            } else if (sensorStatus.getName().equals("phone_gps") && sensorStatus.getType() == SensorType.LAT) {
                lat = sensorStatus.getValue();
            } else if (sensorStatus.getName().equals("phone_gps") && sensorStatus.getType() == SensorType.LONG) {
                lon = sensorStatus.getValue();
            } else if (sensorStatus.getName().equals("phone_gps") && sensorStatus.getType() == SensorType.ALT) {
                alt = sensorStatus.getValue();
            }
        }
        StringBuilder builder = new StringBuilder();
        // lat,long,alt,pressure,batteryLevel
        builder.append(floatToString(lat)).append(",").append(floatToString(lon)).append(",").append(floatToString(alt)).append(",")
                .append(floatToString(pressure)).append(",").append(floatToString(batteryLevel));
        return builder.toString();
    }

    private String floatToString(Float data) {
        if (data == null) {
            return "";
        }
        return decimalFormat.format(data);
    }

}
