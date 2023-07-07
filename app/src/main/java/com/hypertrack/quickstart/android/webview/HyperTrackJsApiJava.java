package com.hypertrack.quickstart.android.webview;

import android.location.Location;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.hypertrack.sdk.GeotagResult;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class HyperTrackJsApiJava {

    private final HyperTrack sdkInstance;
    private static final String TAG = "HyperTrackJsApiJava";
    private static final String KEY_TYPE = "type";
    private static final String KEY_VALUE = "value";

    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_FAILURE = "failure";

    HyperTrackJsApiJava(
            HyperTrack sdkInstance
    ) {
        this.sdkInstance = sdkInstance;
    }

    static final String apiName = "HyperTrack";

    @JavascriptInterface
    public String getDeviceId() {
        String deviceId = sdkInstance.getDeviceID();
        Log.v(TAG, "getDeviceId: " + deviceId);
        return deviceId;
    }

    @JavascriptInterface
    public void setIsTracking(boolean isTracking) {
        if (isTracking) {
            sdkInstance.start();
        } else {
            sdkInstance.stop();
        }
    }

    @JavascriptInterface
    public void setMetadata(String metadataJsonString) {
        try {
            Map<String, Object> metadata = toMap(new JSONObject(metadataJsonString));
            sdkInstance.setDeviceMetadata(metadata);
        } catch (Exception e) {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Location:
     * {
     * "latitude": 37.3318,
     * "longitude": -122.0312
     * }
     * Result:
     * {
     * "type": "success",
     * // deviation from expected location in meters
     * "value": 0.0
     * }
     * {
     * "type": "failure",
     * "value": <failure reason>
     * }
     *
     * @param dataJsonString             JSON string with data object
     * @param expectedLocationJsonString JSON string with location object
     * @return JSON string with result object
     */
    @JavascriptInterface
    public String addGeotagWithExpectedLocation(String dataJsonString, String expectedLocationJsonString) {
        try {
            if (expectedLocationJsonString == null) {
                throw new RuntimeException("You must provide expected location");
            }
            Map<String, Object> data = toMap(new JSONObject(dataJsonString));
            Map<String, Object> expectedLocationMap = toMap(new JSONObject(expectedLocationJsonString));
            Location expectedLocation = deserializeLocation(expectedLocationMap);
            GeotagResult result = sdkInstance.addGeotag(data, expectedLocation);
            return serializeGeotagResultForJs(result);
        } catch (Exception e) {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    @JavascriptInterface
    public void setName(String deviceName) {
        sdkInstance.setDeviceName(deviceName);
    }

    @JavascriptInterface
    public void requestPermissionsIfNecessary() {
        sdkInstance.requestPermissionsIfNecessary();
    }

    /**
     * Serialization
     */

    private Location deserializeLocation(Map<String, Object> map) {
        Location location = new Location("HyperTrack");
        location.setLatitude((Double) map.get("latitude"));
        location.setLongitude((Double) map.get("longitude"));
        return location;
    }

    /**
     * WebViewSerialization
     */

    private String serializeGeotagResultForJs(GeotagResult result) {
        try {
            Map<String, Object> map = new HashMap<>();
            if (result instanceof GeotagResult.SuccessWithDeviation) {
                map.put(KEY_TYPE, TYPE_SUCCESS);
                map.put(KEY_VALUE, ((GeotagResult.SuccessWithDeviation) result).getDeviationDistance());
            } else if (result instanceof GeotagResult.Success) {
                throw new RuntimeException("GeotagResult.Success shouldn't be possible here");
            } else if (result instanceof GeotagResult.Error) {
                map.put(KEY_TYPE, TYPE_FAILURE);
                map.put(KEY_VALUE, ((GeotagResult.Error) result).getReason().name());
            }
            return toJSONObject(map).toString();
        } catch (Exception e) {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> toMap(JSONObject jsonObject) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            map.put(key, value);
        }

        return map;
    }

    private static List<Object> toList(JSONArray jsonArray) throws Exception {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            list.add(value);
        }

        return list;
    }

    private static JSONObject toJSONObject(Map<String, Object> map) {
        try {
            JSONObject jsonObject = new JSONObject();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof Map) {
                    value = toJSONObject((Map) value);
                } else if (value instanceof List) {
                    value = toJSONArray((List) value);
                }

                jsonObject.put(entry.getKey(), value);
            }

            return jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONArray toJSONArray(List<Object> list) {
        JSONArray jsonArray = new JSONArray();

        for (Object value : list) {
            if (value instanceof Map) {
                value = toJSONObject((Map) value);
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            }

            jsonArray.put(value);
        }

        return jsonArray;
    }

    private static void handleException(Exception e) {
        e.printStackTrace();
        Log.e(TAG, e.toString());
    }
}
