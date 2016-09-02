package com.github.s207152.automation;

import android.content.Context;
import android.util.Log;

import com.github.s207152.automation.ipc.JeroMQClient;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.StreamGobbler;

/**
 * Created by s207152 on 6/7/2016.
 */
public class AutomationManager {

    private static final String TAG = "AutomationManager";

    public static final int CODE_TOUCH_DOWN = 0;
    public static final int CODE_TOUCH_MOVE = 1;
    public static final int CODE_TOUCH_UP = 2;

    // export CLASSPATH=/path/to/your/apk &&
    // app_process /your/app/files/path com.github.s207152.automation.Daemon
    private static String startCommand;

    private Shell.Interactive mSession;

    private JeroMQClient mClient;

    public AutomationManager(Context context) {
        mClient = new JeroMQClient();
        startCommand = "export CLASSPATH=" + context.getApplicationInfo().sourceDir + " && " +
                "app_process " +
                context.getFilesDir().getAbsolutePath() + " " +
                Daemon.class.getName()
                + " &"   // Run in background
                ;
        Log.d(TAG, "startCommand: " + startCommand);
    }

    public void startDaemon() {
//        if(Shell.SU.available())
            startDaemonAsRoot();
//        else
//            startDaemonAsADB();
    }

    public void closeSession() {
        stopServer();
//        mSession.kill();
        mSession.close();
    }

    private void startDaemonAsRoot() {
        mSession = new Shell.Builder()
                .useSU()
                .setWantSTDERR(true)
                .setOnSTDOUTLineListener(new StreamGobbler.OnLineListener() {
                    @Override
                    public void onLine(String line) {
                        Log.d(TAG, "onLine: " + line);
                    }
                })
//                .addEnvironment("CLASSPATH", mContext.getApplicationInfo().sourceDir)
                .open(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                            Log.e(TAG, "Error opening root shell: exitCode " + exitCode);
                        } else {
                            sendStartCommand();
                        }
                    }
                });
//        List<String> results = Shell.SU.run(startCommand);
        Log.d(TAG, "start daemon as root.");
//        for (String result : results) {
//            Log.d(TAG, result);
//        }
    }

    private void startDaemonAsADB() {

    }

    private void sendStartCommand() {
        mSession.addCommand(startCommand, 0, new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                if (exitCode < 0) {
                    Log.e(TAG, "Error executing commands: exitCode " + exitCode);
                } else {
                    Log.d(TAG, "startcommand onResult: " + commandCode + ", " + exitCode + ", " + output);
                    for (String result: output) {
                        Log.d(TAG, "command output: " + result);
                    }
                }
            }
        });
    }

    private String sendToServer(String msg) {
        return mClient.send(msg);
    }

    private void stopServer() {
        mClient.stopServer();
    }

    public String touchDown(float x, float y, int id) {
        return sendToServer(CODE_TOUCH_DOWN + " " + x + " " + y + " " + id);
    }

    public String touchMove(float x, float y, int id) {
        return sendToServer(CODE_TOUCH_MOVE + " " + x + " " + y + " " + id);
    }

    public String touchUp(float x, float y, int id) {
        return sendToServer(CODE_TOUCH_UP + " " + x + " " + y + " " + id);
    }
}
