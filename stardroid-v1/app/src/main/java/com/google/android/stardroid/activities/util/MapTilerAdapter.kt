package com.google.android.stardroid.activities.util

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.stardroid.R
import com.google.android.stardroid.math.LatLong
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * Implementation of [MapAdapter] that uses MapTiler Static Maps API.
 * This implementation works for both GMS and F-Droid flavors as it only
 * requires standard HTTP networking.
 */
class MapTilerAdapter @Inject constructor() : MapAdapter {
    private var imageView: ImageView? = null
    private var fallbackLabel: TextView? = null
    private var apiKey: String? = null
    private var lastLocation: LatLong? = null

    override fun initialize(mapView: View, savedInstanceState: Bundle?) {
        if (mapView is ImageView) {
            this.imageView = mapView
            val root = mapView.parent as View
            this.fallbackLabel = root.findViewById(R.id.map_unavailable_label)
            this.apiKey = mapView.context.getString(R.string.maptiler_api_key)
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
            Log.w("MapTilerAdapter", "MapTiler API key is not set.")
            showError()
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

        // MapTiler Static Map URL
        // Note: MapTiler uses lon,lat order for coordinates.
        val urlStr = "https://api.maptiler.com/maps/streets/static/" +
                "${location.longitude},${location.latitude},12/" +
                "${width}x${height}.png?markers=${location.longitude},${location.latitude}&key=$key"

        thread {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                if (connection.responseCode != 200) {
                    throw Exception("HTTP Error ${connection.responseCode}")
                }
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                iv.post {
                    if (bitmap != null) {
                        iv.setImageBitmap(bitmap)
                        iv.visibility = View.VISIBLE
                        fallbackLabel?.visibility = View.GONE
                    } else {
                        showError()
                    }
                }
            } catch (e: Exception) {
                Log.e("MapTilerAdapter", "Error loading map: ${e.message}")
                iv.post { showError() }
            }
        }
    }

    private fun showError() {
        imageView?.visibility = View.GONE
        fallbackLabel?.visibility = View.VISIBLE
    }
}
