package sk.mimac.perun.server.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

}
