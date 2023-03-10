package com.xuqiqiang.fuckmiui.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.xuqiqiang.fuckmiui.R;
import java.lang.reflect.Method;

public class SystemUtils {
    private static final String TAG = "SystemUtils";

    public static void setStatusBarColor(Activity activity, int statusColor, int navColor) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusColor);
        window.setNavigationBarColor(navColor);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    public static void jumpToAppSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jumpToSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void jumpToDevelopmentSettings(Context context) {
        jumpToDevelopmentSettings(context, true);
    }

    public static void jumpToDevelopmentSettings(Context context, boolean clearTask) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        context.startActivity(intent);
        //Intent intent = new Intent();
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //ComponentName
        //    comp = new ComponentName("com.android.settings",
        //    "com.android.settings.Settings$DevelopmentSettingsDashboardActivity");
        //intent.setComponent(comp);
        //mContext.startActivity(intent);
    }

    public static void jumpToAppPermissions(Context context) {
        try {
            // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(localIntent);
        } catch (Exception ignore) {
        }
    }

    public static void jumpToAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void jumpToWifiSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void jumpToAntiFlicker(Context context) {
        try {
            Intent intent = new Intent();
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName
                comp = new ComponentName("com.xiaomi.misettings",
                "com.xiaomi.misettings.display.AntiFlickerMode.AntiFlickerActivity");
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, R.string.cannot_enter, Toast.LENGTH_LONG).show();
        }
    }

    public static void jumpToUrl(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, R.string.no_access, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean checkOp(Context context, int op) {
        int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = AppOpsManager.class.getDeclaredMethod(
                    "checkOp",
                    int.class,
                    int.class,
                    String.class
                );
                method.setAccessible(true);
                int isAllowNum =
                    (int) method.invoke(manager, op, Binder.getCallingUid(),
                        context.getPackageName());

                return AppOpsManager.MODE_ALLOWED == isAllowNum;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
