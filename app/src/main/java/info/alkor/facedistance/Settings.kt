package info.alkor.facedistance

import android.content.Context
import java.util.concurrent.TimeUnit

class Settings(context: Context) : SimpleStorage(context, "settings") {
    companion object {
        private const val key_distanceBetweenEyes = "distanceBetweenEyesMm"
        private const val key_distanceThreshold = "distanceThresholdMm"
        private const val key_measuringPeriod = "measuringPeriodS"
    }

    fun setDistanceBetweenEyes(value: Int) = writeInt(key_distanceBetweenEyes, value)
    fun getDistanceBetweenEyes() = readInt(key_distanceBetweenEyes, 60)
    fun setDistanceBetweenEyesChangeListener(listener: (Int) -> Unit) {
        super.registerListener(key_distanceBetweenEyes, listener, this::getDistanceBetweenEyes)
    }

    fun setDistanceThreshold(value: Int) = writeInt(key_distanceThreshold, value)
    fun getDistanceThreshold() = readInt(key_distanceThreshold, 300)
    fun setDistanceThresholdChangeListener(listener: (Int) -> Unit) {
        super.registerListener(key_distanceThreshold, listener, this::getDistanceThreshold)
    }

    fun setMeasuringPeriod(value: Timeout) = writeLong(key_measuringPeriod, value.toSeconds())
    fun getMeasuringPeriod() = Timeout(readLong(key_measuringPeriod, 30), TimeUnit.SECONDS)
    fun setMeasuringPeriodChangeListener(listener: (Timeout) -> Unit) {
        super.registerListener(key_measuringPeriod,
            { seconds -> listener(Timeout(seconds, TimeUnit.SECONDS)) },
            { getMeasuringPeriod().toSeconds() })
    }
}