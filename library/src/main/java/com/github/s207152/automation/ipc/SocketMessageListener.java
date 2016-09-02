package com.github.s207152.automation.ipc;

import org.zeromq.ZMQ;

/**
 * Created by s207152 on 7/7/2016.
 */
public interface SocketMessageListener {
    public void onSocketReceived(ZMQ.Socket socket, String msg);
}
