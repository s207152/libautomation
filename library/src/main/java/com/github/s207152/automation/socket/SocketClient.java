package com.github.s207152.automation.socket;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by s207152 on 4/7/2016.
 */
public class SocketClient implements Runnable {

    private static final String TAG = "SocketClient";

    LocalSocket mClient = new LocalSocket();
    String mMessage;

    public SocketClient(String name, String message) {
        try {
            mClient.connect(new LocalSocketAddress(name));
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        mMessage = message;
    }

    @Override
    public void run() {
        try {
            OutputStream output = mClient.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(mMessage);
            writer.close();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                mClient.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }

    }
}
