package info.alkor.facedistance

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    companion object {
        private const val tag = "app"
    }

    var distanceBetweenEyesMm = 60
        set(value) {
            field = value
            Log.d(tag, "distance between eyes set to $value mm")
        }
    var distanceThresholdMm = 300
        set(value) {
            field = value
            Log.d(tag, "distance threshold set to $value mm")
        }
    var measuringPeriod = Timeout(30, TimeUnit.SECONDS)
        set(value) {
            field = value
            Log.d(tag, "measuring period set to ${value.toSeconds()} seconds")
        }

    val userObserverEnabled = MutableLiveData<Boolean>()
    val alarmEnabled = MutableLiveData<Boolean>()
    val faceDistance = MutableLiveData<Int>()
    val isMeasuring = MutableLiveData<Boolean>()

    private val userStatusReceiver = UserStatusReceiver { userObserverEnabled.postValue(it) }
    private val alarmManager = PeriodicAlarmManager(this) { alarmEnabled.postValue(it) }
    private val faceDistanceManager = FaceDistanceManager(this,
        { faceDistance.postValue(it) },
        { isMeasuring.postValue(it) }
    )

    private val handler = Handler(Looper.getMainLooper())

    fun registerUserStatusReceiver() = userStatusReceiver.register(applicationContext)
    fun unregisterUserStatusReceiver() = userStatusReceiver.unregister(applicationContext)
    fun isUserStatusReceiverRegistered() = userStatusReceiver.isRegistered()

    fun scheduleAlarm() = alarmManager.scheduleAlarm(measuringPeriod)
    fun cancelAlarm() = alarmManager.cancelAlarm()
    fun isAlarmScheduled() = alarmManager.isAlarmScheduled()

    fun startFaceDistanceAnalysis() = runInMainThread {
        faceDistanceManager.startFaceDistanceAnalysis(distanceBetweenEyesMm.toFloat(), distanceThresholdMm.toFloat())
    }

    private fun runInMainThread(task: () -> Unit) = handler.post { task() }
}
