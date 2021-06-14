package pt.ie.dogwalkingbuddy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User


class leaderboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        loadUsers()

    }

    private fun setupRecyclerView() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PlayerAdapter()
            setHasFixedSize(true)
        }
    }

    private fun loadUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("pontos", Query.Direction.DESCENDING)
            .addSnapshotListener({ snapshots, error ->
                if (error != null) {
                    Log.d("TAG", error.message)
                    return@addSnapshotListener
                }

                val users = snapshots.map{
                    it.toObject(User::class.java)
                }
                //val champions = users.take(3)
                showUsersPosition(users)
            })
    }

    private fun showUsersPosition(players: List<User>) {
        val adapter = recycler.adapter as PlayerAdapter
        adapter.addPlayers(players)
    }

}