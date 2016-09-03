package com.github.s207152.automation.input;

import android.content.Context;
import android.hardware.input.IInputManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import java.util.ArrayList;

/**
 * Created by s207152 on 1/7/2016.
 */
public class TouchInput {
    private static final String TAG = "TouchInput";

    private IInputManager iInputManager = IInputManager.Stub.asInterface(ServiceManager.getService("input"));
    private static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private static TouchInput mInstance = null;
    private Pointers pointers = new Pointers();
    private MotionEvent event = null;

    private TouchInput() {}

    public static TouchInput getInstance() {
        if(mInstance == null)
            mInstance = new TouchInput();

        return mInstance;
    }

    public boolean performActionDown(float x, float y, int id) {
        if(id < 0 || id > pointers.getSize()) {
            Log.e(TAG, "[performActionDown] id: " + id + " is invalid.");
            return false;
        }
        if(id < pointers.getSize()) {
            Log.e(TAG, "[performActionDown] id: " + id + " is already down.");
            return false;
        }

        pointers.addPointer(x, y, id);

        long when = SystemClock.uptimeMillis();
        event =
                MotionEvent.obtain(when, when, getPointerAction(MotionEvent.ACTION_DOWN, id),
                        pointers.getSize(), pointers.getPointerProperties(), pointers.getPointerCoords(),
                        0,  // metastate
                        0,  // buttonstate
                        1,  // xPrecision
                        1,  // yPrecision
                        0,  // deviceID
                        0,  // edgeFlags
                        InputDevice.SOURCE_TOUCHSCREEN, // source
                        0); // flags

        return injectMotionEvent(event);

        // Log.d(TAG, "[performActionDown] id: " + id + " at " + when + " on (" + x + ", " + y + ").");
    }

    public boolean performActionMove(float x, float y, int id) {
        if(id < 0 || id > pointers.getSize()) {
            Log.e(TAG, "[performActionMove] id: " + id + " is invalid");
            return false;
        }
        if(pointers.isPointerUp(id)) {
            Log.e(TAG, "[performActionMove] id: " + id + " is already up");
            return false;
        }

        pointers.updatePointer(x, y, id);

        long when = SystemClock.uptimeMillis();
        event =
                MotionEvent.obtain(when, when, MotionEvent.ACTION_MOVE,
                        pointers.getSize(), pointers.getPointerProperties(), pointers.getPointerCoords(),
                        0,  // metastate
                        0,  // buttonstate
                        1,  // xPrecision
                        1,  // yPrecision
                        0,  // deviceID
                        0,  // edgeFlags
                        InputDevice.SOURCE_TOUCHSCREEN, // source
                        0); // flags

        return injectMotionEvent(event);

        // Log.d(TAG, "[performActionMove] id: " + id + " at " + when + " on (" + x + ", " + y + ").");
    }

    public boolean performActionUp(float x, float y, int id) {
        if(id < 0 || id > pointers.getSize()) {
            Log.e(TAG, "[performActionUp] id: " + id + " is invalid.");
            return false;
        }
        if(pointers.isPointerUp(id)) {
            Log.e(TAG, "[performActionUp] id: " + id + " is already up.");
            return false;
        }

        boolean ret = false;

        pointers.updatePointer(x, y, id);

        int upPointerNum = pointers.getUpPointerNum();
        long when = SystemClock.uptimeMillis();
        event =
                MotionEvent.obtain(when, when, getPointerAction(MotionEvent.ACTION_UP, pointers.getSize() - upPointerNum - 1),  // pointer index should decrease from size to 0.
                        pointers.getSize(), pointers.getPointerProperties(), pointers.getPointerCoords(),
                        0,  // metastate
                        0,  // buttonstate
                        1,  // xPrecision
                        1,  // yPrecision
                        0,  // deviceID
                        0,  // edgeFlags
                        InputDevice.SOURCE_TOUCHSCREEN, // source
                        0); // flags

        ret = injectMotionEvent(event);

        pointers.upPointer(id);

        if(upPointerNum == pointers.getSize())
            pointers.clear();

        return ret;

        // Log.d(TAG, "[performActionUp] id: " + id + " at " + when + " on (" + x + ", " + y + ").");
    }

