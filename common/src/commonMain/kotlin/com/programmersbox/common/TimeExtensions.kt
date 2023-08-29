package com.programmersbox.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun getFormattedDate(
    milliseconds: Long,
): String = getFormattedDate(iso8601TimestampToLocalDateTime(Instant.fromEpochMilliseconds(milliseconds)))

internal fun getFormattedDate(
    iso8601Timestamp: Instant,
): String = getFormattedDate(iso8601TimestampToLocalDateTime(iso8601Timestamp))

private fun getFormattedDate(localDateTime: LocalDateTime): String {
    val date = localDateTime.date
    val day = date.dayOfMonth
    val month = date.monthNumber
    val year = date.year
    val dateTime = "${month.zeroPrefixed(2)}/${day.zeroPrefixed(2)}/${year}"
    val time = localDateTime.time
    val hour = time.hour
    val minute = time.minute
    val timeDate = "$hour:$minute"
    return "$timeDate - $dateTime"
}

private fun Int.zeroPrefixed(
    maxLength: Int,
): String {
    if (this < 0 || maxLength < 1) return ""

    val string = this.toString()
    val currentStringLength = string.length
    return if (maxLength <= currentStringLength) {
        string
    } else {
        val diff = maxLength - currentStringLength
        var prefixedZeros = ""
        repeat(diff) {
            prefixedZeros += "0"
        }
        "$prefixedZeros$string"
    }
}

private fun iso8601TimestampToLocalDateTime(timestamp: Instant): LocalDateTime {
    return timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
}