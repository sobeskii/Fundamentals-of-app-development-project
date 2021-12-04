package ktu.edu.projektas.app.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.app.utils.*
import ktu.edu.projektas.databinding.FragmentCreateEventBinding
import java.sql.Time
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

// fragment class for creating events
class CreateEventFragment: Fragment() {

    private lateinit var binding: FragmentCreateEventBinding

    private val defaultButtonTintColor = "#1B1717"
    private val onFormValidButtonTintColor = "#4F774F"

    private val date = MutableStateFlow("")
    private val startTime = MutableStateFlow("")
    private val duration = MutableStateFlow("")
    private val event = MutableStateFlow("")
    private val location = MutableStateFlow("")
    private var errorMessage: String? = null
    private lateinit var userData: User

    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
        userData = viewModel.userData!!
    }

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
    }
    private val formIsValid = combine(
            date,
            startTime,
            duration,
            event,
            location
    ) { date, startTime, duration, event, location ->
        binding.txtErrorMessage.text = ""

        val valid       =   dateIsValid(date)
        val longDate    =   convertLocalDateToLong(valid)

        val startTimeValues = startTime.split(":")

        val dateIsValid =   valid != null && longDate!! <= semesterEnd!! && longDate >= semesterStart!!
        val duration    =   duration.length in 1..3 && duration.toInt() <= 300 && duration.toInt() >= 60
        val startTimeIsValid =  startTimeValues[0].length in 1..2 &&
                                startTimeValues[0].toInt() <= 19 &&
                                startTimeValues[0].toInt() >= 8

        val event = event.length < 30 && event.isNotEmpty()
        val location = location.length < 30 && location.isNotEmpty()

        errorMessage = when {
            dateIsValid.not() -> "Date is invalid"
            startTimeIsValid.not() -> "Start time is invalid - event has to take place between 8:00 and 19:00"
            duration.not() -> "Duration is invalid - event has to last from 60 to 300 minutes"
            event.not() -> "Event title is invalid"
            location.not() -> "Location is invalid"

            else -> null
        }
        errorMessage?.let {
            if(date.isNotEmpty()) {
                binding.txtErrorMessage.text = it
            }
        }
        dateIsValid and duration and startTimeIsValid and event and location
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        binding.isLecturer = (userData.role == "Lecturer")
        // for selecting event colors from a drop-down list
        val spinner: Spinner = binding.selectEventColors
        ArrayAdapter.createFromResource(
                activity?.applicationContext!!,
                R.array.colors,
                android.R.layout.simple_list_item_1
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        binding.startTimeInput.isFocusable = false
        binding.startTimeInput.setOnClickListener{
            setTimeFromTimePicker(context, binding.startTimeInput)
        }

        binding.selectDayInput.isFocusable = false
        binding.selectDayInput.setOnClickListener{
            setDateFromDatePicker(context, binding.selectDayInput)
        }

        with(binding) {
            selectDayInput.doOnTextChanged { text, _, _, _ ->
                date.value = text.toString()
            }
            startTimeInput.doOnTextChanged { text, _, _, _ ->
                startTime.value = text.toString()
            }
            eventDurationInput.doOnTextChanged { text, _, _, _ ->
                duration.value = text.toString()
            }
            eventNameInput.doOnTextChanged { text, _, _, _ ->
                event.value = text.toString()
            }
            locationInput.doOnTextChanged { text, _, _, _ ->
                location.value = text.toString()
            }
        }

        val snackBar = activity?.let { Snackbar.make(it.findViewById(R.id.drawer_layout), "Event added!", Snackbar.LENGTH_LONG) }

        // button's OnClick listener
        binding.createEventBtn.setOnClickListener {
            if (snackBar != null) {
                snackBar.show()
                viewModel.addEvent(date.value,
                        startTime.value,
                        duration.value,
                        event.value,
                        spinner.selectedItem.toString(),
                        location.value)
                binding.selectDayInput.text.clear()
                binding.eventDurationInput.text.clear()
                binding.startTimeInput.text.clear()
                binding.eventNameInput.text.clear()
                binding.locationInput.text.clear()
            }
        }

        // for getting to massAddEvents
        binding.openMassEventsButton.setOnClickListener{
            view?.findNavController()?.navigate(R.id.action_createEventFragment_to_massAddEvents)
        }

        lifecycleScope.launch {
            formIsValid.collect {
                binding.createEventBtn.apply {
                    backgroundTintList = ColorStateList.valueOf(
                            Color.parseColor(
                                    if (it) onFormValidButtonTintColor else defaultButtonTintColor
                            )
                    )
                    isClickable = it
                }
            }
        }
        return binding.root
    }

    // configures date picker
    private fun setDateFromDatePicker(context: Context?, editText: EditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = context?.let {
            DatePickerDialog(
                    it,
                    { _: DatePicker?, year1: Int, month1: Int, day1: Int ->
                        val date = LocalDate.of(year1, month1 + 1, day1)
                        editText.setText(formatLocalDate(date))
                    }, year, month, day
            )
        }
        dpd?.show()
    }

    // configures time picker
    private fun setTimeFromTimePicker(context: Context?, editText: EditText) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val dpd = context?.let {
            TimePickerDialog(
                    it,
                    { _: TimePicker?, hour1: Int, minute1: Int ->
                        val time = Time(hour1, minute1, 0)
                        editText.setText(formatTime(time))
                    }, hour, minute, true
            )
        }
        dpd?.show()
    }

    private fun dateIsValid(dateStr: String): LocalDate? {
        try{
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception){
            return null
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
    }
}





