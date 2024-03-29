package pt.ie.dogwalkingbuddy.api

import com.google.gson.annotations.SerializedName

class WeatherResponse {

    @SerializedName("sys")
    var sys: Sys? = null

    @SerializedName("weather")
    //var weather: Weather? = null
    var weatherList: List<Weather>? = null

    @SerializedName("main")
    var main: Main? = null
}

class Weather {
    @SerializedName("id")
    var id: Float = 0.0f
    @SerializedName("main")
    var mainstate: String = ""
}

class Main {
    @SerializedName("temp")
    var temp: Float = 0.0f
    @SerializedName("humidity")
    var humidity: Float = 0.0f
    @SerializedName("pressure")
    var pressure: Float = 0.0f
    @SerializedName("temp_min")
    var temp_min: Float = 0.0f
    @SerializedName("temp_max")
    var temp_max: Float = 0.0f
}

class Sys {
    @SerializedName("country")
    var country: String? = null
}