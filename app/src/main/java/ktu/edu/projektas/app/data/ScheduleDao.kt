package ktu.edu.projektas.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>
    @Query("SELECT * FROM events WHERE events.color = :color")
    fun getAllEventsByColor(color:Int): Flow<List<Event>>
    @Insert
    suspend fun insertEvent(event: Event)
    @Query("DELETE FROM events WHERE events.groupId = :id")
    suspend fun deleteByGroup(id:Int)
}

//@Query("UPDATE orders SET order_desc = :description, order_title= :title WHERE order_id =:id")
//void update(String description, String title, int id);