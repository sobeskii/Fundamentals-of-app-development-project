package ktu.edu.projektas.app.ui.Schedule

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.setEndTime
import com.alamkanak.weekview.jsr310.setStartTime
import ktu.edu.projektas.app.data.Event
import ktu.edu.projektas.app.utils.longToLocalDateTime

class ScheduleAdapter( private val clickListener: (data:Event) -> Unit,
                       private val secondListener: (data:Event) -> Unit) : WeekView.SimpleAdapter<Event>() {
    override fun onCreateEntity(item: Event): WeekViewEntity {
        val style = WeekViewEntity.Style.Builder()
                .setBackgroundColor(item.color)
                .build()
       return longToLocalDateTime(item.startTime)?.let {
           longToLocalDateTime(item.endTime)?.let { it1 ->
               WeekViewEntity.Event.Builder(item)
                   .setId(item.id)
                   .setTitle(item.title)
                   .setStartTime(it)
                   .setEndTime(it1)
                   .setSubtitle(item.location)
                   .setStyle(style)
                   .build()
           }
       }!!
    }
    override fun onEventLongClick(data: Event) {
        if (data is Event) {
            clickListener(data)
        }
    }

    override fun onEventClick(data: Event) {
        if (data is Event) {
            secondListener(data)
        }
    }
}