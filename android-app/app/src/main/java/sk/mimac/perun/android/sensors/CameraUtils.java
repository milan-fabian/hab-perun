package sk.mimac.perun.android.sensors;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import sk.mimac.perun.android.webclient.ServiceConnector;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");

    public static void getPicture(boolean highres) {
        Log.i(TAG, "Opening camera");
        try {
            Camera camera = Camera.open();
            if (camera == null) {
                Log.w(TAG, "No camera found");
                return;
            }
            prepareCamera(camera, highres);
            Log.i(TAG, "Taking picture from camera");
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        Log.i(TAG, "Picture taken, size=" + data.length);
                        File dir = new File(Environment.getExternalStorageDirectory(), "hab-perun/images");
                        dir.mkdirs();
                        try (OutputStream stream = new FileOutputStream(new File(dir, DATE_FORMAT.format(new Date()) + ".jpg"))) {
                            stream.write(data);
                        }
                        if(!highres) {
                            new Thread(() -> ServiceConnector.sendImage("gn4", data)).start();
                        }
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

    private static void prepareCamera(Camera camera, boolean highres) throws IOException {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        camera.setPreviewTexture(new SurfaceTexture(0));
        camera.startPreview();

        Camera.Parameters params = camera.getParameters();
        if (highres) {
            Camera.Size size = getMaxSize(params.getSupportedPictureSizes());
            params.setPictureSize(size.width, size.height);
        } else {
            params.setJpegQuality(85);
        }
        Log.i(TAG, "Params: " + params.flatten());
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        params.setExposureCompensation(1);
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

