package ktu.edu.projektas.app.ui.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.EventReg
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.app.ui.HomeFragment
import ktu.edu.projektas.app.utils.*
import ktu.edu.projektas.databinding.FragmentEventBinding
import java.time.LocalDateTime

// fragment class for viewing event's details
class EventFragment: Fragment() {

    private lateinit var binding: FragmentEventBinding
    private val user = FirebaseAuth.getInstance().currentUser
    private val db =    FirebaseFirestore.getInstance()
    private lateinit var userData  :   DocumentSnapshot
    private var userData1 : User? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db.collection("users").document(user!!.uid).get().addOnSuccessListener {
            userData = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        userData1 = viewModel.userData!!

        binding = FragmentEventBinding.inflate(inflater, container, false)

        val args = EventFragmentArgs.fromBundle(requireArguments())

        binding.eventNameText.text = args.eventName
        binding.startTimeText.text = formatLocalDateTime(longToLocalDateTime(args.startTime.toLong()))
        binding.endTimeText.text = formatLocalDateTime(longToLocalDateTime(args.endTime.toLong()))
        binding.locationText.text = args.location
        binding.isLecturer = (userData1!!.role == "Lecturer")

        binding.buttonReg.setOnClickListener {

            val eventReg = EventReg(user!!.uid.toString(),args.firebaseid.toString())
            CheckIfRegistered(eventReg)
        }
        binding.buttonAlert.setOnClickListener {

            //val eventAlert = Notification(user!!.uid.toString(),args.firebaseid.toString())
            alertRegisteredUsers(args.firebaseid.toString(), args.eventName, args.startTime.toLong())
        }
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
    fun alertRegisteredUsers(event: String, eventname : String, eventdate: Long) {
        var notifId = 0
        createNotification("upcoming_events_channel",
            "COVID ALERT CHECK FOR YOURSELF",
            "Class that covid RECOGNIZED: ${eventname}, at: ${formatLocalDateTime(longToLocalDateTime(eventdate))}",notifId)
        activity?.let { Snackbar.make(it.findViewById(R.id.drawer_layout),"Alert successfully published!", Snackbar.LENGTH_LONG).show()}
        db.collection("eventReg").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.get("eventid")!!.equals(event)) {
                        val data = hashMapOf(
                            "userid" to document.get("userid"),
                            "date" to formatLocalDateTime(LocalDateTime.now()),
                            "text" to "In your class:${eventname}, at ${formatLocalDateTime(longToLocalDateTime(eventdate))} COVID RECOGNIZED!!!"
                        )
                        //if document found
                        db.collection("notifications")
                            .add(data)
                    }
                }
            }
    }
    fun insertEventRegistration(event: EventReg){

        val data = hashMapOf(
            "eventid" to event.eventid,
            "userid" to event.userid
        )

        db.collection("eventReg")
            .add(data)
            .addOnSuccessListener {
                activity?.let { Snackbar.make(it.findViewById(R.id.drawer_layout),"You have registered successfully!", Snackbar.LENGTH_LONG).show()}
            }
    }
    fun CheckIfRegistered(event: EventReg){
        var check = false
        db.collection("eventReg").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.get("eventid")!!.equals(event.eventid) && document.get("userid")!!.equals(event.userid)
                    ) {
                        //if document found
                        check = true
                    }
                }
                if (check == false) {
                    //if document not found
                    insertEventRegistration(event)
                }
                else{
                    activity?.let { Snackbar.make(it.findViewById(R.id.drawer_layout), "You have registered already", Snackbar.LENGTH_LONG).show() }
                }
            }
    }
    private fun createNotification(channelId : String, title: String, subtitle: String, id:Int){
        createNotificationChannel(channelId,"Upcoming events")
        val intent = Intent(requireContext(), HomeFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)

        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.calendar_ic)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val mNotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        with(mNotificationManager) {
            notify(id, builder.build())
        }
    }

    private fun createNotificationChannel(channelId : String,desc:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelId, importance).apply {
                description = desc
            }
            val notificationManager: NotificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}