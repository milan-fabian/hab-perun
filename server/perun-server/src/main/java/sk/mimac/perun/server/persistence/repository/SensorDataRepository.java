package sk.mimac.perun.server.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sk.mimac.perun.server.persistence.entity.SensorData;

/**
 *
 * @author Mimac
 */
@Transactional
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    @Query(value = "SELECT * FROM sensor_data s1 JOIN "
            + "(SELECT sensor_id, MAX(timestamp) maxTimestamp FROM sensor_data GROUP BY sensor_id) s2 "
            + "ON s1.sensor_id = s2.sensor_id AND s1.timestamp = s2.maxTimestamp", nativeQuery = true)
    public List<SensorData> getLastData();

    @Query(value = "SELECT * FROM sensor_data s "
            + "WHERE s.sensor_id = :sensorId AND s.timestamp >= :from AND s.timestamp <= :to", nativeQuery = true)
    public List<SensorData> getHistory(@Param("sensorId") long sensorId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}
