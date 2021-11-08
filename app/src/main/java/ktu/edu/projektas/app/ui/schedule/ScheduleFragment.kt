package ktu.edu.projektas.app.ui
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.alamkanak.weekview.jsr310.maxDateAsLocalDate
import com.alamkanak.weekview.jsr310.minDateAsLocalDate
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentScheduleBinding
import java.time.*
import android.view.MenuInflater
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import ktu.edu.projektas.app.data.Event
import ktu.edu.projektas.app.ui.schedule.ScheduleAdapter


class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding

    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null

    private lateinit var spinner : Spinner
    private val adapter =   ScheduleAdapter(clickListener = this::onLongClick,
                                            secondListener = this::onClick)

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
        setHasOptionsMenu(true)
    }
    // Add spinner to top toolbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)

        val item: MenuItem = menu!!.findItem(R.id.spinner)
        spinner = item.actionView as Spinner
        //Fill spinner with color list
        activity?.let {
            ArrayAdapter.createFromResource(
                it.applicationContext,
                R.array.colors, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
        // On selected listener to change data when spinner is changed
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                var selectedItem  = spinner.selectedItem.toString()
                binding.weekView.adapter = null
                // Get events by color
                viewModel.getAllEventsByColor(selectedItem)?.observe(viewLifecycleOwner){
                    adapter.submitList(it)
                }
                binding.weekView.adapter = adapter
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                binding.weekView.adapter = null
                // Reset to normal event data
                viewModel.events.observe(viewLifecycleOwner){
                    adapter.submitList(it)
                }
                binding.weekView.adapter = adapter
            }
        }
        return super.onCreateOptionsMenu(menu!!, inflater!!)
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleBinding.inflate(inflater)

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

    private fun onClick(event: Event) {

        var action = ScheduleFragmentDirections.actionScheduleFragmentToEventFragment(event.title,event.location,event.endTime.toString(),event.startTime.toString())

        view?.findNavController()
            ?.navigate(action)
    }
}