package sk.mimac.perun.server.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.mimac.perun.model.PayloadStatus;
import sk.mimac.perun.server.persistence.entity.Sensor;
import sk.mimac.perun.server.persistence.entity.SensorData;
import sk.mimac.perun.server.persistence.repository.SensorDataRepository;
import sk.mimac.perun.server.persistence.repository.SensorRepository;

/**
 *
 * @author Mimac
 */
@RestController
public class PayloadRestController {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadRestController.class);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Value("${perun.image.store}")
    private String imageStorePath;

    @PostMapping("/payload/status")
    public void postStatus(@RequestBody PayloadStatus payloadStatus) {
        LOG.debug("Received payload status");
        for (PayloadStatus.SensorStatus sensorStatus : payloadStatus.getSensors()) {
            LocalDateTime dateTime = Instant.ofEpochMilli(sensorStatus.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            Sensor sensor = sensorRepository.findByNameAndType(sensorStatus.getName(), sensorStatus.getType());
            if (sensor == null) {
                sensor = new Sensor();
                sensor.setType(sensorStatus.getType());
                sensor.setName(sensorStatus.getName());
                sensor = sensorRepository.save(sensor);
            }
            SensorData entity = new SensorData();
            entity.setSensorId(sensor.getId());
            entity.setValue(sensorStatus.getValue());
            entity.setTimestamp(dateTime);
            sensorDataRepository.save(entity);
        }
    }

    @PostMapping("/payload/image")
    public void postImage(@RequestParam String camera, @RequestBody byte[] data) {
        LOG.debug("Received image");
        try {
            Files.write(Paths.get(imageStorePath, camera + "-" + DATE_TIME_FORMAT.format(LocalDateTime.now()) + ".jpg"), data);
        } catch (IOException ex) {
            LOG.warn("Can't save image", ex);
        }
    }

}
