package ktu.edu.projektas.app.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ktu.edu.projektas.app.data.Event
import ktu.edu.projektas.databinding.EventItemBinding

// home's adapter class
class HomeAdapter(val eventClick: (Event) -> Unit): ListAdapter<Event, HomeAdapter.ViewHolder>(EventDiffCallback()) {

    class ViewHolder(private val binding: EventItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.root.layoutParams
            binding.event = event
        }
    }

    class EventDiffCallback: DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            EventItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.itemView.setOnClickListener {
            eventClick(data)
        }

        holder.bind(getItem(position))
    }
}
