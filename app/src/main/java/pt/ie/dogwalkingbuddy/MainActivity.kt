package pt.ie.dogwalkingbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import pt.ie.dogwalkingbuddy.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    //constantes
    private companion object{
        private const val RC_SIGN_IN = 100
        private const val  TAG = "GOOGLE_SIGN_IN_TAG"
    }

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


        //Google SignIn
        binding.buttonGoogle.setOnClickListener {
            //begin google signup
            Log.d(TAG,"onCreate: begin google sign in")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null) {
            // start profile acivity
//            startActivity(Intent(this@MainActivity, MainMenu::class.java))
  //          finish()
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
                }else{
                    //existing user
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing User ...... \n ${email}")
                    Toast.makeText(this@MainActivity,"Welcome back ...... \n ${email}",Toast.LENGTH_SHORT).show()
                }
                // start profile acivity
                startActivity(Intent(this@MainActivity,MainMenu::class.java))
                finish()
            }
            .addOnFailureListener{
                e ->
                //login failed
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Loggin failed due to ${e.message}")
                Toast.makeText(this@MainActivity,"firebaseAuthWithGoogleAccount: Login failed due to ...... \n ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}