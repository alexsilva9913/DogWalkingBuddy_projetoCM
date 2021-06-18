package pt.ie.dogwalkingbuddy

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import pt.ie.dogwalkingbuddy.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    //constantes
    private companion object {
        private const val RC_SIGN_IN = 100
        private const val  TAG = "GOOGLE_SIGN_IN_TAG"
    }
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Google signin configure
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        val offlinemenu = findViewById<Button>(R.id.buttonOffline)











        offlinemenu.setOnClickListener{
            val intent = Intent(this, pt.ie.dogwalkingbuddy.Menu_Principal_Off::class.java)
            startActivity(intent)
        }

        //Google SignIn
        binding.buttonGoogle.setOnClickListener {
            //begin google signup
            Log.d(TAG,"onCreate: begin google sign in")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun checkUser() {
        if( isNetworkAvailable(this)==true) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // start profile acivity
                //startActivity(Intent(this@MainActivity, MenuPrincipal::class.java))
            //    finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== RC_SIGN_IN){
            Log.d(TAG,"onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                //google sign in com sucesso agora fazer auth co firebase
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }catch (e: Exception){
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
    Log.d(TAG,"firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d(TAG,"firebaseAuthWithGoogleAccount: LoggedIn")

                //get logged in user
                val firebaseUser = firebaseAuth.currentUser

                //get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                Log.d(TAG,"firebaseAuthWithGoogleAccount: UID: ${uid}")
                Log.d(TAG,"firebaseAuthWithGoogleAccount: EMAIL: ${email}")


                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    //user is new
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Account created ...... \n ${email}")
                    Toast.makeText(this@MainActivity,"Account created ...... \n ${email}",Toast.LENGTH_SHORT).show()
                    // Create a new user with a first and last name
                    val user = hashMapOf(
                        "pontos" to 0,
                        "name" to "DefaultName",
                        "photo" to "https://firebasestorage.googleapis.com/v0/b/commov-ed043.appspot.com/o/Screenshot_1.png?alt=media&token=8c7fc37f-74f9-4d9e-9f57-ae3685be9eac"
                    )

                    db.collection("user").document(uid)
                        .set(user, SetOptions.merge())
                }else{
                    //existing user
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing User ...... \n ${email}")
                    Toast.makeText(this@MainActivity,"Welcome back ...... \n ${email}",Toast.LENGTH_SHORT).show()
                }
                // start profile acivity
                startActivity(Intent(this@MainActivity,MenuPrincipal::class.java))
                finish()
            }
            .addOnFailureListener{
                e ->
                //login failed
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Loggin failed due to ${e.message}")
                Toast.makeText(this@MainActivity,"firebaseAuthWithGoogleAccount: Login failed due to ...... \n ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }


    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
}