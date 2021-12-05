package ktu.edu.projektas.app.ui.schedule
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.utils.*
import java.time.LocalDate
import java.util.*



class SettingsFragment : PreferenceFragmentCompat() {



    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val btnDateStart: Preference? = findPreference("semester_start") as Preference?
        val btnDateEnd: Preference? = findPreference("semester_end") as Preference?


        val dateStart = convertLongToLocalDate(viewModel.semesterStart)
        btnDateStart?.summary = formatLocalDate(dateStart)

        val dateEnd = convertLongToLocalDate(viewModel.semesterEnd)
        btnDateEnd?.summary = formatLocalDate(dateEnd)


        btnDateStart?.setOnPreferenceClickListener {
            showDateDialog(btnDateStart,true)
            true
        }

        btnDateEnd?.setOnPreferenceClickListener {
            showDateDialog(btnDateEnd,false)
            true
        }

    }
    private fun showDateDialog(prefs : Preference, start : Boolean) {
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireActivity(), { _: DatePicker?,
                                              year1: Int, month1: Int, day1: Int ->
            val date = LocalDate.of(year1, month1 + 1, day1)
            val long = convertLocalDateToLong(date)

            viewModel.updateSemesterDate(long!!,start)

            prefs.summary = formatLocalDate(date)

        }, year, month, day).show()

    }
}