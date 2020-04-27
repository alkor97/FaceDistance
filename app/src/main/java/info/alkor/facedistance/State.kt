package info.alkor.facedistance

import android.content.Context

class State(context: Context) : SimpleStorage(context, "state") {
    companion object {
        private const val key_lastFaceDistance = "lastFaceDistanceMm"
        private const val key_lastMeasurementTime = "lastMeasurementTime"
        private const val key_alarmScheduled = "alarmScheduled"
        private const val key_userObserverEnabled = "userObserverEnabled"
    }

    fun setLastFaceDistance(value: Int) = writeInt(key_lastFaceDistance, value)
    fun getLastFaceDistance() = readInt(key_lastFaceDistance, 60)
    fun setLastFaceDistanceChangeListener(listener: (Int) -> Unit) {
        super.registerListener(key_lastFaceDistance, listener, this::getLastFaceDistance)
    }

    fun setTimeOfLastMeasurement(value: String) = writeString(key_lastMeasurementTime, value)
    fun getTimeOfLastMeasurement() = readString(key_lastMeasurementTime, "")
    fun setTimeOfLastMeasurementChangeListener(listener: (String) -> Unit) {
        super.registerListener(key_lastMeasurementTime, listener, this::getTimeOfLastMeasurement)
    }

    fun setAlarmScheduled(value: Boolean) = writeBoolean(key_alarmScheduled, value)
    fun isAlarmScheduled() = readBoolean(key_alarmScheduled, false)
    fun setAlarmScheduledChangeListener(listener: (Boolean) -> Unit) {
        super.registerListener(key_alarmScheduled, listener, this::isAlarmScheduled)
    }

    fun setUserObserverEnabled(value: Boolean) = writeBoolean(key_userObserverEnabled, value)
    fun isUserObserverEnabled() = readBoolean(key_userObserverEnabled, false)
    fun setUserObserverEnabledChangeListener(listener: (Boolean) -> Unit) {
        super.registerListener(key_userObserverEnabled, listener, this::isUserObserverEnabled)
    }
}