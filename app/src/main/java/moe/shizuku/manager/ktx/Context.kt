package moe.shizuku.manager.ktx

import android.content.Context
import android.os.Build
import com.xuqiqiang.fuckmiui.FuckApplication

val Context.application: FuckApplication
  get() {
    return applicationContext as FuckApplication
  }

fun Context.createDeviceProtectedStorageContextCompat(): Context {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    createDeviceProtectedStorageContext()
  } else {
    this
  }
}
