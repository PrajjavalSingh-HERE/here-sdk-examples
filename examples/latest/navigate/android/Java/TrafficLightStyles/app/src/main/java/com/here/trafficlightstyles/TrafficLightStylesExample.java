/*
 * Copyright (C) 2019-2026 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.trafficlightstyles;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.gestures.GestureType;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapFeatureModes;
import com.here.sdk.mapview.MapFeatures;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// Shows how the size of the traffic light icons can be controlled from the style side.
//
// Each custom style in the "assets" folder is a HERE Style Editor compatible archive:
//   <root folder>/style.json  ->  {"base": {"style": "oslo", "scheme": "normal.day"},
//                                  "definitions": {"POI.TrafficLight.Icon.Size": [...]}}
// "base" inherits the complete built-in NORMAL_DAY scheme, and the "definitions" block overrides
// only the traffic light icon size. Everything else on the map stays identical, so any visual
// difference on screen comes from that single value.
public class TrafficLightStylesExample {

    private static final String TAG = TrafficLightStylesExample.class.getSimpleName();

    public static final String STYLE_50_PERCENT = "traffic-lights-050.zip";
    public static final String STYLE_100_PERCENT = "traffic-lights-100.zip";
    public static final String STYLE_200_PERCENT = "traffic-lights-200.zip";
    public static final String STYLE_300_PERCENT = "traffic-lights-300.zip";

    // The style definition that drives the "width" of the "traffic_lights" icon style rule.
    private static final String TRAFFIC_LIGHT_ICON_SIZE_DEFINITION = "POI.TrafficLight.Icon.Size";

    // Berlin Mitte (Friedrichstrasse / Unter den Linden): an area with many signalized intersections.
    private static final GeoCoordinates MAP_CENTER = new GeoCoordinates(52.51705, 13.38881);

    // Traffic light icons are shown by the HERE map style only from zoom level 18 onwards.
    private static final double ZOOM_LEVEL = 18.5;

    private final Context context;
    private final MapView mapView;
    private final TextView infoTextView;

    public TrafficLightStylesExample(Context context, MapView mapView, TextView infoTextView) {
        this.context = context;
        this.mapView = mapView;
        this.infoTextView = infoTextView;
    }

    public void onMapSceneLoaded() {
        mapView.getCamera().lookAt(MAP_CENTER, new MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, ZOOM_LEVEL));

        // Keep the map static, so that the different styles can be compared 1:1 on the same view.
        for (GestureType gestureType : GestureType.values()) {
            mapView.getGestures().disableDefaultAction(gestureType);
        }

        enableTrafficLights();
        showInfo("MapScheme.NORMAL_DAY (built-in)", "not overridden -> HERE SDK default");
    }

    // Baseline: the unmodified map scheme as shipped with the HERE SDK.
    public void loadSdkDefaultScheme() {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError == null) {
                    enableTrafficLights();
                    showInfo("MapScheme.NORMAL_DAY (built-in)", "not overridden -> HERE SDK default");
                } else {
                    Log.e(TAG, "onLoadScene failed: " + mapError);
                    showInfo("MapScheme.NORMAL_DAY (built-in)", "Loading failed: " + mapError.name());
                }
            }
        });
    }

    // Loads one of the custom style archives from the "assets" folder.
    public void loadCustomStyle(final String styleFileName) {
        final String iconSizeDefinition = readIconSizeDefinition(styleFileName);
        if (iconSizeDefinition == null) {
            showInfo(styleFileName, "Error: Map style not found or not readable!");
            return;
        }

        mapView.getMapScene().loadScene(styleFileName, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError == null) {
                    enableTrafficLights();
                    showInfo(styleFileName, iconSizeDefinition);
                } else {
                    Log.e(TAG, "onLoadScene failed for " + styleFileName + ": " + mapError);
                    showInfo(styleFileName, "Loading failed: " + mapError.name());
                }
            }
        });
    }

    // Traffic lights are enabled by default on compatible schemes. We enable them explicitly
    // so the example does not depend on that default.
    private void enableTrafficLights() {
        Map<String, String> mapFeatures = new HashMap<>();
        mapFeatures.put(MapFeatures.TRAFFIC_LIGHTS, MapFeatureModes.TRAFFIC_LIGHTS_ALL);
        mapView.getMapScene().enableFeatures(mapFeatures);
    }

    // Reads the value of POI.TrafficLight.Icon.Size from <root folder>/style.json inside the
    // archive, so the value shown on screen is exactly the one the HERE SDK is loading.
    @Nullable
    private String readIconSizeDefinition(String styleFileName) {
        try (InputStream inputStream = context.getAssets().open(styleFileName);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith("/style.json") && !name.startsWith("__MACOSX")) {
                    JSONObject style = new JSONObject(readFully(zipInputStream));
                    JSONObject definitions = style.optJSONObject("definitions");
                    Object value = definitions == null ? null : definitions.opt(TRAFFIC_LIGHT_ICON_SIZE_DEFINITION);
                    return value == null ? "not overridden -> HERE SDK default" : value.toString();
                }
            }
            Log.e(TAG, "No style.json found in " + styleFileName);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Cannot read " + styleFileName + ": " + e.getMessage());
        }
        return null;
    }

    private static String readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, count);
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    private void showInfo(String styleName, String iconSizeDefinition) {
        String info = "Style: " + styleName + "\n"
                + TRAFFIC_LIGHT_ICON_SIZE_DEFINITION + " = " + iconSizeDefinition + "\n"
                + "Zoom level: " + ZOOM_LEVEL + " (gestures disabled)";
        infoTextView.setText(info);
        Log.d(TAG, info);
    }
}
