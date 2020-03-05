package info.alkor.facedistance

class Statistics(private val reporter: StatisticsReporter) {

    private var count = 0
    private var tooCloseCount = 0

    fun measurementTaken(tooClose: Boolean) {
        ++count
        if (tooClose) {
            ++tooCloseCount
        }
        reporter(StatisticsEntry(count, tooCloseCount))
    }
}

data class StatisticsEntry(
    val count: Int,
    val tooCloseCount: Int
)

typealias StatisticsReporter = (StatisticsEntry) -> Unit
