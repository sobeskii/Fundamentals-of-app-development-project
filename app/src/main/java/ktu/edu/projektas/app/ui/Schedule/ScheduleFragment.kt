package ktu.edu.projektas.app.ui.Schedule
import ktu.edu.projektas.app.ui.Schedule.ScheduleAdapter
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.alamkanak.weekview.jsr310.maxDateAsLocalDate
import com.alamkanak.weekview.jsr310.minDateAsLocalDate
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.Event
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentScheduleBinding
import java.time.*

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding

    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
    }

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleBinding.inflate(inflater)

        val adapter =   ScheduleAdapter(clickListener = this::onLongClick)

        viewModel.events.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        binding.weekView.minHour  = 8
        binding.weekView.maxHour  = 20

        binding.weekView.numberOfVisibleDays = 7
        binding.weekView.minDateAsLocalDate = convertLongToLocalDate(semesterStart)
        binding.weekView.maxDateAsLocalDate = convertLongToLocalDate(semesterEnd)

        binding.weekView.showFirstDayOfWeekFirst

        binding.weekView.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        binding.addEvent.setOnClickListener{
            view?.findNavController()?.navigate(R.id.action_scheduleFragment_to_createEventFragment)
        }


        return binding.root
    }

    private fun convertLongToLocalDate(value: Long?) : LocalDate{
        return Instant.ofEpochMilli(value!!).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    private fun onLongClick(event: Event) {
        AlertDialog.Builder(context)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    viewModel.deleteByGroup(event.groupId)
                }
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }
}