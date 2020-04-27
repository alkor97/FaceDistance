package info.alkor.facedistance

import android.content.Context

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

        reporter(StatisticsEntry(totalCount, tooCloseCount))
    }

    fun postStatistics() {
        val totalCount = getTotalMeasurementCount()
        val tooCloseCount = getTooCloseMeasurementCount()
        reporter(StatisticsEntry(totalCount, tooCloseCount))
    }
}

data class StatisticsEntry(
    val totalCount: Int,
    val tooCloseCount: Int
)

typealias StatisticsReporter = (StatisticsEntry) -> Unit
