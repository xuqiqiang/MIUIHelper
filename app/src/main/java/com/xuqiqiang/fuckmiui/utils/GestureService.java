package com.xuqiqiang.fuckmiui.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.RequiresApi;

public class GestureService extends AccessibilityService {
    private static final String TAG = "GestureService";
    private static int mX;
    private static int mY;
    private static GestureDescription.StrokeDescription sd;
    private static GestureService mInstance;

    public static boolean isRunning() {
        return mInstance != null;
    }

    public static AccessibilityNodeInfo getRootNode() {
        if (!isRunning()) return null;
        return mInstance.getRootInActiveWindow();
    }

    public static boolean performAction(int action) {
        if (!isRunning()) return false;
        return mInstance.performGlobalAction(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void click(AccessibilityNodeInfo nodeInfo) {
        boolean ret = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (!ret) {
            Rect bounds = new Rect();
            nodeInfo.getBoundsInScreen(bounds);
            L.d("_test_ bounds", bounds);
            move(MotionEvent.ACTION_DOWN, (bounds.left + bounds.right) / 2,
                (bounds.top + bounds.bottom) / 2);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                move(MotionEvent.ACTION_UP, (bounds.left + bounds.right) / 2,
                    (bounds.top + bounds.bottom) / 2);
            }, 300);
        }
    }

    public static boolean isClickable(AccessibilityNodeInfo nodeInfo) {
//        if (nodeInfo.isClickable()) return true;
        Rect bounds = new Rect();
        nodeInfo.getBoundsInScreen(bounds);
        return bounds.left < bounds.right && bounds.top < bounds.bottom;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void move(int action, int x, int y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        if (GestureService.mInstance == null) return;

        Path path = new Path();
        try {
            if (action == MotionEvent.ACTION_DOWN) {
                if (y > 0) {
                    path.moveTo(x, y - 1);
                } else {
                    path.moveTo(x, y + 1);
                }
                path.lineTo(x, y);
                mX = x;
                mY = y;
                sd = new GestureDescription.StrokeDescription(path, 0, 1, true);
            } else if (action == MotionEvent.ACTION_MOVE) {
                path.moveTo(mX, mY);
                if (mX == x && mY == y) {
                    if (y > 0) {
                        path.lineTo(x, y - 1);
                    } else {
                        path.lineTo(x, y + 1);
                    }
                }
                path.lineTo(x, y);
                mX = x;
                mY = y;
                sd = sd.continueStroke(path, 0, 1, true);
            } else if (action == MotionEvent.ACTION_UP) {
                path.moveTo(mX, mY);
                if (mX == x && mY == y) {
                    if (y > 0) {
                        path.lineTo(x, y - 1);
                    } else {
                        path.lineTo(x, y + 1);
                    }
                }
                path.lineTo(x, y);
                mX = x;
                mY = y;
                sd = sd.continueStroke(path, 0, 1, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GestureDescription.Builder builder = new GestureDescription.Builder().addStroke(sd);
        GestureDescription gesture = builder.build();
        GestureService.mInstance.dispatchGesture(
            gesture,
            new AccessibilityService.GestureResultCallback() {

                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    L.d(TAG, "action=onCompleted");
                }

                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    L.e(TAG, "action=onCancelled");
                }
            },
            null
        );
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        L.d(TAG, "onServiceConnected");
        mInstance = this;
    }

    @Override
    public void onInterrupt() {
        L.d(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
        L.d(TAG, "onDestroy");
    }
}
