package sk.mimac.perun.server.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.mimac.perun.server.persistence.entity.Sensor;
import sk.mimac.perun.server.persistence.entity.SensorData;
import sk.mimac.perun.server.persistence.repository.SensorDataRepository;
import sk.mimac.perun.server.persistence.repository.SensorRepository;

/**
 *
 * @author Mimac
 */
@RestController
public class Controller {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @GetMapping("/data/last")
    public List<SensorData> getLastData() {
        return sensorDataRepository.getLastData();
    }

    @GetMapping("/sensor/list")
    public List<Sensor> getSensorList() {
        return sensorRepository.findAll();
    }

    @GetMapping("/sensor/history")
    public List<SensorData> getSensorList(@RequestParam long sensorId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate from, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate to) {
        return sensorDataRepository.getHistory(sensorId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @GetMapping("/sensor")
    public Sensor getSensorList(@RequestParam long sensorId) {
        return sensorRepository.getOne(sensorId);
    }

}
