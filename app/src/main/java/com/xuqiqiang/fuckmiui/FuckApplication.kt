package com.xuqiqiang.fuckmiui

import android.app.Application
import android.content.Context
import android.os.Build
import com.topjohnwu.superuser.Shell
import com.xuqiqiang.fuckmiui.utils.L
import org.lsposed.hiddenapibypass.HiddenApiBypass

lateinit var application: FuckApplication

class FuckApplication : Application() {
    companion object {
        init {
            L.d("FuckApplication", "init")
            Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR))
            if (Build.VERSION.SDK_INT >= 28) {
                HiddenApiBypass.setHiddenApiExemptions("")
            }
            if (Build.VERSION.SDK_INT >= 30) {
                System.loadLibrary("adb")
            }
        }
    }

    private fun init(context: Context?) {
        FuckSettings.initialize(context)
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        init(this)
    }
}
