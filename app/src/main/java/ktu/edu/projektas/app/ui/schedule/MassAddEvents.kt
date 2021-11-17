package ktu.edu.projektas.app.ui.schedule

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.formatTime
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentMassAddEventsBinding
import java.sql.Time
import java.util.*

// fragment class for adding recurrent events
class MassAddEvents: Fragment() {
    private lateinit var binding: FragmentMassAddEventsBinding

    private val defaultButtonTintColor = "#1B1717"
    private val onFormValidButtonTintColor = "#4F774F"

    private val weekDay = MutableStateFlow("")
    private val startTime = MutableStateFlow("")
    private val duration = MutableStateFlow("")
    private val event = MutableStateFlow("")
    private val location = MutableStateFlow("")

    private var errorMessage: String? = null

    private lateinit var prefs: SharedPreferences
    private var semesterStart: Long? = null
    private var semesterEnd: Long? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = requireContext().getSharedPreferences("ktu.edu.projektas.app", Context.MODE_PRIVATE)
        semesterStart = prefs.getLong("ktu.edu.projektas.app.semester_start", getCurrentMonthFirstDay()?.toEpochMilli()!!)
        semesterEnd = prefs.getLong("ktu.edu.projektas.app.semester_end", getCurrentMonthLastDay()?.toEpochMilli()!!)
    }

    private val viewModel: ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext(), semesterStart!!, semesterEnd!!)
    }

    private val formIsValid = combine(
        weekDay,
        startTime,
        duration,
        event,
        location
    ) { weekDay, startTime, duration, event, location ->
        binding.txtErrorMessageMass.text = ""

        val startTimeValues = startTime.split(":")

        val weekDayValid        =       weekDay.length == 1 && weekDay.toInt() > 0 && weekDay.toInt() <= 7
        val duration            =       duration.length in 1..3 && duration.toInt() <= 300 && duration.toInt() >= 60
        val startTimeIsValid    =       startTimeValues[0].length in 1..2 &&
                startTimeValues[0].toInt() <= 19 &&
                startTimeValues[0].toInt() >= 8
        val event               =       event.length < 30 && event.isNotEmpty()
        val location            =       location.length < 30 && location.isNotEmpty()

        errorMessage = when {
            weekDayValid.not() -> "Day of the week is invalid - has to be expressed as number from 1 to 7"
            startTimeIsValid.not() -> "Start time is invalid - event has to take place between 8:00 and 19:00"
            duration.not() -> "Duration is invalid - event has to last from 60 to 300 minutes"
            event.not() -> "Event title is invalid"
            location.not() -> "Location is invalid"

            else -> null
        }
        errorMessage?.let {
            if(weekDay.isNotEmpty()) {
                binding.txtErrorMessageMass.text = it
            }
        }
        weekDayValid and duration and startTimeIsValid and event and location
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMassAddEventsBinding.inflate(inflater, container, false)

        // for selecting event colors from a drop-down list
        val colorSpinner: Spinner = binding.selectColors
        ArrayAdapter.createFromResource(
            activity?.applicationContext!!,
            R.array.colors,
            android.R.layout.simple_list_item_1
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            colorSpinner.adapter = adapter
        }

        val evenOddSpinner: Spinner = binding.onWhichWeekInput
        ArrayAdapter.createFromResource(
            activity?.applicationContext!!,
            R.array.even_odd_options,
            android.R.layout.simple_list_item_1
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            evenOddSpinner.adapter = adapter
        }

        // for selecting time through time picker
        binding.startTimeMassInput.isFocusable = false
        binding.startTimeMassInput.setOnClickListener{
            setTimeFromTimePicker(context, binding.startTimeMassInput)
        }

        // for adding parameters through text input
        with(binding) {
            eventDayOfWeek.doOnTextChanged { text, _, _, _ ->
                weekDay.value = text.toString()
            }
            startTimeMassInput.doOnTextChanged { text, _, _, _ ->
                startTime.value = text.toString()
            }
            eventDurationMassInput.doOnTextChanged { text, _, _, _ ->
                duration.value = text.toString()
            }
            eventNameMassInput.doOnTextChanged { text, _, _, _ ->
                event.value = text.toString()
            }
            locationMassInput.doOnTextChanged { text, _, _, _ ->
                location.value = text.toString()
            }
        }

        val snackBar = activity?.let { Snackbar.make(it.findViewById(R.id.drawer_layout), "Events added!", Snackbar.LENGTH_LONG) }

        // button's OnClick listener
        binding.massAddBtn.setOnClickListener {
            if (snackBar != null) {
                snackBar.show()
                viewModel.massAddEvents(
                    weekDay.value,
                    startTime.value,
                    duration.value,
                    event.value,
                    colorSpinner.selectedItem.toString(),
                    location.value,
                    evenOddSpinner.selectedItem.toString()
                )

                binding.eventDayOfWeek.text.clear()
                binding.eventDurationMassInput.text.clear()
                binding.startTimeMassInput.text.clear()
                binding.eventNameMassInput.text.clear()
                binding.locationMassInput.text.clear()
            }
        }

        lifecycleScope.launch {
            formIsValid.collect {
                binding.massAddBtn.apply {
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
}