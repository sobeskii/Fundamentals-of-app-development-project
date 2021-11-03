package ktu.edu.projektas.app.data

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import ktu.edu.projektas.app.utils.localDateTimeToLong
import java.time.*
import java.util.*
import kotlin.collections.ArrayList
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions


class ScheduleViewModel(context: Context,
                        private val semesterStart: Long, private val semesterEnd: Long) : ViewModel() {

    private var fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _events: MutableLiveData<List<Event>> = MutableLiveData<List<Event>>()

    internal var events:MutableLiveData<List<Event>>
        get() { return _events}
        set(value) {_events = value}

    init {
        fdb.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToEvents()
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
    fun deleteByGroup(id:String) {
        fdb.collection("events").document(id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    fun addEvent(date: String, startTime: String, duration: String, name: String, color: String,location: String) {

        val yr : LocalDate = LocalDate.parse(date)
        val time = LocalTime.parse(startTime)

        var colorCode = getColorCode(color)
        var groupId = generateGroupId()

        val startDateTime = yr.atTime(time)
        val endDateTime = startDateTime.plusMinutes(duration.toLong())

        val ref: DocumentReference = fdb.collection("events").document()
        val myId = ref.id


        localDateTimeToLong(startDateTime)?.let {
            localDateTimeToLong(endDateTime)?.let { it1 ->
                Event(myId,0,groupId, name,
                    it, it1, colorCode,location)
            }
        }?.let {
            fdb.collection("events").document(myId).set(
                it,
                SetOptions.merge())
        }
    }
    private fun getColorCode(str:String?) : Int{
        return when (str) {
            "Red" -> Color.RED
            "Black" -> Color.BLACK
            "Blue" -> Color.BLUE
            "Green" -> Color.GREEN
            "Cyan" -> Color.CYAN

            else -> -1
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


