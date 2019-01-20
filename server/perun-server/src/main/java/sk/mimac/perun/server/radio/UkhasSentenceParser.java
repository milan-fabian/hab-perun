package sk.mimac.perun.server.radio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.model.SensorType;

/**
 * Description of protocol: https://ukhas.org.uk/communication:protocol
 *
 * Example line:
 * $$CHANGEME,125,00:00:00,0.00000,0.00000,00000,0,0,0,34.6,0.0,0*D6A0
 *
 * @author Mimac
 */
public class UkhasSentenceParser implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UkhasSentenceParser.class);
    private static final String CALLSIGN = "CHANGEME";

    private final RadioService radioService;
    private final String host;
    private final int port;

    public UkhasSentenceParser(RadioService radioService, String host, int port) {
        this.radioService = radioService;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        while (true) {
            runInternal();
        }
    }

    private void runInternal() {
        try (Socket socket = new Socket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            LOG.debug("Got connection");
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (Exception ex) {
            LOG.warn("Unexpected exception", ex);
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException ignore) {
            }
        }
    }

    protected void processLine(String line) {
        if (checkLine(line)) {
            LOG.debug("Valid line: \"{}\"", line);
            saveData(line.substring(0, line.length() - 5).split(","));
        } else {
            LOG.debug("Invalid line: \"{}\"", line);
        }
    }

    private void saveData(String[] parts) {
        long timestamp = System.currentTimeMillis();
        if (parts.length > 5) {
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.LAT, "ublox", Float.parseFloat(parts[3])));
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.LNG, "ublox", Float.parseFloat(parts[4])));
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.ALT, "ublox", Float.parseFloat(parts[5])));

        }
        if (parts.length > 9 && !parts[9].isEmpty()) {
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.TEMP, "rpi", Float.parseFloat(parts[9])));
        }
        if (parts.length > 14 && !parts[12].isEmpty() && !parts[13].isEmpty() && !parts[14].isEmpty()) {
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.LAT, "gn4_gps", Float.parseFloat(parts[12])));
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.LNG, "gn4_gps", Float.parseFloat(parts[13])));
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.ALT, "gn4_gps", Float.parseFloat(parts[14])));
        }
        if (parts.length > 15 && !parts[15].isEmpty()) {
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.PRES, "gn4_pressure", Float.parseFloat(parts[15])));
        }
        if (parts.length > 16 && !parts[16].isEmpty()) {
            radioService.insert(new PayloadStatus.SensorStatus(timestamp, SensorType.BAT_LVL, "gn4_bat", Float.parseFloat(parts[16])));
        }
    }

    private boolean checkLine(String line) {
        // first sanity check
        if (!line.startsWith("$$" + CALLSIGN) || line.length() < 15 || line.charAt(line.length() - 5) != '*' || !line.contains(",")) {
            return false;
        }
        int index = line.lastIndexOf('*');
        String calculatedChecksum = calculateCRC16CCITT(line.substring(2, index));
        String realChecksum = line.substring(index + 1);
        LOG.debug("Checksum calculated=" + calculatedChecksum + ", real=" + realChecksum);
        return calculatedChecksum.equalsIgnoreCase(realChecksum);
    }

    /**
     * https://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java
     */
    private String calculateCRC16CCITT(String text) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xffff;
        String result = Integer.toHexString(crc);
        return result.length() == 3 ? "0" + result : result;
    }

}
