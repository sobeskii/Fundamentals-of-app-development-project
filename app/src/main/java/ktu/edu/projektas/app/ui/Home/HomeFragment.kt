package ktu.edu.projektas.app.ui.Home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter : HomeAdapter
    private var semesterStart : Long? = null
    private var semesterEnd : Long? = null

    /*
    *   When fragment is added on to the activity add semester start time and semester end time
    *
    * */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        semesterStart = getCurrentMonthFirstDay()?.toEpochMilli()!!
        semesterEnd = getCurrentMonthLastDay()?.toEpochMilli()!!
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  Bindings are activated in layour files.
        //  Press on ALT+Enter while having <layout> selected and press the "Convert to databinding layout" button
        //  Then you can use the Fragment<fragmentname>Binding import
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        //  Adapter (list) declaration
        adapter =   HomeAdapter()

        // Bind adapter to the UI adapter element
        binding.upcomingEventAdapter.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

}