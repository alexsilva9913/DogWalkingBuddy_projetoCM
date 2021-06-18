package pt.ie.dogwalkingbuddy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.content_scrolling.*
import pt.ie.dogwalkingbuddy.adapter.Player
import pt.ie.dogwalkingbuddy.adapter.PlayerAdapter


class leaderboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        try {this.supportActionBar!!.hide()} catch (e: NullPointerException) {}

        setupRecyclerView()
        loadPlayers()
    }

    private fun setupRecyclerView() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PlayerAdapter()
            setHasFixedSize(true)
        }
    }

    private fun loadPlayers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .orderBy("pontos", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.d("TAG", error.message!!)
                    return@addSnapshotListener
                }

                val players = snapshots?.map {
                    it.toObject(Player::class.java)
                }
                val champions = players?.take(3)
                if (players != null) {
                    showPlayersPosition(players)
                    if (champions != null) {
                        if (champions.size > 2) {
                            showChampions(champions)
                        }
                    }
                }
            }
    }

    private fun showPlayersPosition(players: List<Player>) {
        val adapter = recycler_view.adapter as PlayerAdapter
        adapter.addPlayers(players)
    }

    private fun showChampions(championPlayers: List<Player>) {
        Glide.with(applicationContext)
            .load(championPlayers[0].photo)
            .into(iv_champion1)
        Glide.with(applicationContext)
            .load(championPlayers[1].photo)
            .into(iv_champion2)
        Glide.with(applicationContext)
            .load(championPlayers[2].photo)
            .into(iv_champion3)
    }

}