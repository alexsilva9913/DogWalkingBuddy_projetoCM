package pt.ie.dogwalkingbuddy.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ie.dogwalkingbuddy.R
import pt.ie.dogwalkingbuddy.TrailActivity
import pt.ie.dogwalkingbuddy.other.Constants.ACTION_SHOW_TRACKING_ACTIVITY
import pt.ie.dogwalkingbuddy.other.Constants.ACTION_START_OR_RESUME_SERVICE
import pt.ie.dogwalkingbuddy.other.Constants.ACTION_STOP_SERVICE
import pt.ie.dogwalkingbuddy.other.Constants.NOTIFICATION_CHANNEL_ID
import pt.ie.dogwalkingbuddy.other.Constants.NOTIFICATION_CHANNEL_NAME
import pt.ie.dogwalkingbuddy.other.Constants.NOTIFICATION_ID
import pt.ie.dogwalkingbuddy.other.Constants.TIMER_UPDATE_INTERVAL
import pt.ie.dogwalkingbuddy.other.TrackingUtility
import kotlin.math.floor
import kotlin.math.roundToLong

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isFirstRun = true
    private var isTimerEnabled = false
    private var timeWalked = 0L
    private var lastSecondTimestamp = 0L

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
        val totalDistanceWalked = MutableLiveData<Double>()
        val totalPointsEarned = MutableLiveData<Long>()
        val currentSpeed = MutableLiveData<Double>()
        var timeStarted = 0L
        var lapTime = 0L
    }

    private fun startTracking() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeWalked + lapTime)
                // 1 Second has passed
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    if (pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().size > 1) {
                        val secondToLastLatLng =
                            pathPoints.value!!.last()[pathPoints.value!!.last().size - 2]
                        val lastLatLng = pathPoints.value!!.last().last()
                        val lastDistance = TrackingUtility.meterDistanceBetweenPoints(
                            secondToLastLatLng.latitude,
                            secondToLastLatLng.longitude,
                            lastLatLng.latitude,
                            lastLatLng.longitude
                        )
                        // Verifica se a ultima distancia é maior a 1.5 metros ou não-NaN
                        if (!lastDistance.isNaN() && lastDistance >= 1.5) {
                            totalDistanceWalked.postValue(totalDistanceWalked.value!! + lastDistance)
                            if (currentSpeed.value!! < 18.0) {
                                totalPointsEarned.postValue(floor(totalDistanceWalked.value!!/100).toLong() * 10)
                            }
                        }
                    }
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeWalked += lapTime
        }
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        totalDistanceWalked.postValue(0.0)
        totalPointsEarned.postValue(0)
        currentSpeed.postValue(0.0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                        Log.d(this.javaClass.name, "Started service")
                    } else {
                        Log.d(this.javaClass.name, "Resumed service")
                    }
                }
                ACTION_STOP_SERVICE -> {
                    isTracking.postValue(false)
                    Log.d(this.javaClass.name, "Stopped service")
                }
                else -> {
                    Log.d(this.javaClass.name, "Action not supported")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = 5000
                    fastestInterval = 2000
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        if (location.hasSpeed()) {
                            // Multiply by 3.6 to get Km/H
                            currentSpeed.postValue(location.speed.toDouble() * 3.6)
                        }
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTracking()
        isTracking.postValue(true)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(
            this, NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle(getString(R.string.tracking_walk_notification_label))
            .setContentText("00:00:00")
            .setContentIntent(getTrailActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            val notification = notificationBuilder.setContentText(
                TrackingUtility.getFormattedStopwatchTime(it * 1000L)
            )
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        })
    }

    private fun getTrailActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, TrailActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_ACTIVITY
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}