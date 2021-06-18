package pt.ie.dogwalkingbuddy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.ie.dogwalkingbuddy.api.WeatherResponse
import pt.ie.dogwalkingbuddy.api.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MenuPrincipal : AppCompatActivity() {

    private var weatherData: TextView? = null

    //Notificações
    private val channelId = "12345"
    private val notificationId = 101

    //Localização para obter dados da API
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var locAdd: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)
        try {this.supportActionBar!!.hide()} catch (e: NullPointerException) {}

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val leaderboard = findViewById<Button>(R.id.buttonleaderboard)
        leaderboard.setOnClickListener{
            val intent = Intent(this, pt.ie.dogwalkingbuddy.leaderboard::class.java)
            startActivity(intent)
        }
        val trail = findViewById<Button>(R.id.buttontrail)
        trail.setOnClickListener{
            val intent = Intent(this, TrailActivity::class.java)
            startActivity(intent)
        }
        val timeline = findViewById<Button>(R.id.buttontimeline)
        timeline.setOnClickListener{
            val intent = Intent(this, pt.ie.dogwalkingbuddy.timeline::class.java)
            startActivity(intent)
        }
        val reward = findViewById<Button>(R.id.buttonrewards)
        reward.setOnClickListener{
           val intent = Intent(this, loja::class.java)
           startActivity(intent)
        }
        val walk = findViewById<FloatingActionButton>(R.id.fabwalk)
        walk.setOnClickListener{
            val intent = Intent(this, TrailActivity::class.java)
            startActivity(intent)
        }

        var latNow: Double = 0.0
        var lonNow: Double = 0.0

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                locAdd = loc

                latNow = locAdd.latitude
                lonNow = locAdd.longitude

                //Mostra as coordenadas periodicamente
                Log.d("Coords",loc.latitude.toString() + " - " + loc.longitude.toString())

                getCurrentData(latNow, lonNow)
            }
        }
        createLocationRequest()
    }

    private fun getCurrentData(latNow: Double, lonNow: Double) {

        //TextView
        val showtext = findViewById<TextView>(R.id.wethernow)
        showtext.text = "Fetching..."

        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(latNow.toString(), lonNow.toString(), AppId)

        if (!isNetworkAvailable(this)) {
            showtext.text = "Unavailable"
            return
        }

        // spaghetti to enable offline trail saving (saves pending trails when this activity is called)
        val db = Firebase.firestore
        db.enableNetwork()

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!

                    showtext.setText(weatherResponse.weatherList?.get(0)?.mainstate)

                    Log.d("Coords","FEZ CALL")

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val name = "Teste"
                        val descriptionTeste = "Teste desc"
                        val importance = NotificationManager.IMPORTANCE_DEFAULT

                        val channel = NotificationChannel(channelId,name,importance).apply {
                            description = descriptionTeste
                        }

                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    //Builder da Notificação
                    val builder = NotificationCompat.Builder(this@MenuPrincipal, channelId)
                        .setSmallIcon(R.drawable.baseline_pets_24)
                        .setContentTitle("Watch out! Its raining!")
                        //.setContentText(" " + weatherResponse.weatherList?.get(0)?.mainstate)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    with(NotificationManagerCompat.from(this@MenuPrincipal)) {

                        //Se estiver a chover, trigger da notificação
                        if(weatherResponse.weatherList?.get(0)?.mainstate == "Rain"){
                            notify(notificationId, builder.build())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherData!!.text = t.message
            }
        })


    }

    companion object {
        var BaseUrl = "https://api.openweathermap.org/"
        var AppId = "7aa419cff467343ef52cbf8b59063c08"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setSmallestDisplacement(1000.0F)
        //locationRequest.interval = 300000 //Rate de 5 Minutos
        locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    //Parar de receber Coordenadas
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("Valor:", "Pause")
    }

    //Resumir a receção de coordenadas
    public override fun onResume() {
        super.onResume()
        startLocationUpdates()
        Log.d("Valor:", "Resume")
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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