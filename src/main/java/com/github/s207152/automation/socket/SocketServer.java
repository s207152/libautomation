package com.github.s207152.automation.socket;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by s207152 on 4/7/2016.
 */
public class SocketServer implements Runnable {

    private static final String TAG = "SocketServer";

    private LocalServerSocket mServer;
    private boolean mRunning = true;

    Handler mHandler;

    public SocketServer(String name, Handler handler) {
        try {
            mServer = new LocalServerSocket(name);
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        Log.d(TAG, "Server created.");
        mHandler = handler;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void stop() {
        mRunning = false;
        Log.d(TAG, "stopped.");
    }

    @Override
    public void run() {
        try {
            while(mRunning) {
                Log.d(TAG, "waiting for accept");
                LocalSocket receiver = mServer.accept();
                Log.d(TAG, "accepted");

                BufferedReader reader = new BufferedReader(new InputStreamReader(receiver.getInputStream()));

                String line;
                StringBuilder message = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    message.append(line);
                }
                handleMessage(message.toString());

                reader.close();
                receiver.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                mServer.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }

    }

    private void handleMessage(String message) {
        Log.d(TAG, "received from client: " + message);
        if(message.equals("stop"))
            stop();
        else
            mHandler.sendMessage(Message.obtain(mHandler, 0, message));
    }
}
