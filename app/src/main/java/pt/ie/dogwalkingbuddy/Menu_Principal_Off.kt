package pt.ie.dogwalkingbuddy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Menu_Principal_Off : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu__principal__off)


        val trail = findViewById<Button>(R.id.buttontrail)
        trail.setOnClickListener {
            val intent = Intent(this, TrailActivity::class.java)
            startActivity(intent)


            val walk = findViewById<FloatingActionButton>(R.id.fabwalk)
            walk.setOnClickListener {
                val intent = Intent(this, TrailActivity::class.java)
                startActivity(intent)
            }


        }


    }
}