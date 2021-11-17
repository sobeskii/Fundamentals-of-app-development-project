package ktu.edu.projektas.app.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.R
import ktu.edu.projektas.databinding.FragmentProfileBinding

// fragment class for user's profile
class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonChange.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_changePwFragment)
        }

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        if(uid.isNotEmpty()){
            readFireStoreData()
        }
        binding.buttonLogout.setOnClickListener {
            logOut()
        }

        return binding.root
    }

    private fun logOut() {
        auth.signOut()
        view?.findNavController()?.navigate(R.id.action_profileFragment_to_loginFragment)
    }

    private fun readFireStoreData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnSuccessListener { documentSnapshot ->
            val firstName = documentSnapshot.getString("firstName")
            val lastName = documentSnapshot.getString("lastName")
            val email = documentSnapshot.getString("email")
            val role = documentSnapshot.getString("role")
            val group = documentSnapshot.getString("group")
            binding.fullname.text = firstName
            binding.lastname.text = lastName
            binding.email.text = email
            binding.layoutRole.text = role
            binding.layoutGroup.text = group
        }
    }
}