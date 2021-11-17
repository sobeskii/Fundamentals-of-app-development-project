package ktu.edu.projektas.app.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ktu.edu.projektas.app.utils.formatLocalDateTime
import ktu.edu.projektas.app.utils.longToLocalDateTime
import ktu.edu.projektas.databinding.FragmentEventBinding

// fragment class for viewing event's details
class EventFragment: Fragment() {

    private lateinit var binding: FragmentEventBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEventBinding.inflate(inflater, container, false)

        val args = EventFragmentArgs.fromBundle(requireArguments())

        binding.eventNameText.text = args.eventName
        binding.startTimeText.text = formatLocalDateTime(longToLocalDateTime(args.startTime.toLong()))
        binding.endTimeText.text = formatLocalDateTime(longToLocalDateTime(args.endTime.toLong()))
        binding.locationText.text = args.location

        binding.button.setOnClickListener{
            var graph: View = binding.green
            val params: ViewGroup.LayoutParams = graph.layoutParams
            if(params.height <= 140){
                params.height += 10
            }

            graph =  binding.green
            graph.layoutParams = params
        }

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}