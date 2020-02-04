package info.alkor.facedistance

import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean

class PeriodicAlarmManager(private val context: Context, private val scheduledStream: (Boolean) -> Unit) {

    private val tag = "pam"

    private val scheduled = AtomicBoolean(false)

    fun scheduleAlarm(timeout: Timeout) {
        if (isScreenNotLocked()) {
            if (scheduled.compareAndSet(false, true)) {
                val now = System.currentTimeMillis()
                alarmManager().setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    now,
                    timeout.toMillis(),
                    PeriodicAlarmReceiver.prepareAlarmIntent(context)
                )
                Log.d(tag, "Alarm scheduled to be run every ${timeout.toSeconds()} seconds")
                scheduledStream(true)
            }
        } else {
            Log.d(tag, "Screen is locked, do not schedule alarm")
        }
    }

    fun cancelAlarm() {
        if (scheduled.compareAndSet(true, false)) {
            alarmManager().cancel(PeriodicAlarmReceiver.prepareAlarmIntent(context))
            Log.d(tag, "Alarm cleared")
            scheduledStream(false)
        }
    }

    fun isAlarmScheduled() = scheduled.get()

    private fun isScreenNotLocked() = !(context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
    private fun alarmManager() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}