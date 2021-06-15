package pt.ie.dogwalkingbuddy

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_trail_bottom_sheet.*
import pt.ie.dogwalkingbuddy.other.Constants.ACTION_START_OR_RESUME_SERVICE
import pt.ie.dogwalkingbuddy.other.Constants.ACTION_STOP_SERVICE
import pt.ie.dogwalkingbuddy.other.Constants.MAP_ZOOM
import pt.ie.dogwalkingbuddy.other.Constants.POLYLINE_COLOR
import pt.ie.dogwalkingbuddy.other.Constants.POLYLINE_WIDTH
import pt.ie.dogwalkingbuddy.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import pt.ie.dogwalkingbuddy.other.TrackingUtility
import pt.ie.dogwalkingbuddy.services.TrackingService
import pt.ie.dogwalkingbuddy.services.Polyline
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit


class TrailActivity : AppCompatActivity(),
                      EasyPermissions.PermissionCallbacks,
                      OnMapReadyCallback,
                      DialogInterface.OnClickListener {
    private var isTracking = true
    private lateinit var mMap: GoogleMap
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInSecs = 0L
    private var distanceWalkedInMeters = 0.0
    private var pointsEarned = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trail)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        end_walk_btn.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.end_walk_confirm_dialog))
                setMessage(getString(R.string.end_walk_confirm_dialog_desc))
                setPositiveButton(getString(R.string.confirm_label), this@TrailActivity)
                setNegativeButton(getString(R.string.cancel_label), this@TrailActivity)
            }.show()
        }
        subscribeToObservers()
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onBackPressed() {
        // Logic to save / discard trail
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (TrackingUtility.hasLocationPermissions(this)) {
            mMap.isMyLocationEnabled = true
            sendCommandToTrackingService(ACTION_START_OR_RESUME_SERVICE)
            addAllPolylines()
        }
    }

    private fun endWalk() {
        sendCommandToTrackingService(ACTION_STOP_SERVICE)
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(this, Observer {
            isTracking = it
        })
        TrackingService.pathPoints.observe(this, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToCurrentPosition()
        })
        TrackingService.timeRunInMillis.observe(this, Observer {
            curTimeInSecs = it
            val formattedTime = TrackingUtility.getFormattedStopwatchTime(curTimeInSecs)
            trail_time_elapsed.text = formattedTime
        })
        TrackingService.totalDistanceWalked.observe(this, Observer {
            distanceWalkedInMeters = it
            val distanceWalkedInKms = distanceWalkedInMeters / 1000
            trail_walked_distance.text = getString(R.string.trail_walked_distance_label, distanceWalkedInKms)
        })
        TrackingService.totalPointsEarned.observe(this, Observer {
            pointsEarned = it
            trail_points_earned.text = "$pointsEarned"
        })
    }

    private fun moveCameraToCurrentPosition() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            mMap?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val secondToLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(secondToLastLatLng)
                .add(lastLatLng)
            mMap?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToTrackingService(action: String) =
        Intent(this, TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { }

    override fun onRequestPermissionsResult(reqCode: Int, perm: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(reqCode, perm, results)
        EasyPermissions.onRequestPermissionsResult(reqCode, perm, results, this)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            val firebaseAuth = FirebaseAuth.getInstance()
            val db = Firebase.firestore

            val trail = hashMapOf(
                "user" to firebaseAuth.currentUser?.uid,
                "time_started_unix" to TrackingService.timeStarted,
                "seconds_walked" to curTimeInSecs,
                "points_earned" to pointsEarned
            )

            db.collection("trails").add(trail)
                .addOnSuccessListener { trail ->
                    for (point in pathPoints.last()) {
                        trail.collection("points").add(
                            hashMapOf("lat" to point.latitude, "lng" to point.longitude)
                        )
                    }
                    db.collection("user").document(firebaseAuth.uid!!).get()
                        .addOnSuccessListener { user ->
                            if (user.data != null) {
                                val curPoints = user.data!!["points"] as Long
                                db.collection("user").document(firebaseAuth.uid!!)
                                    .set(hashMapOf("points" to curPoints + pointsEarned))
                                sendCommandToTrackingService(ACTION_STOP_SERVICE)
                                finish()
                                Log.d(this.javaClass.name, "Trail Successfully Saved!")
                            } else {
                                // user doesn't exist or doesn't have any points
                                db.collection("user").document(firebaseAuth.uid!!)
                                    .set(hashMapOf("points" to pointsEarned))
                            }
                        }
                }
                .addOnFailureListener {
                    Log.w(this.javaClass.name, "Error writing document", it)
                }
        }
    }
}