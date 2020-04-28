package info.alkor.facedistance

import android.content.Context
import java.util.*

class Statistics(context: Context, private val reporter: StatisticsReporter) : SimpleStorage(context, "statistics") {

    companion object {
        private const val key_totalMeasurementCount = "totalMeasurementCount"
        private const val key_tooCloseMeasurementCount = "tooCloseMeasurementCount"
    }

    fun setTotalMeasurementCount(value: Int) = writeInt(key_totalMeasurementCount, value)
    fun getTotalMeasurementCount() = readInt(key_totalMeasurementCount, 0)

    fun setTotalTooCloseMeasurementCount(value: Int) = writeInt(key_tooCloseMeasurementCount, value)
    fun getTooCloseMeasurementCount() = readInt(key_tooCloseMeasurementCount, 0)

    fun measurementTaken(tooClose: Boolean) {
        val totalCount = getTotalMeasurementCount() + 1
        setTotalMeasurementCount(totalCount)

        val tooCloseCount = getTooCloseMeasurementCount() + if (tooClose) 1 else 0
        setTotalTooCloseMeasurementCount(tooCloseCount)

        postStatistics()
    }

    fun getErrorCount(error: Error) = readInt(error.keyCount(), 0)
    fun setErrorCount(error: Error, value: Int) = writeInt(error.keyCount(), value)

    fun measurementFailed(error: Error) {
        val count = getErrorCount(error) + 1
        setErrorCount(error, count)
        postStatistics()
    }

    private fun getErrorMap(): Map<Error, Int> {
        val map = EnumMap<Error, Int>(Error::class.java)
        for (error in Error.values()) {
            val count = getErrorCount(error)
            if (count > 0) {
                map[error] = getErrorCount(error)
            }
        }
        return map
    }

    fun postStatistics() {
        reporter(StatisticsEntry(getTotalMeasurementCount(), getTooCloseMeasurementCount(), getErrorMap()))
    }
}

data class StatisticsEntry(
    val totalCount: Int,
    val tooCloseCount: Int,
    val errors: Map<Error, Int>
)

typealias StatisticsReporter = (StatisticsEntry) -> Unit
