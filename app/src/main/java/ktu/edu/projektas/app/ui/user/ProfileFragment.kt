package ktu.edu.projektas.app.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.databinding.FragmentProfileBinding
import java.lang.ref.PhantomReference
import com.google.firebase.firestore.DocumentSnapshot

import com.google.android.gms.tasks.OnSuccessListener




class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth : FirebaseAuth
    private var fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var dialog: Dialog
    private lateinit var user: User
    private lateinit var uid: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        if(uid.isNotEmpty()){
            readFireStoreData()
        }

        return binding.root
    }
    fun readFireStoreData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnSuccessListener(
            OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                val firstName = documentSnapshot.getString("firstName")
                val lastName = documentSnapshot.getString("lastName")
                val email  = documentSnapshot.getString("email")
                val role  = documentSnapshot.getString("role")
                val group  = documentSnapshot.getString("group")
                binding.fullname.text = firstName
                binding.lastname.text = lastName
                binding.email.text = email
                binding.layoutRole.text = role
                binding.layoutGroup.text = group
            })
    }



}