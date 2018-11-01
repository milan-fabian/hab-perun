package sk.mimac.perun.server.persistence.repository;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import sk.mimac.perun.model.SensorType;
import sk.mimac.perun.server.persistence.entity.Sensor;

/**
 *
 * @author Mimac
 */
@Transactional
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    
    public Sensor findByNameAndType(String name, SensorType type);
    
}
