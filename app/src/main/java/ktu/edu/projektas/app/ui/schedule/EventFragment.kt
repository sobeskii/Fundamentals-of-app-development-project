package ktu.edu.projektas.app.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.app.utils.*
import ktu.edu.projektas.databinding.FragmentEventBinding

class EventFragment : Fragment() {

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
    ): View? {

        userData1 = viewModel.userData!!

        binding = FragmentEventBinding.inflate(inflater, container, false)

        val args = EventFragmentArgs.fromBundle(requireArguments())

        binding.eventNameText.text = args.eventName
        binding.startTimeText.text = formatLocalDateTime(longToLocalDateTime(args.startTime.toLong()))
        binding.endTimeText.text = formatLocalDateTime(longToLocalDateTime(args.endTime.toLong()))
        binding.locationText.text = args.location
        binding.isLecturer = (userData1!!.role == "Lecturer")

        binding!!.buttonReg.setOnClickListener {
            val db = FirebaseFirestore.getInstance().document("eventReg")
            val eventData = hashMapOf(
                "userid" to 10,
                "eventid" to 11
            )
            db.collection("eventReg")
                .add(eventData)
                .addOnSuccessListener {
                    Log.d("TAG","paejo")
                }
                .addOnFailureListener{
                    Log.d("TAG","nepaejo")
                }
        }
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }
}