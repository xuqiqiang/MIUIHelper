package com.xuqiqiang.fuckmiui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.xuqiqiang.fuckmiui.BuildConfig;
import com.xuqiqiang.fuckmiui.R;
import com.xuqiqiang.fuckmiui.utils.ApkHelper;
import com.xuqiqiang.fuckmiui.utils.AutoHelper;
import com.xuqiqiang.fuckmiui.utils.Constants;
import com.xuqiqiang.fuckmiui.utils.GestureService;
import com.xuqiqiang.fuckmiui.utils.SystemUtils;
import moe.shizuku.manager.starter.Starter;

import static com.xuqiqiang.fuckmiui.utils.ViewUtils.dialog;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";
    private AutoHelper mAutoHelper;
    private boolean mApkStatus;

    private TextView btRun;
    private TextView tvStatus;
    private View vStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtils.setStatusBarColor(this, 0xFF4a90e2, Color.WHITE);
        setContentView(R.layout.activity_home);
        initUI();
        writeStarterFiles();
        checkApkStatus();
    }

    private void initUI() {
        btRun = findViewById(R.id.bt_run);
        tvStatus = findViewById(R.id.tv_status);
        vStatus = findViewById(R.id.v_status);
    }

    @SuppressLint("NewApi")
    public void updateUI() {
        if (isFinishing()) return;
        if (mApkStatus) {
            tvStatus.setText(R.string.anti_flashing_fixed);
            tvStatus.setTextColor(0xFF333333);
            vStatus.setBackgroundResource(R.drawable.oval_green_14);

            btRun.setText(R.string.anti_flashing_mode);
            btRun.setBackgroundResource(R.drawable.btn_bg_selector);
        } else {
            tvStatus.setText(R.string.anti_flashing_not_fix);
            tvStatus.setTextColor(0xFF999999);
            vStatus.setBackgroundResource(R.drawable.oval_grey_14);

            if (mAutoHelper != null && mAutoHelper.isRunning()) {
                btRun.setText(R.string.stop_operation);
                btRun.setBackgroundResource(R.drawable.btn_bg_selected_selector);
            } else {
                btRun.setText(R.string.start_repairing);
                btRun.setBackgroundResource(R.drawable.btn_bg_selector);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mAutoHelper != null) {
                mAutoHelper.stopRunAuto();
            }
        }
    }

    public void checkApkStatus() {
        new Thread() {
            public void run() {
                mApkStatus = loadApkStatus();
                runOnUiThread(HomeActivity.this::updateUI);
            }
        }.start();
    }

    private boolean loadApkStatus() {
        // for test
        //if (true) return false;
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo("com.xiaomi.misettings", 0);
            return pi.versionCode <= 220113010;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startRun(View view) {
        if (mApkStatus) {
            SystemUtils.jumpToAntiFlicker(this);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mAutoHelper != null && mAutoHelper.isRunning()) {
                mAutoHelper.stopRunAuto();
                return;
            }
            if (!checkCondition()) return;
            mAutoHelper = new AutoHelper(this);
            mAutoHelper.runAuto();
            mAutoHelper.postDelayed(this::updateUI, 1000);
        } else {
            Toast.makeText(this, R.string.not_support, Toast.LENGTH_SHORT).show();
        }
    }

    public void moreInfo(View view) {
        dialog(this, R.string.learn_more, getString(R.string.access_project_address)
                + "https://github.com/xuqiqiang/MIUIHelper",
            () -> SystemUtils.jumpToUrl(this, "https://github.com/xuqiqiang/MIUIHelper"), true);
    }

    private void writeStarterFiles() {
        new Thread() {
            public void run() {
                try {
                    Starter.INSTANCE.writeSdcardFiles(HomeActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> dialog(HomeActivity.this, R.string.some_issue,
                        Log.getStackTraceString(e),
                        null, false));
                }
            }
        }.start();
    }

    private boolean checkCondition() {
        ConnectivityManager connectMgr = (ConnectivityManager) this
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info == null || info.getType() != ConnectivityManager.TYPE_WIFI) {
            dialog(this, R.string.connect_wifi, 0,
                () -> SystemUtils.jumpToWifiSettings(this), true);
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ApkHelper apkHelper = new ApkHelper(this);
            if (!apkHelper.checkSettingApk()) {
                dialog(this, R.string.download_file, R.string.file_size,
                    apkHelper::startDownloadSettingApk, true);
                return false;
            }
        }

        if (Settings.Secure.getInt(getContentResolver(),
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) <= 0) {
            dialog(this, R.string.enable_developer_mode, 0,
                () -> SystemUtils.jumpToSettings(this), true);
            return false;
        }

        if (Settings.Secure.getInt(getContentResolver(),
            Settings.Global.ADB_ENABLED, 0) <= 0) {
            dialog(this, R.string.enable_usb_debugging, R.string.enable_usb_debugging_desc,
                () -> SystemUtils.jumpToDevelopmentSettings(this), true);
            return false;
        }

        if (!GestureService.isRunning()) {
            dialog(this, R.string.enable_accessibility,
                getString(R.string.click_downloaded_apps) + getString(R.string.app_name) + "]",
                () -> SystemUtils.jumpToAccessibilitySettings(this), true);
            return false;
        }

        if (!SystemUtils.checkOp(this, Constants.OP_AUTO_START)) {
            dialog(this, R.string.enable_self_start, R.string.not_need_to_authorize_again,
                () -> SystemUtils.jumpToAppSetting(this), true);
            return false;
        }
        return true;
    }
}