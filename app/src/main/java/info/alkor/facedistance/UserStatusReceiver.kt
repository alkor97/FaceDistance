package info.alkor.facedistance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * This class is responsible for detecting whether user is actively using the device.
 */
class UserStatusReceiver : BroadcastReceiver() {

    private val tag = "usr"
    private var registered = false

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
        Log.i(tag, "User is present, try to schedule alarm")
        appContext(context).scheduleAlarm()
    }

    private fun onScreenOff(context: Context) {
        Log.i(tag, "Screen is off, disabling alarm")
        appContext(context).cancelAlarm()
    }

    fun register(context: Context, enable: Boolean) = if (enable) register(context) else unregister(context)

    private fun register(context: Context): Boolean {
        if (!registered) {
            context.registerReceiver(this, IntentFilter(Intent.ACTION_SCREEN_OFF))
            context.registerReceiver(this, IntentFilter(Intent.ACTION_USER_PRESENT))
            Log.d(tag, "user status observer registered")
            registered = true
        }
        return registered
    }

    private fun unregister(context: Context): Boolean {
        if (registered) {
            context.unregisterReceiver(this)
            Log.d(tag, "user status observer unregistered")
            registered = false
        }
        return registered
    }

    private fun appContext(context: Context) = context.applicationContext as MyApplication
}