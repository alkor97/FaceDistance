package info.alkor.facedistance

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PeriodicAlarmReceiver : BroadcastReceiver() {

    private val tag = "alm"

    companion object {
        private fun prepareAlarmIntent(context: Context, flag: Int): PendingIntent? {
            val intent = Intent(context, PeriodicAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context,
                12345,
                intent,
                flag
            )
        }

        fun prepareAlarmIntent(context: Context) = prepareAlarmIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)!!
        fun isAlarmRegistered(context: Context) = null != prepareAlarmIntent(context, PendingIntent.FLAG_NO_CREATE)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "alarm received, starting face distance service")
        val serviceIntent = Intent(context, FaceDistanceService::class.java)
        context.startService(serviceIntent)
    }
}