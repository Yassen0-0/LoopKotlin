package com.loop.app.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun todayIsoDate(): String = isoDate(Calendar.getInstance())

fun isoDate(calendar: Calendar): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

fun displayDate(isoDate: String, locale: Locale = Locale.US): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate) ?: return@runCatching isoDate
    SimpleDateFormat("EEEE, d MMMM yyyy", locale).format(parsed)
}.getOrElse { isoDate }

fun shiftIsoDate(isoDate: String, days: Int): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    parser.parse(isoDate)?.let { calendar.time = it }
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return parser.format(calendar.time)
}

fun newId(prefix: String): String = "$prefix${System.currentTimeMillis()}"
