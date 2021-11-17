package ktu.edu.projektas.app.ui.schedule

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
import java.time.*
import android.view.MenuInflater
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import ktu.edu.projektas.app.data.Event
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import ktu.edu.projektas.databinding.FragmentScheduleBinding

// schedule's fragment class
class ScheduleFragment: Fragment() {

    private lateinit var binding: FragmentScheduleBinding

    private var semesterStart: Long? = null
    private var semesterEnd: Long? = null

    private lateinit var spinner: Spinner
    private val adapter = ScheduleAdapter(clickListener = this::onLongClick, secondListener = this::onClick)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
    }

    private val viewModel: ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // adds a drop-down list (a spinner) of colors for event filtering to top toolbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_menu, menu)

        val item: MenuItem = menu.findItem(R.id.spinner)
        spinner = item.actionView as Spinner

        // fills the spinner with a list of colors
        activity?.let {
            ArrayAdapter.createFromResource(
                it.applicationContext,
                R.array.colors, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        // adds onItemSelected listener for changing data (filtering events) when the spinner is changed
        spinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedItem  = spinner.selectedItem.toString()
                binding.weekView.adapter = null

                // gets events by color
                viewModel.getAllEventsByColor(selectedItem).observe(viewLifecycleOwner){
                    adapter.submitList(it)
                }
                binding.weekView.adapter = adapter
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                binding.weekView.adapter = null

                // resets to normal event data
                viewModel.events.observe(viewLifecycleOwner){
                    adapter.submitList(it)
                }
                binding.weekView.adapter = adapter
            }
        }

        val getItemBtn = menu.findItem(R.id.button_query)
        val getItemInput = menu.findItem(R.id.query)
        if (getItemBtn != null && getItemInput != null) {
            val btn = getItemBtn.actionView as AppCompatButton
            val input = getItemInput.actionView as TextInputEditText

            btn.setBackgroundColor(resources.getColor(R.color.orange_900))
            btn.setText(R.string.search_button)
            btn.setTextColor(resources.getColor(R.color.white))

            btn.setOnClickListener{
                val query  = input.text.toString()

                binding.weekView.adapter = null

                viewModel.getAllEventsByQuery(query).observe(viewLifecycleOwner){
                    adapter.submitList(it)
                }
                binding.weekView.adapter = adapter
            }
        }
        return super.onCreateOptionsMenu(menu, inflater)
    }

    // configures schedule's view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    private fun convertLongToLocalDate(value: Long?): LocalDate{
        return Instant.ofEpochMilli(value!!).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun onLongClick(event: Event) {
        AlertDialog.Builder(context)
                .setTitle("Delete event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    viewModel.deleteByGroup(event.groupId)
                }
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    private fun onClick(event: Event) {
        val action = ScheduleFragmentDirections.actionScheduleFragmentToEventFragment(event.title, event.location, event.endTime.toString(), event.startTime.toString())

        view?.findNavController()
            ?.navigate(action)
    }
}