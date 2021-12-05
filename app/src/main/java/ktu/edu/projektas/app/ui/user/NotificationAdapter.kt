package ktu.edu.projektas.app.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ktu.edu.projektas.app.data.Notification
import ktu.edu.projektas.databinding.NotificationItemBinding


class NotificationAdapter: ListAdapter<Notification, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

    class ViewHolder(private val binding: NotificationItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.root.layoutParams
            binding.notification = notification
        }
    }

    class NotificationDiffCallback: DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.firebaseId == newItem.firebaseId
        }
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            NotificationItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}