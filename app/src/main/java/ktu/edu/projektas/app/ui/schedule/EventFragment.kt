package ktu.edu.projektas.app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import ktu.edu.projektas.R
import ktu.edu.projektas.databinding.FragmentEventBinding
import ktu.edu.projektas.databinding.FragmentHomeBinding
import ktu.edu.projektas.databinding.FragmentScheduleBinding


class EventFragment : Fragment() {

    private lateinit var binding: FragmentEventBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEventBinding.inflate(inflater, container, false)

        val args = EventFragmentArgs.fromBundle(requireArguments())

        binding.eventNameText.text = args.eventName
        binding.startTimeText.text = args.startTime
        binding.locationText.text = args.location

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }
}