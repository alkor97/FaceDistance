package info.alkor.facedistance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class UserStatusReceiver(private val registrationObserver: (Boolean) -> Unit) : BroadcastReceiver() {

    private val tag = "uso"
    private val registered = AtomicBoolean(false)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                onUserPresent(context)
            } else if (intent.action == Intent.ACTION_SCREEN_OFF) {
                onScreenOff(context)
            }
        }
    }

    private fun onUserPresent(context: Context) {
        Log.i("screen-status", "User is present, try to schedule alarm")
        appContext(context).scheduleAlarm()
    }

    private fun onScreenOff(context: Context) {
        Log.i("screen-status", "Screen is off, disabling alarm")
        appContext(context).cancelAlarm()
    }

    fun register(context: Context): Boolean {
        if (registered.compareAndSet(false, true)) {
            context.registerReceiver(this, IntentFilter(Intent.ACTION_SCREEN_OFF))
            context.registerReceiver(this, IntentFilter(Intent.ACTION_USER_PRESENT))
            Log.d(tag, "user status observer registered")
            registrationObserver(true)
        }
        return registered.get()
    }

    fun unregister(context: Context): Boolean {
        if (registered.compareAndSet(true, false)) {
            context.unregisterReceiver(this)
            Log.d(tag, "user status observer unregistered")
            registrationObserver(false)
        }
        return registered.get()
    }

    fun isRegistered() = registered.get()

    private fun appContext(context: Context) = context.applicationContext as MyApplication
}