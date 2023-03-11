package com.xuqiqiang.fuckmiui.utils;

import android.accessibilityservice.AccessibilityService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.xuqiqiang.fuckmiui.R;
import com.xuqiqiang.fuckmiui.FuckSettings;
import com.xuqiqiang.fuckmiui.home.HomeActivity;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import moe.shizuku.manager.adb.AdbKey;
import moe.shizuku.manager.adb.AdbMdns;
import moe.shizuku.manager.adb.AdbPairingClient;
import moe.shizuku.manager.adb.PreferenceAdbKeyStore;
import rikka.shizuku.Shizuku;

import static com.xuqiqiang.fuckmiui.utils.ViewUtils.dialog;
import static com.xuqiqiang.fuckmiui.utils.ViewUtils.progressDialog;

@RequiresApi(api = Build.VERSION_CODES.R)
public class AutoHelper {
    private static final String TAG = "AutoHelper";

    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final AdbMdns adbMdns;
    private volatile String mPairCode;
    private volatile boolean isRunning;
    private AdbHelper mAdbHelper;
    private ServiceHelper mServiceHelper;

    public AutoHelper(Context context) {
        mContext = context;
        adbMdns = new AdbMdns(context, AdbMdns.TLS_PAIRING, port -> {
            L.d(TAG, "Pairing service port: port", port, mPairCode);
            synchronized (this) {
                if (port < 0 || port > 65535) return;
                if (FuckSettings.getPreferences().getInt("pairPort", -1) >= 0) return;
                FuckSettings.getPreferences().edit().putInt("pairPort", port).apply();
                checkPair();
            }
        });
        adbMdns.start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void runAuto() {
        isRunning = true;
        FuckSettings.getPreferences().edit().remove("pairCode").apply();
        FuckSettings.getPreferences().edit().remove("pairPort").apply();

        SystemUtils.jumpToDevelopmentSettings(mContext);

        postDelayed(() -> {
            enterAdbWireless(() -> enableDebug(() -> postDelayed(() -> {
                List<AccessibilityNodeInfo> nodeInfos3 =
                    findNodeByText(mContext.getString(R.string.pairing_with_code));
                if (!ArrayUtils.isEmpty(nodeInfos3)) {
                    GestureService.click(nodeInfos3.get(0));

                    postDelayed(() -> {
                        List<AccessibilityNodeInfo> nodeInfos4 = findNodeByViewId(
                            "com.android.settings:id/pairing_code");
                        if (!ArrayUtils.isEmpty(nodeInfos4)) {
                            String code = nodeInfos4.get(0).getText().toString();
                            L.d(TAG, "pairing_code", code);
                            if (!TextUtils.isEmpty(code)) {
                                mPairCode = code + "";
                                FuckSettings.getPreferences()
                                    .edit()
                                    .putString("pairCode", mPairCode)
                                    .apply();
                                checkPair();
                            }
                        }
                    }, delay(1000));
                }
            }, delay(1000))));
        }, delay(1000));
    }

    public void stopRunAuto() {
        if (mAdbHelper != null) {
            mAdbHelper.stop();
        }
        if (mServiceHelper != null) {
            mServiceHelper.stop();
        }
        mHandler.post(() -> {
            adbMdns.stop();
            if (isRunning) {
                Toast.makeText(mContext, R.string.execution_end, Toast.LENGTH_LONG).show();
            }
            isRunning = false;
            ((HomeActivity) mContext).checkApkStatus();
        });
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        if (r == null) return false;
        return mHandler.postDelayed(() -> {
            if (!isRunning) {
                return;
            }
            r.run();
        }, delayMillis);
    }

    private void enableDebug(Runnable runnable) {
        List<AccessibilityNodeInfo> nodeInfos = findNodeByViewId("android:id/switch_widget");
        L.d(TAG, "enableDebug 1", ArrayUtils.size(nodeInfos));
        if (!ArrayUtils.isEmpty(nodeInfos)) {
            L.d(TAG, "enableDebug 2", nodeInfos.get(0).isChecked());
            if (!nodeInfos.get(0).isChecked()) {
                GestureService.click(nodeInfos.get(0));
                postDelayed(() -> {
                    List<AccessibilityNodeInfo> nodeInfos1 =
                        findNodeByText(mContext.getString(android.R.string.ok));
                    L.d(TAG, "enableDebug 3", ArrayUtils.size(nodeInfos1));
                    if (!ArrayUtils.isEmpty(nodeInfos1)) {
                        GestureService.click(nodeInfos1.get(0));
                        postDelayed(() -> enableDebug(runnable), delay(1000));
                    } else {
                        runnable.run();
                    }
                }, delay(1000));
            } else {
                runnable.run();
            }
        }
    }

    private void disableDebug(Runnable runnable) {
        List<AccessibilityNodeInfo> nodeInfos = findNodeByViewId("android:id/switch_widget");
        L.d(TAG, "disableDebug", ArrayUtils.size(nodeInfos));
        if (!ArrayUtils.isEmpty(nodeInfos)) {
            L.d(TAG, "disableDebug isChecked", nodeInfos.get(0).isChecked());
            if (nodeInfos.get(0).isChecked()) {
                GestureService.click(nodeInfos.get(0));
                if (runnable != null) {
                    postDelayed(runnable, delay(1000));
                }
            } else {
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    private void enterAdbWireless(Runnable runnable) {
        enterAdbWireless(runnable, 3);
    }

    private void enterAdbWireless(Runnable runnable, int retryTimes) {
        List<AccessibilityNodeInfo> nodeInfos =
            findNodeByViewId("com.android.settings:id/recycler_view");
        L.d(TAG, "enterAdbWireless 1", ArrayUtils.size(nodeInfos));
        if (!ArrayUtils.isEmpty(nodeInfos)) {
            List<AccessibilityNodeInfo> nodeInfos1 =
                findNodeByText(mContext.getString(R.string.wireless_debugging));
            if (ArrayUtils.isEmpty(nodeInfos1) || !GestureService.isClickable(nodeInfos1.get(0))) {
                if (retryTimes < 0) {
                    stopRunAuto();
                    return;
                }
                nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                postDelayed(() -> enterAdbWireless(runnable, retryTimes - 1), delay(1000));
            } else {
                GestureService.click(nodeInfos1.get(0));
                postDelayed(runnable, delay(1000));
            }
        }
    }

    private void jumpToHome(Runnable runnable) {
        GestureService.performAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        postDelayed(() -> {
            List<AccessibilityNodeInfo> nodeInfos =
                findNodeByText(mContext.getString(R.string.app_name));
            L.d(TAG, "jumpToHome", ArrayUtils.size(nodeInfos));
            if (!ArrayUtils.isEmpty(nodeInfos)) {
                GestureService.click(nodeInfos.get(0));
                postDelayed(runnable, delay(600));
            } else {
                stopRunAuto();
            }
        }, delay(1000));
    }

    private void setupService(Runnable runnable) {
        setupService(runnable, 3);
    }

    private void setupService(Runnable runnable, int retryTimes) {
        if (mAdbHelper != null) {
            mAdbHelper.stop();
        }
        mAdbHelper = new AdbHelper(mContext);
        mAdbHelper.setCallback(port -> {
            if (port >= 0) {
                checkServiceStart(port, runnable);
            } else {
                if (retryTimes <= 0) {
                    stopRunAuto();
                    dialog(mContext, R.string.some_issue, R.string.reopen_wifi, null, false);
                    return;
                }
                setupService(runnable, retryTimes - 1);
            }
        });
        mAdbHelper.start();
    }

    private void checkServiceStart(int port, Runnable runnable) {
        if (mServiceHelper != null) {
            mServiceHelper.stop();
        }
        mServiceHelper = new ServiceHelper(mContext);
        mServiceHelper.setCallback(started -> {
            if (started) {
                runnable.run();
            } else {
                stopRunAuto();
            }
        });
        mServiceHelper.start(port);
    }

    private void installApk() {
        ProgressDialog dialog =
            progressDialog(mContext, R.string.waiting, R.string.installing, false);
        new Thread() {
            public void run() {
                AdbShell shell = new AdbShell();
                int sessionId = shell.createSession();
                L.d(TAG, "installApk sessionId", sessionId);
                if (sessionId == -1) {
                    mHandler.post(() -> {
                        dialog.cancel();
                        Toast.makeText(mContext, R.string.installation_error, Toast.LENGTH_LONG)
                            .show();
                        if (Shizuku.pingBinder()) {
                            try {
                                Shizuku.exit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        SystemUtils.jumpToDevelopmentSettings(mContext);
                        postDelayed(() -> enterAdbWireless(
                                () -> disableDebug(() -> jumpToHome(() -> stopRunAuto()))),
                            delay(1000));
                    });
                    return;
                }
                int currentApkFile = 0;
                ApkHelper apkHelper = new ApkHelper(mContext);
                File file = new File(apkHelper.getApkPath());
                L.d(TAG, "installApk file", file.length());
                try {
                    Shell.Result result = shell.exec(
                        new Shell.Command("pm", "install-write", "-S",
                            String.valueOf(file.length()),
                            String.valueOf(sessionId), String.format("%d.apk", currentApkFile++)),
                        new FileInputStream(file));

                    L.d(TAG, "installApk result", result.isSuccessful());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Shell.Result installationResult =
                    shell.exec(
                        new Shell.Command("pm", "install-commit", String.valueOf(sessionId)));
                L.d(TAG, "installApk installationResult", installationResult.isSuccessful());
                mHandler.post(() -> {
                    dialog.cancel();
                    if (installationResult.isSuccessful()) {
                        Toast.makeText(mContext, R.string.installation_completed, Toast.LENGTH_LONG)
                            .show();
                    } else {
                        Toast.makeText(mContext, R.string.installation_error, Toast.LENGTH_LONG)
                            .show();
                    }

                    if (Shizuku.pingBinder()) {
                        try {
                            Shizuku.exit();
                        } catch (Exception ignored) {
                        }
                    }

                    SystemUtils.jumpToDevelopmentSettings(mContext);
                    postDelayed(() -> enterAdbWireless(() -> disableDebug(() -> jumpToHome(() -> {
                        stopRunAuto();
                        SystemUtils.jumpToAntiFlicker(mContext);
                    }))), delay(1000));
                });
            }
        }.start();
    }

    private void pair(String code, int port) {
        String host = "127.0.0.1";
        AdbKey key;
        try {
            key =
                new AdbKey(new PreferenceAdbKeyStore(FuckSettings.getPreferences()), "shizuku");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        L.d(TAG, "pair start");
        AdbPairingClient client = new AdbPairingClient(host, port, code, key);
        try {
            boolean ret = client.start();
            L.d(TAG, "pair ret", ret);

            postDelayed(() -> jumpToHome(
                () -> setupService(this::installApk)), delay(600));
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "pair error", e);
        }
    }

    private synchronized void checkPair() {
        String pairCode = FuckSettings.getPreferences().getString("pairCode", null);
        int pairPort = FuckSettings.getPreferences().getInt("pairPort", -1);
        L.d(TAG, "checkPair check", pairCode, pairPort);
        if (!TextUtils.isEmpty(pairCode) && pairPort >= 0) {
            adbMdns.stop();
            new Thread() {
                public void run() {
                    pair(pairCode, pairPort);
                }
            }.start();
        }
    }

    private static List<AccessibilityNodeInfo> findNodeByText(String text) {
        AccessibilityNodeInfo rootNode = GestureService.getRootNode();
        if (rootNode == null) return null;
        return rootNode.findAccessibilityNodeInfosByText(text);
    }

    private static List<AccessibilityNodeInfo> findNodeByViewId(String viewId) {
        AccessibilityNodeInfo rootNode = GestureService.getRootNode();
        if (rootNode == null) return null;
        return rootNode.findAccessibilityNodeInfosByViewId(viewId);
    }

    private static long delay(int i) {
        return (long) (i * 0.9f);
    }
}