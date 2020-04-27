package info.alkor.facedistance

import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.Context
import android.util.Log

class PeriodicAlarmManager(private val context: Context) {

    private val tag = "pam"

    fun scheduleAlarm(timeout: Timeout) {
        if (isScreenNotLocked()) {
            if (!isAlarmScheduled()) {
                val now = System.currentTimeMillis()
                alarmManager().setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    now,
                    timeout.toMillis(),
                    PeriodicAlarmReceiver.prepareAlarmIntent(context)
                )
                Log.d(tag, "Alarm scheduled to be run every ${timeout.toSeconds()} seconds")
                notifyAlarmScheduled(true)
            } else {
                Log.d(tag, "Alarm is already scheduled")
            }
        } else {
            Log.d(tag, "Screen is locked, do not schedule alarm")
        }
    }

    fun cancelAlarm() {
        if (isAlarmScheduled()) {
            alarmManager().cancel(PeriodicAlarmReceiver.prepareAlarmIntent(context))
            Log.d(tag, "Alarm cleared")
            notifyAlarmScheduled(false)
        } else {
            Log.d(tag, "Alarm is already cancelled")
        }
    }

    fun onCreate() {
        val storedState = state().isAlarmScheduled()
        if (storedState != isAlarmScheduled()) {
            Log.d(tag, "Alarm is not " + (if (storedState) "scheduled" else "cancelled") + ", fixing this...")
            scheduleAlarm(storedState)
        }
    }

    private fun scheduleAlarm(enable: Boolean) =
        if (enable)
            scheduleAlarm(appContext().settings.getMeasuringPeriod())
        else
            cancelAlarm()

    private fun isScreenNotLocked() = !(context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
    private fun alarmManager() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private fun appContext() = context.applicationContext as MyApplication
    private fun isAlarmScheduled() = PeriodicAlarmReceiver.isAlarmRegistered(context)

    private fun state() = appContext().state
    private fun notifyAlarmScheduled(state: Boolean) = state().setAlarmScheduled(state)
}