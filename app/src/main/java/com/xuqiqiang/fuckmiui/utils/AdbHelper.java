package com.xuqiqiang.fuckmiui.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import com.xuqiqiang.fuckmiui.R;
import moe.shizuku.manager.adb.AdbMdns;

import static com.xuqiqiang.fuckmiui.utils.ViewUtils.progressDialog;

@RequiresApi(api = Build.VERSION_CODES.R)
public class AdbHelper {
    private static final String TAG = "AdbHelper";
    private final Context mContext;
    private AdbMdns adbMdns;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog mDialog;
    private Callback mCallback;

    public AdbHelper(Context context) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void start() {
        adbMdns = new AdbMdns(mContext, AdbMdns.TLS_CONNECT, port -> {
            L.d(TAG, "Pairing service port: port", port);
            if (port < 0 || port > 65535) return;
            mHandler.removeCallbacksAndMessages(null);
            stop();
            if (mCallback != null) {
                mCallback.onConnected(port);
            }
        });
        adbMdns.start();

        mHandler.post(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
            }
            mDialog = progressDialog(mContext, R.string.waiting, R.string.adb_connection, true);
        });
        mHandler.postDelayed(() -> {
            stop();
            if (mCallback != null) {
                mCallback.onConnected(-1);
            }
        }, 5000);
    }

    public void stop() {
        if (adbMdns != null) {
            adbMdns.stop();
            adbMdns = null;
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler.post(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
            }
        });
    }

    public interface Callback {
        void onConnected(int port);
    }
}