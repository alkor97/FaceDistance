package info.alkor.facedistance

import java.util.concurrent.TimeUnit

data class Timeout (
    val value: Long,
    val unit: TimeUnit
) {
    fun toMillis() = unit.toMillis(value)
    fun toSeconds() = unit.toSeconds(value)
}