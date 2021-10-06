package ktu.edu.projektas.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ktu.edu.projektas.app.utils.LocalDateTimeConverter

@Database(entities = arrayOf(Event::class), version = 4, exportSchema = true)
@TypeConverters(LocalDateTimeConverter::class)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun ScheduleDao(): ScheduleDao
}
