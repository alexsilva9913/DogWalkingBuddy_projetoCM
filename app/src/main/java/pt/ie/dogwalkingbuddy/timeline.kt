package pt.ie.dogwalkingbuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import pt.ie.dogwalkingbuddy.TimelineAdapter.TimelineAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_timeline.*

class timeline : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        try {this.supportActionBar!!.hide()} catch (e: NullPointerException) {}

        setupRecyclerView()

        loadPlayers()
    }

    private fun setupRecyclerView() {
        reciclerviewtimeline.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TimelineAdapter()
            setHasFixedSize(true)
        }
    }

    private fun loadPlayers() {
        val db = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val UIDvalor = auth.uid

        val trailsusers = db.collection("trails")
            //.orderBy("time_started_unix", Query.Direction.ASCENDING)

        trailsusers.whereEqualTo("user", UIDvalor.toString())
            .addSnapshotListener({ snapshots, error ->
                if (error != null) {
                    Log.d("TAG", error.message!!)
                    return@addSnapshotListener
                }
                val trilho = snapshots?.map{
                    it.toObject(Trails::class.java)
                }
                if (trilho != null) {
                    showPlayersPosition(trilho)
                }
            })

    }

    private fun showPlayersPosition(trilhos: List<Trails>) {
        val adapter = reciclerviewtimeline.adapter as TimelineAdapter
        adapter.addTrails(trilhos)
    }

}
