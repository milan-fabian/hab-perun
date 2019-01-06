package sk.mimac.perun.model;

import java.util.List;

public class PayloadStatus {

    private List<SensorStatus> sensors;

    public List<SensorStatus> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorStatus> sensors) {
        this.sensors = sensors;
    }

    public static class SensorStatus {

        private SensorType type;
        private String name;
        private float value;
        private long timestamp;

        public SensorStatus() {
        }

        public SensorStatus(long timestamp, SensorType type, String name, float value) {
            this.timestamp = timestamp;
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public SensorType getType() {
            return type;
        }

        public void setType(SensorType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
