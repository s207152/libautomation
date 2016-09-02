package com.github.s207152.automation.screen;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceControl;

import com.github.s207152.automation.TimingLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by s207152 on 5/7/2016.
 */
public class Screencap {

    private static final String TAG = "Screencap";

    public Screencap() {}

    public void surfaceControlScreencap() {
        TimingLogger timings = new TimingLogger(TAG, "surfaceControlScreencap");

        Bitmap bitmap = SurfaceControl.screenshot(0, 0);

        timings.addSplit("screenshot");

        String file_path = "/data/local/tmp" +
                "/screenshot";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "screenshot" + SystemClock.uptimeMillis() + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }

        timings.addSplit("create file");

        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        timings.addSplit("write bitmap");
        timings.dumpToLog();
    }
}
