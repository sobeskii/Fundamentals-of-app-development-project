package ktu.edu.projektas.app.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentHomeBinding

// home's fragment class
class HomeFragment: Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter
    private var semesterStart: Long? = null
    private var semesterEnd: Long? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
    }

    private val viewModel: ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter = HomeAdapter()

        viewModel.upcomingEvents.observe(viewLifecycleOwner, { list ->
            if(list.isNotEmpty()) {
                setVisible(true)
            } else setVisible(false)
            adapter.submitList(list)
        })
        binding.upcomingEventAdapter.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun setVisible(boolean: Boolean) {
        binding.upcomingEventAdapter.visibility = if(boolean) View.VISIBLE else View.GONE
        binding.emptyView.visibility = if(boolean) View.GONE else View.VISIBLE
    }
}