package sk.mimac.perun.server.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Mimac
 */
@Entity
@Table(name = "sensor_data")
public class SensorData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime time;

    @Column(name = "sensor_id", nullable = false)
    private long sensor;

    @Column(name = "value", nullable = false)
    private float val;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public long getSensor() {
        return sensor;
    }

    public void setSensor(long sensor) {
        this.sensor = sensor;
    }

}
