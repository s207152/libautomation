package com.github.s207152.automation.ipc;

import android.util.Log;

import org.zeromq.ZMQ;

/**
 * Created by s207152 on 6/7/2016.
 */
public class JeroMQClient {

    private static final String TAG = "JeroMQClient";

    private int mPort = 5500;
    private ZMQ.Context mContext;
    private ZMQ.Socket mSocket;

    public JeroMQClient() {
        this(5500);
    }

    public JeroMQClient(JeroMQServer server) {
        this(server.getPort());
    }

    public JeroMQClient(int port) {
        mPort = port;

        mContext = ZMQ.context(1);
        mSocket = mContext.socket(ZMQ.REQ);

        mSocket.connect ("tcp://localhost:" + mPort);
    }

    public String send(final String request) {
        mSocket.send(request.getBytes(ZMQ.CHARSET), 0);
//        Log.d(TAG, "request sent to server: " + request);

        byte[] feedback = mSocket.recv(0);
        String msg = new String(feedback);
//        Log.d(TAG, "feedback from server:" + msg);
        return msg;
    }

    public void stopServer() {
        mSocket.send("stop".getBytes(ZMQ.CHARSET), 0);
    }
}
