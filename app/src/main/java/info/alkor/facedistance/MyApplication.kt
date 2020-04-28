package info.alkor.facedistance

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import java.text.DateFormat
import java.util.*

class MyApplication : Application() {

    val userObserverEnabled = MutableLiveData<Boolean>()
    val alarmEnabled = MutableLiveData<Boolean>()
    val faceDistance = MutableLiveData<Int>()
    val timeOfLastMeasurement = MutableLiveData<String>()
    val isMeasuring = MutableLiveData<Boolean>()
    val statistics = MutableLiveData<StatisticsEntry>()

    private val userStatusReceiver = UserStatusReceiver()
    private val alarmManager = PeriodicAlarmManager(this)
    private val stats = Statistics(this) { statistics.postValue(it) }
    private val faceDistanceManager = FaceDistanceManager(this,
        this::onFaceDistanceMeasured,
        { isMeasuring.postValue(it) },
        stats
    )
    val state = State(this)
    val settings = Settings(this)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        state.setLastFaceDistanceChangeListener { faceDistance.postValue(it) }
        state.setTimeOfLastMeasurementChangeListener { timeOfLastMeasurement.postValue(it) }
        state.setAlarmScheduledChangeListener { alarmEnabled.postValue(it) }
        state.setUserObserverEnabledChangeListener { userObserverEnabled.postValue(it) }

        alarmManager.onCreate()
    }

    fun registerUserStatusReceiver(enable: Boolean) = userStatusReceiver.register(applicationContext, enable)

    fun scheduleAlarm() = alarmManager.scheduleAlarm(settings.getMeasuringPeriod())
    fun cancelAlarm() = alarmManager.cancelAlarm()

    fun startFaceDistanceAnalysis() = runInMainThread {
        faceDistanceManager.startFaceDistanceAnalysis(
            settings.getDistanceBetweenEyes().toFloat(),
            settings.getDistanceThreshold().toFloat()
        )
    }

    fun postState() {
        userObserverEnabled.postValue(state.isUserObserverEnabled())
        alarmEnabled.postValue(state.isAlarmScheduled())
        timeOfLastMeasurement.postValue(state.getTimeOfLastMeasurement())
        faceDistance.postValue(state.getLastFaceDistance())
        stats.postStatistics()
    }

    fun logStorage() {
        state.logContent()
        stats.logContent()
    }

    private fun runInMainThread(task: () -> Unit) = handler.post { task() }
    private fun onFaceDistanceMeasured(distance: FaceDistance) {
        if (distance is Success) {
            state.setLastFaceDistance(distance.value)
        } else if (distance is Failure) {
            stats.measurementFailed(distance.error)
        }
        postTImestamp()
    }

    private fun postTImestamp() {
        state.setTimeOfLastMeasurement(DateFormat.getTimeInstance().format(Date()))
    }
}
