package com.xuqiqiang.fuckmiui.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.xuqiqiang.fuckmiui.R;
import java.io.File;

public class ApkHelper {
    private static final String TAG = "ApkHelper";
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public ApkHelper(Context context) {
        mContext = context;
    }

    public void startDownloadSettingApk() {
        if (checkSettingApk()) {
            Toast.makeText(mContext, R.string.download_complete, Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog dialog =
            new ProgressDialog(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        dialog.setTitle(R.string.waiting);
        dialog.setMessage(mContext.getString(R.string.downloading));
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        new Thread() {
            public void run() {
                boolean ret = downloadSettingApk(progress -> mHandler.post(() -> {
                    if (dialog.isShowing()) {
                        dialog.setProgress((int) (progress * 100 / Constants.APK_SIZE));
                    }
                }));
                mHandler.post(() -> {
                    if (ret) {
                        Toast.makeText(mContext, R.string.download_complete, Toast.LENGTH_LONG)
                            .show();
                    } else {
                        Toast.makeText(mContext, R.string.download_error, Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                });
            }
        }.start();
    }

    public String getApkPath() {
        File dirFile = mContext.getExternalFilesDir("apk");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return dirFile.getPath() + File.separator + Constants.APK_NAME;
    }

    public boolean checkSettingApk() {
        File apkFile = new File(getApkPath());
        return apkFile.exists() && apkFile.length() == Constants.APK_SIZE;
    }

    private boolean downloadSettingApk(IOUtils.ProgressListener listener) {
        String apkPath = getApkPath();
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            apkFile.delete();
        }
        return IOUtils.downloadFile(Constants.APK_URL, apkPath, listener);
    }
}