package com.google.android.stardroid.activities.util

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.google.android.stardroid.R
import com.google.android.stardroid.math.LatLong
import com.google.android.stardroid.util.AnalyticsInterface
import javax.inject.Inject

/**
 * Implementation of [MapAdapter] that uses Stadia Maps Static Maps API.
 * This implementation works for both GMS and F-Droid flavors as it only
 * requires standard HTTP networking.
 *
 * It uses the Coil library for lifecycle-aware image loading and on-disk caching.
 */
class StadiaMapsAdapter @Inject constructor(
    private val analytics: AnalyticsInterface
) : MapAdapter {
    private var imageView: ImageView? = null
    private var fallbackLabel: TextView? = null
    private var apiKey: String? = null
    private var lastLocation: LatLong? = null

    override fun initialize(mapView: View, savedInstanceState: Bundle?) {
        if (mapView is ImageView) {
            this.imageView = mapView
            val root = mapView.parent as View
            this.fallbackLabel = root.findViewById(R.id.map_unavailable_label)
            this.apiKey = mapView.context.getString(R.string.stadia_maps_api_key)
        }
    }

    override fun onResume() {
        lastLocation?.let { updateLocation(it) }
    }

    override fun onPause() {}
    override fun onDestroy() {
        imageView = null
        fallbackLabel = null
    }

    override fun onSaveInstanceState(outState: Bundle) {}

    override fun updateLocation(location: LatLong) {
        lastLocation = location
        val key = apiKey ?: return
        if (key == "unset" || key.isEmpty()) {
            Log.w("StadiaMapsAdapter", "Stadia Maps API key is not set.")
            showError()
            trackMapLoad(false, "missing_key")
            return
        }

        val iv = imageView ?: return
        
        // If view is not yet laid out, wait and try again
        if (iv.width == 0 || iv.height == 0) {
            iv.post { updateLocation(location) }
            return
        }

        val width = iv.width
        val height = iv.height

        // Stadia Maps Static Map URL
        // https://tiles.stadiamaps.com/static/alidade_smooth.png?api_key={key}&center={lat},{lon}&zoom=12&size={width}x{height}&markers={lat},{lon}
        // See https://stadiamaps.com/products/maps/map-styles/ for map styles.
        val urlStr = "https://tiles.stadiamaps.com/static/stamen_toner_dark.png?" +
                "api_key=$key" +
                "&center=${location.latitude},${location.longitude}" +
                "&zoom=11" +
                "&size=${width}x${height}" +
                "&markers=${location.latitude},${location.longitude}"

        iv.load(urlStr) {
            listener(
                onSuccess = { _, _ ->
                    iv.visibility = View.VISIBLE
                    fallbackLabel?.visibility = View.GONE
                    trackMapLoad(true, null)
                },
                onError = { _, result ->
                    Log.e("StadiaMapsAdapter", "Error loading map: ${result.throwable.message}")
                    showError()
                    trackMapLoad(false, result.throwable.message ?: "unknown_error")
                }
            )
        }
    }

    private fun showError() {
        imageView?.visibility = View.GONE
        fallbackLabel?.visibility = View.VISIBLE
    }

    private fun trackMapLoad(success: Boolean, errorCode: String?) {
        val b = Bundle()
        b.putBoolean(AnalyticsInterface.MAP_LOAD_SUCCESS, success)
        b.putString(AnalyticsInterface.MAP_LOAD_PROVIDER, AnalyticsInterface.MAP_LOAD_PROVIDER_STADIA)
        if (errorCode != null) {
            b.putString(AnalyticsInterface.MAP_LOAD_ERROR_CODE, errorCode)
        }
        analytics.trackEvent(AnalyticsInterface.MAP_LOAD_EVENT, b)
    }
}
