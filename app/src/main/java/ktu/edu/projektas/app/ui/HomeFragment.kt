package ktu.edu.projektas.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter : HomeAdapter
    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = requireContext().getSharedPreferences("ktu.edu.projektas.app", Context.MODE_PRIVATE)
        semesterStart = prefs.getLong("ktu.edu.projektas.app.semester_start", getCurrentMonthFirstDay()?.toEpochMilli()!!)
        semesterEnd = prefs.getLong("ktu.edu.projektas.app.semester_end", getCurrentMonthLastDay()?.toEpochMilli()!!)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter =   HomeAdapter()

        binding.upcomingEventAdapter.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

}