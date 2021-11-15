package ktu.edu.projektas.app.data

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch
import ktu.edu.projektas.R
import ktu.edu.projektas.app.utils.localDateTimeToLong
import java.time.*
import java.util.*




class ScheduleViewModel(context: Context,
                        private val semesterStart: Long, private val semesterEnd: Long) : ViewModel() {

    private var fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _events: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

    private var _upcomingEvents: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

    private val currentTime     =   localDateTimeToLong(LocalDateTime.now())
    private val timeAfterHour   =   localDateTimeToLong(LocalDateTime.now().plusDays(1).withHour(0))


    private val context : Context = context

    internal var events:MutableLiveData<List<Event>>
        get() { return _events}
        set(value) {_events = value}

    internal var upcomingEvents:MutableLiveData<List<Event>>
        get() { return _upcomingEvents}
        set(value) {_upcomingEvents = value}

    init {
        fdb.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToEvents()
        listenToUpcomingEvents()
    }

    private fun listenToEvents() {
        fdb.collection("events").addSnapshotListener {
                snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allEvents = mutableListOf<Event>()
                val documents = snapshot.documents
                documents.forEach {

                    val event = it.toObject(Event::class.java)
                    if (event != null) {
                        event.firebaseId = it.id
                        allEvents.add(event!!)
                    }
                }
                _events.value = Collections.unmodifiableList(allEvents)
            }
        }
    }

    private fun listenToUpcomingEvents(){
        if (currentTime != null) {
            if (timeAfterHour != null) {
                fdb.collection("events")
                    .whereGreaterThan("startTime",currentTime)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen Failed", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val allEvents = mutableListOf<Event>()
                            val documents = snapshot.documents
                            documents.forEach {
                                val event = it.toObject(Event::class.java)
                                if (event != null && event.endTime <= timeAfterHour) {
                                    event.firebaseId = it.id
                                    allEvents.add(event!!)
                                }
                            }
                            _upcomingEvents.value = Collections.unmodifiableList(allEvents)
                        }
                    }
            }
        }
    }


    fun getAllEventsByColor(color:String) : LiveData<List<Event>>? {

        var colorCode = getColorCode(color)

        if(colorCode == -1)
            return events

        var data: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

        fdb.collection("events").addSnapshotListener {
                snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allEvents = mutableListOf<Event>()
                val documents = snapshot.documents
                documents.forEach {

                    val event = it.toObject(Event::class.java)
                    if (event != null && event.color == colorCode) {
                        event.firebaseId = it.id
                        allEvents.add(event!!)
                    }
                }
                data.value = Collections.unmodifiableList(allEvents)
            }
        }
        return data
    }


    fun getAllEventsByQuery(query:String) : LiveData<List<Event>>? {
        if(query.isEmpty())
            return events

        var data: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

        fdb.collection("events").
        orderBy("title").startAt(query).endAt(query+"\uf8ff").addSnapshotListener {
                snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allEvents = mutableListOf<Event>()
                val documents = snapshot.documents
                documents.forEach {

                    val event = it.toObject(Event::class.java)
                    if (event != null) {
                        event.firebaseId = it.id
                        allEvents.add(event!!)
                    }
                }
                data.value = Collections.unmodifiableList(allEvents)
            }
        }
        return data
    }

    fun deleteByGroup(groupId : Int) {
        val eventsRef: CollectionReference = fdb.collection("events")
        val docIdQuery: Query = eventsRef.whereEqualTo("groupId", groupId)
        docIdQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    document.reference.delete()
                        .addOnSuccessListener(object : OnSuccessListener<Void?> {
                            override fun onSuccess(aVoid: Void?) {
                                Log.d(TAG, "Document successfully deleted!")
                            }
                        }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            Log.w(TAG, "Error deleting document", e)
                        }
                    })
                }
            } else {
                Log.d(
                    TAG,
                    "Error getting documents: ",
                    task.getException()
                ) //Don't ignore potential errors!
            }
        }
    }

    fun addEvent(date: String, startTime: String, duration: String, name: String, color: String,location: String,group : Int = 0) {

        val yr : LocalDate = LocalDate.parse(date)
        val time = LocalTime.parse(startTime)

        var colorCode = getColorCode(color)

        var groupId =   if (group == 0)  generateGroupId()   else   group
        val startDateTime = yr.atTime(time)
        val endDateTime = startDateTime.plusMinutes(duration.toLong())

        val ref: DocumentReference = fdb.collection("events").document()
        val myId = ref.id


        localDateTimeToLong(startDateTime)?.let {
            localDateTimeToLong(endDateTime)?.let { it1 ->
                Event(myId,generateId(),groupId, name,
                    it, it1, colorCode,location)
            }
        }?.let {
            fdb.collection("events").document(myId).set(
                it,
                SetOptions.merge())
        }
    }
    fun massAddEvents(weekDay: String, startTime: String, duration: String, name: String, color: String, location:String, evenOdd:String) {
        viewModelScope.launch {
            val daysToAdd = (weekDay.toInt().toLong()-1)
            val startTimeValues = startTime.split(":")

            val hoursToAdd = startTimeValues[0].toInt().toLong()
            val minutesToAdd = startTimeValues[1].toInt().toLong()

            val startDate = Instant.ofEpochMilli(semesterStart!!).atZone(ZoneId.systemDefault()).toLocalDateTime()
            val firstDayOfGivenWeek = startDate.with(DayOfWeek.MONDAY)

            val addedDays = firstDayOfGivenWeek.plusDays(daysToAdd)
            val addedHours = addedDays.plusHours(hoursToAdd)
            val addedMins = addedHours.plusMinutes(minutesToAdd)

            val firstEventTime = addedMins.toInstant(OffsetDateTime.now().offset).toEpochMilli()/1000

            var iterate : Long = firstEventTime

            var groupId = generateGroupId()

            val getEvenOdd = getEvenOddValues(evenOdd)

            var weekNumber = 1

            while(iterate < (semesterEnd/1000)){
                if(weekNumber % 2 == 0 && getEvenOdd == 1) {
                    val eventStart = LocalDateTime.ofInstant(Instant.ofEpochSecond(iterate), OffsetDateTime.now().offset)
                    addEvent(eventStart.toLocalDate().toString(),eventStart.toLocalTime().toString(),duration,name,color, location,groupId)

                }
                else if(weekNumber % 2 != 0 && getEvenOdd == 2){
                    val eventStart = LocalDateTime.ofInstant(Instant.ofEpochSecond(iterate), OffsetDateTime.now().offset)
                    addEvent(eventStart.toLocalDate().toString(),eventStart.toLocalTime().toString(),duration,name,color, location,groupId)
                }
                else if(getEvenOdd == 0 ){
                    val eventStart = LocalDateTime.ofInstant(Instant.ofEpochSecond(iterate), OffsetDateTime.now().offset)
                    addEvent(eventStart.toLocalDate().toString(),eventStart.toLocalTime().toString(),duration,name,color, location,groupId)
                }
                iterate += 604800
                weekNumber += 1
            }

        }
    }
    private fun getEvenOddValues(str:String?) : Int{
        return when (str) {
            "Every week" -> 0
            "Even week" ->  1
            "Odd week" -> 2
            else -> 0
        }
    }
    private fun getColorCode(str:String?) : Int{


        return when (str) {
            "Red" -> ContextCompat.getColor(context, R.color.red_700)
            "Black" -> ContextCompat.getColor(context, R.color.black)
            "Blue" -> ContextCompat.getColor(context, R.color.blue)
            "Green" -> ContextCompat.getColor(context, R.color.green)
            "Cyan" -> ContextCompat.getColor(context, R.color.cyan)

            else -> -1
        }
    }
    private fun generateGroupId(): Int {
        return (0..100000).random()
    }
    private fun generateId(): Long {
        return (0..1000000000000).random()
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


