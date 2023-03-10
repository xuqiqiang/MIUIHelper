package com.xuqiqiang.fuckmiui.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.xuqiqiang.fuckmiui.FuckSettings;
import com.xuqiqiang.fuckmiui.R;
import moe.shizuku.manager.adb.AdbClient;
import moe.shizuku.manager.adb.AdbKey;
import moe.shizuku.manager.adb.PreferenceAdbKeyStore;
import moe.shizuku.manager.starter.Starter;
import rikka.shizuku.Shizuku;

import static com.xuqiqiang.fuckmiui.utils.ViewUtils.progressDialog;

public class ServiceHelper {
    private static final String TAG = "AdbHelper";
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog mDialog;
    private Callback mCallback;
    private String mInfo = "";

    public ServiceHelper(Context context) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void start(int port) {
        mHandler.post(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
            }
            mDialog = progressDialog(mContext, R.string.waiting, R.string.service_loading, false);
        });
        new Thread() {

            public void run() {
                try {
                    String host = "127.0.0.1";
                    AdbKey key;
                    try {
                        key = new AdbKey(new PreferenceAdbKeyStore(FuckSettings.getPreferences()),
                            "shizuku");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    try (AdbClient adbClient = new AdbClient(host, port, key)) {
                        adbClient.connect();
                        adbClient.shellCommand(Starter.INSTANCE.getSdcardCommand(),
                            bytes -> {
                                String data = new String(bytes);
                                mInfo += data;
                                return null;
                            });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } finally {
                    L.d(TAG, "info:" + mInfo);
                    if (mInfo.contains("info: shizuku_starter exit with 0")) {
                        L.d(TAG, "Waiting for service...");
                        Shizuku.addBinderReceivedListenerSticky(
                            new Shizuku.OnBinderReceivedListener() {
                                @Override public void onBinderReceived() {
                                    Shizuku.removeBinderReceivedListener(this);
                                    L.d(TAG,
                                        "Service started, this window will be automatically closed in 3 seconds");
                                    mHandler.post(() -> {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.cancel();
                                        }
                                        if (mCallback != null) {
                                            mCallback.onResult(true);
                                        }
                                    });
                                }
                            });
                    } else {
                        mHandler.post(() -> {
                            if (mDialog != null && mDialog.isShowing()) {
                                mDialog.cancel();
                            }
                            if (mCallback != null) {
                                mCallback.onResult(false);
                            }
                        });
                    }
                }
            }
        }.start();
    }

    public void stop() {
        mCallback = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
            }
        });
    }

    public interface Callback {
        void onResult(boolean started);
    }
}