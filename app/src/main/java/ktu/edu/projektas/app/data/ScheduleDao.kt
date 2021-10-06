package ktu.edu.projektas.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>
    @Insert
    suspend fun insertEvent(event: Event)
    @Query("DELETE FROM events WHERE events.groupId = :id")
    suspend fun deleteByGroup(id:Int)
}