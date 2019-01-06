package sk.mimac.perun.android.pipe;

import java.util.List;

import sk.mimac.perun.model.PayloadStatus;

public class PipeCommunicator {

    private static final String TAG = PipeCommunicator.class.getSimpleName();

    private static final int DATA_PORT = 9900;
    private static final int IMAGE_PORT = 9901;

    private final PipeDataCommunicator dataCommunicator = new PipeDataCommunicator(DATA_PORT);
    private final PipeImageCommunicator imageCommunicator = new PipeImageCommunicator(IMAGE_PORT);


    public void start() {
        new Thread(dataCommunicator, "PipeDataCommunicator").start();
        new Thread(imageCommunicator, "PipeImageCommunicator").start();
    }

    public List<PayloadStatus.SensorStatus> getLastStatuses() {
        return dataCommunicator.getReceivedStatuses();
    }

    public void setLastStatuses(List<PayloadStatus.SensorStatus> lastStatuses) {
        dataCommunicator.setSendStatuses(lastStatuses);
    }

    public void stop() {
        dataCommunicator.stop();
        imageCommunicator.stop();
    }

}
