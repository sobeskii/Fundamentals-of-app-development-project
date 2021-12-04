package ktu.edu.projektas.app.ui.schedule

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
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
import ktu.edu.projektas.app.utils.*
import ktu.edu.projektas.databinding.FragmentEventBinding

// fragment class for viewing event's details
class EventFragment: Fragment() {

    private lateinit var binding: FragmentEventBinding
    private val user = FirebaseAuth.getInstance().currentUser
    private val db =    FirebaseFirestore.getInstance()
    private lateinit var userData  :   DocumentSnapshot
    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null
    private var userData1 : User? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
    }

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
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
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
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
}