package ktu.edu.projektas.app.ui.schedule
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color.BLACK
import android.graphics.Color.LTGRAY
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
import java.time.*
import android.view.MenuInflater
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import ktu.edu.projektas.app.data.Event
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.databinding.FragmentScheduleBinding



class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding

    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var userData  : User

    private lateinit var spinner : Spinner
    private val adapter =   ScheduleAdapter(clickListener = this::onLongClick,
                                            secondListener = this::onClick)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userData = viewModel.userData!!
    }

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext())
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

        val getItemBtn = menu.findItem(R.id.button_query)
        val getItemInput = menu.findItem(R.id.query)
        if (getItemBtn != null && getItemInput != null) {
            val btn = getItemBtn.actionView as AppCompatButton
            val input = getItemInput.actionView as TextInputEditText

            btn.setBackgroundColor(resources.getColor(R.color.teal_regular))
            btn.text = "Search"
            btn.setTextColor(resources.getColor(R.color.white))
            btn.width = 10

            btn.setOnClickListener{

                var query  = input.text.toString()

                binding.weekView.adapter = null

                // Get events by color
                viewModel.getAllEventsByQuery(query)?.observe(viewLifecycleOwner){
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
        binding.weekView.maxHour  = 24

        binding.weekView.numberOfVisibleDays = 7
        binding.weekView.minDateAsLocalDate = convertLongToLocalDate(viewModel.semesterStart)
        binding.weekView.maxDateAsLocalDate = convertLongToLocalDate(viewModel.semesterEnd)

        binding.weekView.showFirstDayOfWeekFirst
        binding.weekView.eventTextSize = 30

        binding.weekView.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        binding.addEvent.setOnClickListener{
            view?.findNavController()?.navigate(R.id.action_scheduleFragment_to_createEventFragment)
        }

        binding.settingsBtn.visibility = if(userData!!.role == "Lecturer") View.VISIBLE else View.GONE

        binding.settingsBtn.setOnClickListener{
            view?.findNavController()?.navigate(R.id.action_scheduleFragment_to_settingsFragment)
        }



        return binding.root
    }

    private fun convertLongToLocalDate(value: Long?) : LocalDate{
        return Instant.ofEpochMilli(value!!).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    private fun onLongClick(event: Event) {
        if(event.userUUID == user!!.uid) {
            AlertDialog.Builder(context)
                .setTitle("Delete event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    if (event.groupId != 0) {
                        viewModel.deleteByGroup(event.groupId)
                    } else {
                        viewModel.deleteByFirebaseId(event.firebaseId)
                    }
                }
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    private fun onClick(event: Event) {

        var action = ScheduleFragmentDirections.actionScheduleFragmentToEventFragment(event.title,event.location,event.endTime.toString(),event.startTime.toString(),event.firebaseId)

        view?.findNavController()
            ?.navigate(action)
    }
}