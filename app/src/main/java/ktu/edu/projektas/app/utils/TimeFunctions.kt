package ktu.edu.projektas.app.utils

import java.sql.Time
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun convertLocalDateToLong(value: LocalDate?) : Long? {
    return value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
}
fun getCurrentMonthLastDay(): Instant? {
    return YearMonth.now().atEndOfMonth().atStartOfDay().toInstant(
            ZoneOffset.UTC)
}
fun getCurrentMonthFirstDay():Instant? {
    return YearMonth.now().atDay(1).atStartOfDay().toInstant(
            ZoneOffset.UTC)
}
fun formatLocalDate(date: LocalDate) : String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return date.format(formatter)
}
fun formatTime(time: Time) : String{
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(time)
}
