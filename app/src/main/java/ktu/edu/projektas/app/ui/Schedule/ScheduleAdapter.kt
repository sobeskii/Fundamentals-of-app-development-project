package ktu.edu.projektas.app.ui.Schedule

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.setEndTime
import com.alamkanak.weekview.jsr310.setStartTime
import ktu.edu.projektas.app.data.Event


// Created to show the weekly schedule component
// Boiler plate schedule adapter code
class ScheduleAdapter( private val clickListener: (data:Event) -> Unit) : WeekView.SimpleAdapter<Event>() {

    override fun onCreateEntity(item: Event): WeekViewEntity {
        val style = WeekViewEntity.Style.Builder()
                .setBackgroundColor(item.color)
                .build()
       return WeekViewEntity.Event.Builder(item)
               .setId(item.id)
               .setTitle(item.title)
               .setStartTime(item.startTime)
               .setEndTime(item.endTime)
               .setSubtitle(item.location)
               .setStyle(style)
               .build()
    }

    override fun onEventLongClick(data: Event) {
        if (data is Event) {
            clickListener(data)
        }
    }
}