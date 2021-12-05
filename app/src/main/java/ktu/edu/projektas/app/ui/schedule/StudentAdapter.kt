package ktu.edu.projektas.app.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import ktu.edu.projektas.app.data.User
import ktu.edu.projektas.databinding.StudentItemBinding

// home's adapter class
class StudentAdapter(options: FirestoreRecyclerOptions<User>) : FirestoreRecyclerAdapter<User, StudentAdapter.ViewHolder>(options) {

    class ViewHolder(private val binding: StudentItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: User) {
        holder.bind(getItem(position))
    }
}
