package ktu.edu.projektas.app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.app.utils.formatLocalDate
import ktu.edu.projektas.app.utils.formatLocalDateTime
import ktu.edu.projektas.app.utils.longToLocalDateTime
import ktu.edu.projektas.databinding.FragmentEventBinding

class EventFragment : Fragment() {

    private lateinit var binding: FragmentEventBinding
    private val user = FirebaseAuth.getInstance().currentUser
    private val db =    FirebaseFirestore.getInstance()
    private lateinit var userData   :   DocumentSnapshot

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

        binding = FragmentEventBinding.inflate(inflater, container, false)

        val args = EventFragmentArgs.fromBundle(requireArguments())

        binding.eventNameText.text = args.eventName
        binding.startTimeText.text = formatLocalDateTime(longToLocalDateTime(args.startTime.toLong()))
        binding.endTimeText.text = formatLocalDateTime(longToLocalDateTime(args.endTime.toLong()))
        binding.locationText.text = args.location

        binding.button.setOnClickListener{
            var graph: View = binding.green
            var params: ViewGroup.LayoutParams = graph.layoutParams
            if(params.height <= 140){
                params.height += 10
            }

            graph =  binding.green;
            graph.layoutParams = params;
        }


        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }
}