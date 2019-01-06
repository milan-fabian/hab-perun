package sk.mimac.perun.server.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.mimac.perun.server.model.ImagesModel;
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

    @Value("${perun.image.store}")
    private String imageStorePath;

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

    @GetMapping(value = "/image/file/{file}", produces = "image/jpeg")
    public Resource getImageFile(@PathVariable("file") String file) {
        return new FileSystemResource(new File(imageStorePath, file));
    }

    @GetMapping("/image/statistics")
    public List<ImagesModel> getImageStatistics() {
        File[] files = new File(imageStorePath).listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, File> mostRecentMap = new HashMap<>();
        for (File file : files) {
            String fileName = file.getName();
            String camera = fileName.substring(0, fileName.indexOf('-'));
            Integer count = countMap.get(camera);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            countMap.put(camera, count);
            File mostRecent = mostRecentMap.get(camera);
            if (mostRecent == null || file.lastModified() > mostRecent.lastModified()) {
                mostRecentMap.put(camera, file);
            }
        }
        List<ImagesModel> result = new ArrayList<>(countMap.size());
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            File mostRecent = mostRecentMap.get(entry.getKey());
            result.add(new ImagesModel(entry.getKey(), entry.getValue(), mostRecent.lastModified(), mostRecent.getName(), mostRecent.length()));
        }
        return result;
    }

}
