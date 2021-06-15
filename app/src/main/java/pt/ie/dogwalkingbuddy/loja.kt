package pt.ie.dogwalkingbuddy

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth;


class loja : AppCompatActivity() {

    private val TAG = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loja)

        val auth = FirebaseAuth.getInstance()
        val UIDvalor = auth.uid

        getDados(UIDvalor)

    }

    fun getDados(UIDvalor: String?) {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        Log.d("VALORUID", UIDvalor.toString())

        val uidString = UIDvalor.toString()

        Log.d("VALORUID2", uidString)

        val docRef = db.collection("user")
            .document(uidString)

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    Log.d("PONTOS", document.getDouble("pontos").toString()) //Print the name

                    val viewpontos = findViewById<TextView>(R.id.PontosNow)
                    var pontos: Long? = document.getLong("pontos")

                    viewpontos.setText(pontos.toString())

                } else {
                    Log.d( TAG,"No such document")
                }
            } else {
                Log.d(
                    TAG,
                    "get failed with ",
                    task.exception
                )
            }
        }

    }
}