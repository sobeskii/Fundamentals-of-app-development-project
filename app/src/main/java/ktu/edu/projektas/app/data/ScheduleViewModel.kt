package ktu.edu.projektas.app.data

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch
import ktu.edu.projektas.R
import ktu.edu.projektas.app.utils.localDateTimeToLong
import java.time.*
import java.util.*
import androidx.fragment.app.activityViewModels
import kotlin.collections.HashMap
import kotlin.math.log

// schedule's ViewModel class
class ScheduleViewModel(context: Context): ViewModel() {

    private var fdb: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var _events: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()
    private var _upcomingEvents: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()
    private var _notifications: MutableLiveData<List<Notification>> = MutableLiveData<List<Notification>>()

    private val currentTime     =   localDateTimeToLong(LocalDateTime.now())
    private val timeAfterHour   =   localDateTimeToLong(LocalDateTime.now().plusDays(1).withHour(0))

    private var _userData : User? = null
    private var _semesterStart : Long? = null
    private var _semesterEnd : Long? = null
    private var _semesterDateId : String? = null


    private val context : Context = context

    internal var userData:User?
        get() {
            getUserData()
            return _userData
        }
        set(value) {_userData = value!! }

    internal var semesterEnd:Long?
        get() {
            getSemesterDates()
            return _semesterEnd
        }
        set(value) {_semesterEnd = value!! }

    internal var semesterStart:Long?
        get() {
            getSemesterDates()
            return _semesterStart
        }
        set(value) {_semesterStart = value!! }

    internal var events:MutableLiveData<List<Event>>
        get() {
            listenToEvents()
            return _events
        }
        set(value) {_events = value}

    internal var notifications:MutableLiveData<List<Notification>>
        get() {
            listenToNotifications()
            return _notifications
        }
        set(value) {_notifications = value}

    internal var upcomingEvents:MutableLiveData<List<Event>>
        get() {
            listenToUpcomingEvents()
            return _upcomingEvents}
        set(value) {_upcomingEvents = value}

    init {
        fdb.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        getSemesterDates()
        listenToEvents()
        listenToUpcomingEvents()
        getUserData()
    }

