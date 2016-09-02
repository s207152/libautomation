package com.github.s207152.automation;

import android.util.Log;

import com.github.s207152.automation.input.TouchInput;
import com.github.s207152.automation.ipc.JeroMQServer;
import com.github.s207152.automation.ipc.SocketMessageListener;

import org.zeromq.ZMQ;

/**
 * Created by s207152 on 29/6/2016.
 */
public class Daemon implements SocketMessageListener {
    public static final boolean DEBUG = true;
    private static final String TAG = "Daemon";

    private JeroMQServer mJeroMQServer;

    public static void main(String[] args) {
        new Daemon().run();
    }

    private void run() {
        registerShutDownHook();

        Log.d(TAG, "Daemon starting up");

        startServer();

//        TouchInput.getInstance().testInput();
//        new Screencap().surfaceControlScreencap();
    }

    /**
     * Handle SIGTERM from app.
     */
    private void registerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mJeroMQServer.stop();
            }
        });
    }

    private void startServer() {
        mJeroMQServer = new JeroMQServer(this);
        mJeroMQServer.run();
    }

    @Override
    public void onSocketReceived(ZMQ.Socket socket, String msg) {
        String result = "";

        String[] args = msg.split(" ");
        switch (Integer.parseInt(args[0])) {
            case AutomationManager.CODE_TOUCH_DOWN:
                result = Boolean.valueOf(TouchInput.getInstance().performActionDown(
                        Float.parseFloat(args[1]),
                        Float.parseFloat(args[2]),
                        Integer.parseInt(args[3])
                )).toString();
                break;
            case AutomationManager.CODE_TOUCH_MOVE:
                result = Boolean.valueOf(TouchInput.getInstance().performActionDown(
                        Float.parseFloat(args[1]),
                        Float.parseFloat(args[2]),
                        Integer.parseInt(args[3])
                )).toString();
                break;
            case AutomationManager.CODE_TOUCH_UP:
                result = Boolean.valueOf(TouchInput.getInstance().performActionUp(
                        Float.parseFloat(args[1]),
                        Float.parseFloat(args[2]),
                        Integer.parseInt(args[3])
                )).toString();
                break;
        }

        socket.send(result.getBytes(ZMQ.CHARSET), 0);
    }
}
