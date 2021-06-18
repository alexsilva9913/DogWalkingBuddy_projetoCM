package pt.ie.dogwalkingbuddy.TimelineAdapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_timeline.view.*
import pt.ie.dogwalkingbuddy.R
import pt.ie.dogwalkingbuddy.Trails
import pt.ie.dogwalkingbuddy.dataclass.Trilhos
import java.sql.Timestamp
import java.util.*

class TimelineAdapter: RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    private var trilhos: MutableList<Trails> = mutableListOf()

    override fun getItemCount(): Int {
        return trilhos.size
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(trilhos[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    fun addTrails(trilhos: List<Trails>) {
        this.trilhos.apply {
            clear()
            addAll(trilhos)
        }
        notifyDataSetChanged()
    }

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(trilho: Trails, position: Int) {

                val stamp = Timestamp(trilho.seconds_walked.toLong())
                val date = Date(stamp.getTime())
                itemView.text_timeline_title.text = "Walk Done! "

                var input = trilho.seconds_walked

                var hours = input / 3600;
                var minutes = (input % 3600) / 60;
                var seconds = (input % 3600) % 60;

                itemView.text_timeline_date.text = "" + hours + ":" + minutes + ":" + seconds
                itemView.text_timeline_pontos.text = trilho.points_earned.toString()

        }
    }
}