package com.xuqiqiang.fuckmiui.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import com.xuqiqiang.fuckmiui.R;

public class ViewUtils {

    public static void dialog(Context context, int titleId, int messageId, Runnable onPositive,
        boolean showCancel) {
        dialog(context, titleId, messageId == 0 ? null : context.getString(messageId), onPositive,
            showCancel);
    }

    public static void dialog(Context context, int titleId, String message, Runnable onPositive,
        boolean showCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        if (titleId != 0) {
            builder.setTitle(titleId);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        builder.setPositiveButton(android.R.string.ok,
            onPositive == null ? null : (dialog, which) -> onPositive.run());
        if (showCancel) {
            builder.setNegativeButton(android.R.string.cancel, null);
        }
        builder.create().show();
    }

    public static ProgressDialog progressDialog(Context context, int titleId, int messageId, boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        dialog.setTitle(titleId);
        dialog.setMessage(context.getString(messageId));
        dialog.setIndeterminate(true);
        dialog.setCancelable(cancelable);
        dialog.show();
        return dialog;
    }
}
