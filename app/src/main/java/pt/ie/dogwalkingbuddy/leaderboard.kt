package pt.ie.dogwalkingbuddy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.content_scrolling.*
import pt.ie.dogwalkingbuddy.adapter.Player
import pt.ie.dogwalkingbuddy.adapter.PlayerAdapter
import pt.ie.dogwalkingbuddy.adapter.loadImg


class leaderboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
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
            .addSnapshotListener({ snapshots, error ->
                if (error != null) {
                    Log.d("TAG", error.message!!)
                    return@addSnapshotListener
                }

                val players = snapshots?.map{
                    it.toObject(Player::class.java)
                }
                val champions = players?.take(3)
                if (players != null) {
                    showPlayersPosition(players)
                    if (champions != null) {
                        showChampions(champions)
                    }
                }
            })
    }

    private fun showPlayersPosition(players: List<Player>) {
        val adapter = recycler_view.adapter as PlayerAdapter
        adapter.addPlayers(players)
    }


    private fun showChampions(championPlayers: List<Player>) {
        iv_champion1.loadImg(championPlayers[0].photo)
        iv_champion2.loadImg(championPlayers[1].photo)
        iv_champion3.loadImg(championPlayers[2].photo)
    }

}