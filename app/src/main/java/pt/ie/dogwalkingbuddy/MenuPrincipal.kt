package pt.ie.dogwalkingbuddy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MenuPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)
        try {this.supportActionBar!!.hide()} catch (e: NullPointerException) {}

        val leaderboard = findViewById<Button>(R.id.buttonleaderboard)
        leaderboard.setOnClickListener{
          //  val intent = Intent(this, /*LeaderBoard*/::class.java)
          //  startActivity(intent)
        }
        val trail = findViewById<Button>(R.id.buttontrail)
        trail.setOnClickListener{
        //    val intent = Intent(this, /*Trail*/::class.java)
          //  startActivity(intent)
        }
        val timeline = findViewById<Button>(R.id.buttontimeline)
        timeline.setOnClickListener{
           // val intent = Intent(this, /*Timeline*/::class.java)
           // startActivity(intent)
        }
        val reward = findViewById<Button>(R.id.buttonrewards)
        reward.setOnClickListener{
           // val intent = Intent(this, /*Reward*/::class.java)
           // startActivity(intent)
        }
        val walk = findViewById<FloatingActionButton>(R.id.fabwalk)
        walk.setOnClickListener{
            val intent = Intent(this, TrailActivity::class.java)
            startActivity(intent)
        }

    }
}