package moe.shizuku.manager

import android.os.Bundle
import com.xuqiqiang.fuckmiui.utils.L
import moe.shizuku.api.BinderContainer
import moe.shizuku.manager.utils.workerHandler
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_TOKEN
import rikka.shizuku.ShizukuProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ShizukuManagerProvider : ShizukuProvider() {

    companion object {
        private const val EXTRA_BINDER = "com.xuqiqiang.fuckmiui.intent.extra.BINDER"
        private const val METHOD_SEND_USER_SERVICE = "sendUserService"
    }

    override fun onCreate(): Boolean {
        disableAutomaticSuiInitialization()
        return super.onCreate()
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (extras == null) return null

        return if (method == METHOD_SEND_USER_SERVICE) {
            try {
                extras.classLoader = BinderContainer::class.java.classLoader

                val token = extras.getString(USER_SERVICE_ARG_TOKEN) ?: return null
                val binder = extras.getParcelable<BinderContainer>(EXTRA_BINDER)?.binder ?: return null

                val countDownLatch = CountDownLatch(1)
                var reply: Bundle? = Bundle()

                val listener = object : Shizuku.OnBinderReceivedListener {

                    override fun onBinderReceived() {
                        try {
                            val bundle = Bundle()
                            bundle.putString(USER_SERVICE_ARG_TOKEN, token)
                            Shizuku.attachUserService(binder, bundle)
                            reply!!.putParcelable(EXTRA_BINDER, BinderContainer(Shizuku.getBinder()))
                        } catch (e: Throwable) {
                            L.e("onBinderReceived", "attachUserService $token", e)
                            reply = null
                        }

                        Shizuku.removeBinderReceivedListener(this)

                        countDownLatch.countDown()
                    }
                }

                Shizuku.addBinderReceivedListenerSticky(listener, workerHandler)

                return try {
                    countDownLatch.await(5, TimeUnit.SECONDS)
                    reply
                } catch (e: TimeoutException) {
                    L.e("call", "Binder not received in 5s", e)
                    null
                }
            } catch (e: Throwable) {
                L.e("call", "sendUserService", e)
                null
            }
        } else {
            super.call(method, arg, extras)
        }
    }
}
