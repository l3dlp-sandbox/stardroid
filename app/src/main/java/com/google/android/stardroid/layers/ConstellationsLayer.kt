// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.stardroid.layers

import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Resources
import com.google.android.stardroid.R

/**
 * An implementation of the [AbstractFileBasedLayer] to display
 * Constellations in the renderer.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
class ConstellationsLayer(assetManager: AssetManager, resources: Resources, preferences: SharedPreferences) :
    AbstractFileBasedLayer(assetManager, resources, "constellations.binary", preferences) {
    override val layerDepthOrder = 10

    // TODO(johntaylor): rename this string id.
    override val layerNameId = R.string.show_constellations_pref

    // TODO(brent): Remove this.
    override val preferenceId = "source_provider.1"
}