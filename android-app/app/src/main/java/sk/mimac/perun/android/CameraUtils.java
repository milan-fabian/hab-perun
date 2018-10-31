package sk.mimac.perun.android;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import sk.mimac.perun.android.service.ServiceConnector;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();

    public static void getPicture() {
        Log.i(TAG, "Opening camera");
        Camera camera = Camera.open();
        if (camera == null) {
            Log.w(TAG, "No camera found");
            return;
        }
        try {
            prepareCamera(camera);
            Log.i(TAG, "Taking picture from camera");
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        Log.i(TAG, "Picture taken, size=" + data.length);
                        try (OutputStream stream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/image" + System.currentTimeMillis() + ".jpg")) {
                            stream.write(data);
                        }
                        new Thread(() -> ServiceConnector.sendImage(data)).start();
                    } catch (Exception ex) {
                        Log.w(TAG, "Can't get image from camera", ex);
                    } finally {
                        camera.release();
                    }
                }
            });
        } catch (Exception ex) {
            Log.w(TAG, "Can't get image from camera", ex);
        }
    }

    private static void prepareCamera(Camera camera) throws IOException {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        camera.setPreviewTexture(new SurfaceTexture(0));
        camera.startPreview();

        Camera.Parameters params = camera.getParameters();
        Camera.Size size = getMaxSize(params.getSupportedPictureSizes());
        params.setPictureSize(size.width, size.height);
        camera.setParameters(params);
    }

    private static Camera.Size getMaxSize(List<Camera.Size> sizes) {
        Camera.Size maxSize = sizes.get(0);
        for (Camera.Size size : sizes) {
            if (size.height >= maxSize.height && size.width >= maxSize.width) {
                maxSize = size;
            }
        }
        return maxSize;
    }
}

