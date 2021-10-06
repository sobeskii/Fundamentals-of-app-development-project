package ktu.edu.projektas.app.data

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.*
import androidx.room.Room
import kotlinx.coroutines.launch
import java.time.*


class ScheduleViewModel(context: Context,
                        private val semesterStart: Long, private val semesterEnd: Long) : ViewModel() {

    private val db = Room.databaseBuilder(context, ScheduleDatabase::class.java, "events").build()

    private val _events = db.ScheduleDao().getAllEvents().asLiveData()
    val events: LiveData<List<Event>>
        get() = _events

    fun deleteByGroup(id:Int) {
        viewModelScope.launch {
            db.ScheduleDao().deleteByGroup(id)
        }
    }
    fun addEvent(date: String, startTime: String, duration: String, name: String, color: String,location: String) {
        viewModelScope.launch {
            val yr : LocalDate = LocalDate.parse(date)

            val time = LocalTime.parse(startTime)

            var colorCode = getColorCode(color)
            var groupId = generateGroupId()

            val startDateTime = yr.atTime(time)
            val endDateTime = startDateTime.plusMinutes(duration.toLong())

            db.ScheduleDao().insertEvent(Event(0, groupId, name, startDateTime, endDateTime, colorCode,location))
        }
    }
    private fun getColorCode(str:String?) : Int{
        return when (str) {
            "Red" -> Color.RED
            "Black" -> Color.BLACK
            "Blue" -> Color.BLUE
            "Green" -> Color.GREEN
            "Cyan" -> Color.CYAN

            else -> Color.BLACK
        }
    }
    private fun generateGroupId(): Int {
        return (0..100000).random()
    }
}


class ScheduleViewModelFactory(val context:Context, private val semesterStart: Long, private val semesterEnd: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(context, semesterStart, semesterEnd) as T
        }
        throw IllegalArgumentException()
    }
}


