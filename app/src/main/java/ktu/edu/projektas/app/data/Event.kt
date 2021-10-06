package ktu.edu.projektas.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event (
        @PrimaryKey(autoGenerate = true) val id: Long,
        val groupId: Int,
        val title: String,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val color : Int,
        val location : String
        )