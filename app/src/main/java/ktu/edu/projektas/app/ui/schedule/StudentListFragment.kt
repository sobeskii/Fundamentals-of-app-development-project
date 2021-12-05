package ktu.edu.projektas.app.ui.schedule

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.getField
import ktu.edu.projektas.app.data.*
import ktu.edu.projektas.app.ui.home.HomeAdapter
import ktu.edu.projektas.app.ui.home.StudentAdapter
import ktu.edu.projektas.app.utils.formatLocalDateTime
import ktu.edu.projektas.app.utils.longToLocalDateTime
import ktu.edu.projektas.databinding.FragmentStudentListBinding
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class StudentListFragment : Fragment() {

    private lateinit var binding: FragmentStudentListBinding
    private var adapter: StudentAdapter? = null
    private var list : MutableList<User>? = null
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =   FragmentStudentListBinding.inflate(inflater, container, false)
        binding.studentList.layoutManager = LinearLayoutManager(context)
        binding.studentList.setHasFixedSize(true)

        val args = StudentListFragmentArgs.fromBundle(requireArguments())

        val fdb = FirebaseFirestore.getInstance()

        val query = fdb.collection("users")

        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java).build()

        adapter = StudentAdapter(options)

        binding.studentList.adapter = adapter


        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }



    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()

    }

}
