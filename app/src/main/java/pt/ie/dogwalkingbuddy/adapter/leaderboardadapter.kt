package pt.ie.dogwalkingbuddy.adapter

import android.text.method.TextKeyListener.clear
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ie.dogwalkingbuddy.R


class leaderboardadapter : RecyclerView.Adapter<leaderboardadapter.UserViewHolder>() {

    private var users: MutableList<User> = mutableListOf()

    override fun getItemCount(): Int = users.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_leaderboard_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    fun addUsers(users: List<User>) {
        this.users.apply {
            clear()
            addAll(users)
        }
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User, position: Int) {
            itemView.positionitem.text = (position + 1).toString()
            itemView.pontos.text = user.pontos
        }
    }
}