    // gathers all events
    private fun listenToNotifications() {
        val user = FirebaseAuth.getInstance().currentUser

        fdb.collection("notifications").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allNotifications = mutableListOf<Notification>()
                val documents = snapshot.documents
                documents.forEach {

                    val notification = it.toObject(Notification::class.java)
                    if (notification != null && user != null) {
                        notification.firebaseId = it.id
                        if (notification.userid == user.uid) {
                            allNotifications.add(notification)
                        }
                    }
                }
                _notifications.value = Collections.unmodifiableList(allNotifications)
            }
        }
    }



    private fun getSemesterDates() {
        fdb.collection("semester_dates").limit(1).addSnapshotListener{
                snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val semesterDates = snapshot.documents[0]
                _semesterEnd = semesterDates.get("semester_end") as Long?
                _semesterStart = semesterDates.get("semester_start") as Long?
                _semesterDateId = semesterDates.id
            }
        }
    }
    fun updateSemesterDate(date: Long,start : Boolean){
        if(start){
            fdb.collection("semester_dates").document(_semesterDateId!!).update("semester_start",date)
        }else{
            fdb.collection("semester_dates").document(_semesterDateId!!).update("semester_end",date)
        }
    }

    fun getUserData(){
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            fdb.collection("users").document(user.uid)
                .addSnapshotListener { documentSnapshot, e ->
                    if (documentSnapshot != null) {
                        userData = User(
                            documentSnapshot.getString("firstName")!!,
                            documentSnapshot.getString("lastName")!!,
                            documentSnapshot.getString("email")!!,
                            documentSnapshot.getString("role")!!,
                            documentSnapshot.getString("group")!!,
                            documentSnapshot.getString("firebaseId")!!,
                        )
                    }
                }
        }
    }


    // gathers all events
    private fun listenToEvents() {
        val user = FirebaseAuth.getInstance().currentUser

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
                    if (event != null && user != null) {
                        event.firebaseId = it.id
                        if(event.groupId == 0 ){
                            if(event.userUUID == user.uid){
                                allEvents.add(event)
                            }
                        }else if( event.groupId != 0){
                            allEvents.add(event)
                        }
                    }
                }
                _events.value = Collections.unmodifiableList(allEvents)
            }
        }
    }
    // gathers all upcoming events for today
    private fun listenToUpcomingEvents() {
        val user = FirebaseAuth.getInstance().currentUser
        if (currentTime != null) {
            if (timeAfterHour != null) {
                    fdb.collection("events")
                    .whereGreaterThan("startTime", currentTime)
                    .addSnapshotListener {snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen Failed", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val allEvents = mutableListOf<Event>()
                            val documents = snapshot.documents
                            documents.forEach {
                                val event = it.toObject(Event::class.java)
                                if (event != null && user != null && event.endTime <= timeAfterHour) {
                                    event.firebaseId = it.id
                                    if(event.groupId == 0 ){
                                        if(event.userUUID == user!!.uid){
                                            allEvents.add(event!!)
                                        }
                                    }else if( event.groupId != 0){
                                        allEvents.add(event)
                                    }
                                }

                            }
                            _upcomingEvents.value = Collections.unmodifiableList(allEvents)
                        }
                    }
            }
        }
    }


    fun getAllEventsByColor(color:String) : LiveData<List<Event>>? {
        val user = FirebaseAuth.getInstance().currentUser

        var colorCode = getColorCode(color)

        if(colorCode == -1)
            return events

        val data: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

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
                    if (event != null &&  user != null && event.color == colorCode) {
                        event.firebaseId = it.id
                        if(event.groupId == 0 ){
                            if(event.userUUID == user!!.uid){
                                allEvents.add(event!!)
                            }
                        }else if( event.groupId != 0){
                            allEvents.add(event)
                        }
                    }
                }
                data.value = Collections.unmodifiableList(allEvents)
            }
        }
        return data
    }

    // gathers events fulfilling a specified query
    fun getAllEventsByQuery(query: String): LiveData<List<Event>> {
        val user = FirebaseAuth.getInstance().currentUser
        if(query.isEmpty())
            return events

        val data: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

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
                    if (event != null && user != null ) {
                        event.firebaseId = it.id
                        if(event.groupId == 0 ){
                            if(event.userUUID == user!!.uid){
                                allEvents.add(event!!)
                            }
                        }else if( event.groupId != 0){
                            allEvents.add(event)
                        }
                    }
                }
                data.value = Collections.unmodifiableList(allEvents)
            }
        }
        return data
    }

    // deletes events with a specified groupId
    fun deleteByGroup(groupId: Int) {
        val eventsRef: CollectionReference = fdb.collection("events")
        val docIdQuery: Query = eventsRef.whereEqualTo("groupId", groupId)
        docIdQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "Document successfully deleted!")
                        }.addOnFailureListener { e ->
                            Log.w(
                                TAG,
                                "Error deleting document",
                                e
                            )
                        }
                }
            } else {
                Log.d(
                    TAG,
                    "Error getting documents: ",
                    task.exception
                )
            }
        }
    }
    fun deleteByFirebaseId(id : String){
        fdb.collection("events").document(id).delete().
        addOnSuccessListener {
            Log.d(TAG, "Document successfully deleted!")
        }.addOnFailureListener{ e ->
            Log.w(
                TAG,
                "Error deleting document",
                e
            )
        }
    }


    fun addEvent(date: String, startTime: String, duration: String, name: String, color: String,location: String,group : Int = 0) {
        val user = FirebaseAuth.getInstance().currentUser

        val yr : LocalDate = LocalDate.parse(date)
        val time = LocalTime.parse(startTime)

        val colorCode = getColorCode(color)

        val groupId =   if (group == 0)  0   else   group
        val startDateTime = yr.atTime(time)
        val endDateTime = startDateTime.plusMinutes(duration.toLong())

        val ref: DocumentReference = fdb.collection("events").document()
        val myId = ref.id

        if (user != null) {
            localDateTimeToLong(startDateTime)?.let {
                localDateTimeToLong(endDateTime)?.let { it1 ->
                    Event(myId,generateId(),groupId, name,
                        it, it1, colorCode,location,user.uid)
                }
            }?.let {
                fdb.collection("events").document(myId).set(
                    it,
                    SetOptions.merge())
            }
        }
    }

    // adds recurring events
    fun massAddEvents(weekDay: String, startTime: String, duration: String, name: String, color: String, location: String, evenOdd: String) {
        viewModelScope.launch {
            val daysToAdd = (weekDay.toInt().toLong()-1)
            val startTimeValues = startTime.split(":")

            val hoursToAdd = startTimeValues[0].toInt().toLong()
            val minutesToAdd = startTimeValues[1].toInt().toLong()

            val startDate = Instant.ofEpochMilli(_semesterStart!!).atZone(ZoneId.systemDefault()).toLocalDateTime()
            val firstDayOfGivenWeek = startDate.with(DayOfWeek.MONDAY)

            val addedDays = firstDayOfGivenWeek.plusDays(daysToAdd)
            val addedHours = addedDays.plusHours(hoursToAdd)
            val addedMins = addedHours.plusMinutes(minutesToAdd)

            val firstEventTime = addedMins.toInstant(OffsetDateTime.now().offset).toEpochMilli()/1000

            var iterate: Long = firstEventTime

            val groupId = generateGroupId()

            val getEvenOdd = getEvenOddValues(evenOdd)

            var weekNumber = 1

            while(iterate < (_semesterEnd!! / 1000)){
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

    // sets int of recurrence
    private fun getEvenOddValues(str: String?): Int{
        return when (str) {
            "Every week" -> 0
            "Even week" ->  1
            "Odd week" -> 2
            else -> 0
        }
    }

    // sets colors
    private fun getColorCode(str: String?): Int{
        return when (str) {
            "Red" -> ContextCompat.getColor(context, R.color.red_light)
            "Grey" -> ContextCompat.getColor(context, R.color.grey_light)
            "Blue" -> ContextCompat.getColor(context, R.color.blue_light)
            "Green" -> ContextCompat.getColor(context, R.color.green_light)
            "Cyan" -> ContextCompat.getColor(context, R.color.cyan_light)

            else -> -1
        }
    }

    // generates groupId
    private fun generateGroupId(): Int {
        return (1..100000).random()
    }

    // generates Id
    private fun generateId(): Long {
        return (1..1000000000000).random()
    }
}

class ScheduleViewModelFactory(val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(context) as T
        }
        throw IllegalArgumentException()
    }
}


