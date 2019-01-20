package sk.mimac.perun.server.radio;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.server.persistence.entity.Sensor;
import sk.mimac.perun.server.persistence.entity.SensorData;
import sk.mimac.perun.server.persistence.repository.SensorDataRepository;
import sk.mimac.perun.server.persistence.repository.SensorRepository;

/**
 *
 * @author Mimac
 */
@Component
public class RadioService {

    private static final Logger LOG = LoggerFactory.getLogger(UkhasSentenceParser.class);

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Value("${perun.radio.setup:#{null}}")
    private String setupString;

    @PostConstruct
    public void setup() {
        if (setupString != null && !setupString.isEmpty()) {
            for (String part : setupString.split(";")) {
                LOG.info("Starting radio service for {}", part);
                String[] address = part.split(":");
                UkhasSentenceParser parser = new UkhasSentenceParser(this, address[0], Integer.parseInt(address[1]));
                Thread thread = new Thread(parser, "UkhasSentenceParser-" + part);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void insert(PayloadStatus.SensorStatus sensorStatus) {
        LocalDateTime dateTime = Instant.ofEpochMilli(sensorStatus.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Sensor sensor = sensorRepository.findByNameAndType(sensorStatus.getName(), sensorStatus.getType());
        if (sensor == null) {
            sensor = new Sensor();
            sensor.setType(sensorStatus.getType());
            sensor.setName(sensorStatus.getName());
            sensor = sensorRepository.save(sensor);
        }
        SensorData entity = new SensorData();
        entity.setSensor(sensor.getId());
        entity.setVal(sensorStatus.getVal());
        entity.setTime(dateTime);
        sensorDataRepository.save(entity);
    }

}
