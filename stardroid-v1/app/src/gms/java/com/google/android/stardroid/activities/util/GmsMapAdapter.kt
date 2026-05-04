package com.google.android.stardroid.activities.util

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.stardroid.math.LatLong
import javax.inject.Inject

/**
 * GMS implementation of [MapAdapter] using Google Maps.
 */
class GmsMapAdapter @Inject constructor() : MapAdapter, OnMapReadyCallback {
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var lastLocation: LatLong? = null

    override fun initialize(mapView: View, savedInstanceState: Bundle?) {
        if (mapView is MapView) {
            this.mapView = mapView
            try {
                mapView.onCreate(savedInstanceState)
                mapView.getMapAsync(this)
            } catch (e: Exception) {
                Log.e("GmsMapAdapter", "Error initializing MapView - check your API key: ${e.message}")
                this.mapView = null
                mapView.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView?.onSaveInstanceState(outState)
    }

    override fun updateLocation(location: LatLong) {
        lastLocation = location
        googleMap?.let { map ->
            val latLng = LatLng(location.latitude.toDouble(), location.longitude.toDouble())
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        lastLocation?.let { updateLocation(it) }
    }
}
