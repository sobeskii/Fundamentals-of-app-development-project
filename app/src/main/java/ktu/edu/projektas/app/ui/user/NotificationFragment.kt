package ktu.edu.projektas.app.ui.user

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.app.ui.home.HomeAdapter
import ktu.edu.projektas.app.utils.formatLocalDateTime
import ktu.edu.projektas.app.utils.longToLocalDateTime
import ktu.edu.projektas.databinding.FragmentHomeBinding
import ktu.edu.projektas.databinding.FragmentNotificationBinding
import android.content.Intent
import android.net.Uri
import com.facebook.share.model.ShareContent
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.ShareMediaContent
import com.facebook.share.model.SharePhoto
import android.R
import android.content.ContentValues.TAG
import android.util.Log

import com.facebook.share.widget.ShareButton

import com.facebook.share.Sharer

import com.facebook.FacebookCallback
import com.facebook.share.widget.ShareDialog
import com.facebook.FacebookException










class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var auth : FirebaseAuth
    private var fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var dialog: Dialog
    private lateinit var user: User
    private lateinit var uid: String
    private lateinit var mAuth : FirebaseAuth
    private lateinit var adapter : NotificationAdapter

    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext())
    }




    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mAuth = FirebaseAuth.getInstance()
        binding = FragmentNotificationBinding.inflate(inflater, container, false)





        binding.button2.setOnClickListener{
            var message = "COVID OUTBREAK RECOGNIZED"
            var share : Intent = Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(share, "Title of the dialog the system will open"))
        }



        adapter = NotificationAdapter()

        viewModel.notifications.observe(viewLifecycleOwner, Observer { list ->
            if(list.isNotEmpty()) {
                setVisible(true)
            } else setVisible(false)
            adapter.submitList(list)
        })

        binding.notificationAdapter.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner





        return binding.root
    }





    private fun setVisible(boolean: Boolean){
        binding.notificationAdapter.visibility = if(boolean) View.VISIBLE else View.GONE
        binding.emptyView2.visibility = if(boolean) View.GONE else View.VISIBLE
    }

}