package ktu.edu.projektas.app.ui.schedule

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.databinding.StudentItemBinding

// home's adapter class
class StudentAdapter1: ListAdapter<User, StudentAdapter1.ViewHolder>(UserDiffCallback()) {

    class ViewHolder(private val binding: StudentItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.root.layoutParams
            binding.user = user

            Log.d("aaa",user.role)

        }
    }

    class UserDiffCallback: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.firebaseId == newItem.firebaseId
        }
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            StudentItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
