package ktu.edu.projektas.app.utils

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class LocalDateTimeConverter{
    @TypeConverter
    fun longToLocalDateTime(value: Long?): LocalDateTime? {
        return LocalDateTime.ofInstant(value?.let { Instant.ofEpochMilli(it) },
                TimeZone.getDefault().toZoneId())
    }
    @TypeConverter
    fun localDateTimeToLong(localDateTime : LocalDateTime?): Long? {
        return localDateTime?.atZone(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()
    }
}




