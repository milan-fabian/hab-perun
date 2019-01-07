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
        private float val;
        private long time;

        public SensorStatus() {
        }

        public SensorStatus(long time, SensorType type, String name, float val) {
            this.time = time;
            this.type = type;
            this.name = name;
            this.val = val;
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

        public float getVal() {
            return val;
        }

        public void setVal(float val) {
            this.val = val;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}
