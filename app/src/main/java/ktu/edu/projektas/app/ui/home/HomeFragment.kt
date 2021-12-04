package ktu.edu.projektas.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import ktu.edu.projektas.R
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.app.utils.formatLocalDateTime
import ktu.edu.projektas.app.utils.getCurrentMonthFirstDay
import ktu.edu.projektas.app.utils.getCurrentMonthLastDay
import ktu.edu.projektas.app.utils.longToLocalDateTime
import ktu.edu.projektas.databinding.FragmentHomeBinding
import com.google.firebase.firestore.*
import ktu.edu.projektas.app.ui.home.HomeAdapter


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter : HomeAdapter


    private val viewModel : ScheduleViewModel by activityViewModels {
        ScheduleViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter =   HomeAdapter()

        viewModel.upcomingEvents.observe(viewLifecycleOwner, Observer { list ->
            if(list.isNotEmpty()) {
                setVisible(true)
                var notifId = 0
                for (event in list) {
                    createNotification("upcoming_events_channel",
                        "Upcoming events",
                        "You have upcoming: ${event.title} ${formatLocalDateTime(longToLocalDateTime(event.startTime))}",notifId)
                    notifId +=1
                }
            } else setVisible(false)

            adapter.submitList(list)
        })
        binding.upcomingEventAdapter.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


    private fun setVisible(boolean: Boolean){
        binding.upcomingEventAdapter.visibility = if(boolean) View.VISIBLE else View.GONE
        binding.emptyView.visibility = if(boolean) View.GONE else View.VISIBLE
    }

    private fun createNotification(channelId : String, title: String, subtitle: String, id:Int){
        createNotificationChannel(channelId,"Upcoming events")
        val intent = Intent(requireContext(), HomeFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)

        var builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.calendar_ic)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val mNotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        with(mNotificationManager) {
            notify(id, builder.build())
        }
    }

    private fun createNotificationChannel(channelId : String,desc:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelId, importance).apply {
                description = desc
            }
            val notificationManager: NotificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}