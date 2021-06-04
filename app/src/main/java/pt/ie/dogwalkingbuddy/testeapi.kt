package pt.ie.dogwalkingbuddy

import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pt.ie.dogwalkingbuddy.api.WeatherResponse
import pt.ie.dogwalkingbuddy.api.WeatherService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


@Suppress("DEPRECATION")
class testeapi : AppCompatActivity() {

    private var weatherData: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testeapi)

        weatherData = findViewById(R.id.textView)

        findViewById<View>(R.id.button).setOnClickListener { getCurrentData() }
    }


    private fun getCurrentData() {

        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!

                    val stringBuilder = Html.fromHtml("<b>País:</b> " +
                            weatherResponse.sys!!.country +
                            "<br>" +
                            "<b>Temperatura:</b> " +
                            (weatherResponse.main!!.temp - 273).toString().substring(0,3) + " ºC" +
                            "<br>" +
                            "<b>Temperatura(Min):</b> " +
                            (weatherResponse.main!!.temp_min - 273).toString().substring(0,3) + " ºC" +
                            "<br>" +
                            "<b>Temperatura(Max):</b> " +
                            (weatherResponse.main!!.temp_max - 273).toString().substring(0,3) + " ºC" +
                            "<br>" +
                            "<b>Humedad:</b> " +
                            weatherResponse.main!!.humidity +
                            "<br>" +
                            "<b>Tempo Agora:</b> " +
                            weatherResponse.Weather!!.mainstate)

                    weatherData!!.text = stringBuilder
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherData!!.text = t.message
            }
        })

    }

    companion object {

        var BaseUrl = "http://api.openweathermap.org/"
        var AppId = "7aa419cff467343ef52cbf8b59063c08"
        var lat = "40.4167"
        var lon = "-3.7036"
    }

}