    private int getPointerAction(int motionEventAction, int index) {
        // Log.d(TAG, "[getPointerAction] index: " + index);
        return index == 0 ?
                motionEventAction : motionEventAction + 5 + (index << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
    }

    private boolean injectMotionEvent(MotionEvent event) {
        try {
            return iInputManager.injectInputEvent(event,
                    INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
        } catch (RemoteException ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private class Pointers {
        private ArrayList<PointerCoords> coords = new ArrayList<>();
        private ArrayList<PointerProperties> props = new ArrayList<>();
        private ArrayList<Boolean> downStatuses = new ArrayList<>();

        public int getSize() {
            return coords.size();
        }

        public int getUpPointerNum() {
            int num = 0;
            for(int i = 0; i < getSize(); i++) {
                if(isPointerUp(i))
                    num++;
            }
            return num;
        }

        public boolean isPointerUp(int id) {
            return !downStatuses.get(id);
        }

        public boolean isAllPointersUp() {
            for(int i = 0; i < getSize(); i++) {
                if(downStatuses.get(i))
                    return false;
            }
            return true;
        }

        public void upPointer(int id) {
            downStatuses.set(id, false);
        }

        public void addPointer(float x, float y, int id) {
            coords.add(id, getPointerCoord(x, y, id));
            props.add(id, getPointerProperty(id));
            downStatuses.add(id, true);
        }

        public void updatePointer(float x, float y, int id) {
            updatePointerCoord(x, y, id);
        }

        public void clear() {
            coords.clear();
            props.clear();
            downStatuses.clear();
        }

        public PointerCoords[] getPointerCoords() {
            return coords.toArray(new PointerCoords[getSize()]);
        }

        public PointerProperties[] getPointerProperties() {
            return props.toArray(new PointerProperties[getSize()]);
        }

        private PointerCoords getPointerCoord(float x, float y, int id) {
            PointerCoords p = new PointerCoords();
            p.x = x;
            p.y = y;
            p.pressure = 1;
            p.size = 1;

            return p;
        }

        private void updatePointerCoord(float x, float y, int id) {
            PointerCoords p = coords.get(id);
            p.x = x;
            p.y = y;

            coords.set(id, p);
        }

        private PointerProperties getPointerProperty(int id) {
            PointerProperties prop = new PointerProperties();
            prop.id = id;
            prop.toolType = MotionEvent.TOOL_TYPE_FINGER;

            return prop;
        }
    }

    // test functions.
//    public void testInput() {
//        sleep(5000);
//
//        Log.d(TAG, "start input");
//
//        TouchInput.getInstance().performActionDown(100, 300, 0);
//        TouchInput.getInstance().performActionDown(200, 300, 0);    // Error testing
//        TouchInput.getInstance().performActionDown(200, 300, 1);
//        TouchInput.getInstance().performActionDown(300, 300, 2);
//        sleep(500);
//
//        TouchInput.getInstance().performActionMove(100, 400, 0);
//        TouchInput.getInstance().performActionMove(200, 400, 1);
//        TouchInput.getInstance().performActionMove(300, 400, 2);
//        sleep(500);
//
//        TouchInput.getInstance().performActionUp(100, 500, 0);
//        TouchInput.getInstance().performActionUp(200, 500, 1);
//        TouchInput.getInstance().performActionMove(200, 400, 1);    // Error testing
//        TouchInput.getInstance().performActionUp(300, 500, 2);
//        TouchInput.getInstance().performActionUp(200, 400, 2);      // Error testing
//
//        Log.d(TAG, "finish input");
//    }
//
//    private void sleep(int time) {
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException ex) {
//            Log.e(TAG, ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
}
