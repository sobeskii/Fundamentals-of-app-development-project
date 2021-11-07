package ktu.edu.projektas.app.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.databinding.FragmentProfileBinding
import java.lang.ref.PhantomReference
import com.google.firebase.firestore.DocumentSnapshot

import com.google.android.gms.tasks.OnSuccessListener
import ktu.edu.projektas.databinding.FragmentChangepwBinding


class ChangePwFragment : Fragment() {

    private lateinit var binding: FragmentChangepwBinding
    private lateinit var auth : FirebaseAuth
    private var fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var dialog: Dialog
    private lateinit var user: User
    private lateinit var uid: String
    private lateinit var mAuth : FirebaseAuth


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentChangepwBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}