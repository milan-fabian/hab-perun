package sk.mimac.perun.android.pipe;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import sk.mimac.perun.android.webclient.ServiceConnector;

public class PipeImageCommunicator implements Runnable {

    private static final String TAG = PipeImageCommunicator.class.getSimpleName();

    private final int pipePort;

    private ServerSocket serverSocket;
    private boolean shouldRun = true;

    public PipeImageCommunicator(int pipePort) {
        this.pipePort = pipePort;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(pipePort);
            while (shouldRun) {
                byte[] data = null;
                try (Socket socket = serverSocket.accept();
                     InputStream input = socket.getInputStream()) {
                    Log.d(TAG, "Got socket connection");

                    data = IOUtils.toByteArray(input);
                } catch (Exception ex) {
                    Log.e(TAG, "Can't read/write data", ex);
                }
                // Send after the connection is closed, in order not to bloc RPi
                if (data != null) {
                    ServiceConnector.sendImage("rpi", data);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Unexpected exception", ex);
        }
    }


    public void stop() {
        shouldRun = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
        }
    }


}
