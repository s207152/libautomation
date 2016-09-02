package com.github.s207152.automation.ipc;

import android.util.Log;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import com.github.s207152.automation.ipc.SocketMessageListener;


public class JeroMQServer {

    private static final String TAG = "JeroMQServer";

    private SocketMessageListener mMessageListener;
    private int mPort = 5500;
    private ZMQ.Context mContext;
    private ZMQ.Socket mSocket;

    private volatile boolean isCancel = false;

    public JeroMQServer(SocketMessageListener messageListener) {
        this(5500, messageListener);
    }

    public JeroMQServer(int port, SocketMessageListener messageListener) {
        mPort = port;

        bindSocket(mPort);

        mMessageListener = messageListener;
    }

    private void bindSocket(int port) {
        mContext = ZMQ.context(1);
        mSocket = mContext.socket(ZMQ.REP);

        try {
            mSocket.bind ("tcp://localhost:" + port);
        } catch (ZMQException ex) {
            Log.e(TAG, ex.getMessage(), ex);

            // Try to send stop to the zombie server
            mSocket.connect("tcp://localhost:" + port);
            mSocket.send("stop".getBytes(ZMQ.CHARSET), 0);
            bindSocket(port);
        } catch (RuntimeException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    public int getPort() {
        return mPort;
    }

    public void run() {
        while(!isCancel) {
            byte[] reply = mSocket.recv(0);
            String msg = new String(reply, ZMQ.CHARSET);
//            Log.d(TAG, "request from client: |" + msg + "|");
            if(msg.equals("stop")) {
                Log.d(TAG, "stop now");
                stop();
                break;
            }

            if (mMessageListener != null)
                mMessageListener.onSocketReceived(mSocket, msg);
        }

        close();
    }

    public void stop() {
        isCancel = true;
    }

    public void close() {
        mSocket.close();
        mContext.term();
    }
}
