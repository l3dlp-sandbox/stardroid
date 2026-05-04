package com.google.android.stardroid.activities.util

import android.os.Bundle
import android.view.View
import com.google.android.stardroid.math.LatLong
import javax.inject.Inject

/**
 * fdroid implementation of [MapAdapter] (no-op).
 */
class FdroidMapAdapter @Inject constructor() : MapAdapter {
    override fun initialize(mapView: View, savedInstanceState: Bundle?) {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onDestroy() {}
    override fun onSaveInstanceState(outState: Bundle) {}
    override fun updateLocation(location: LatLong) {}
}
