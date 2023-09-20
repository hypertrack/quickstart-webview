package com.hypertrack.quickstart.android.webview.sdkwebview;

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

public class HyperTrackJsApiJava {

    private final HyperTrack sdkInstance;
    private static final String TAG = "HyperTrackJsApiJava";

    public HyperTrackJsApiJava(HyperTrack sdkInstance) {
        this.sdkInstance = sdkInstance;
    }

    public static final String apiName = "HyperTrack";

    @JavascriptInterface
    public String addGeotag(String dataJsonString) {
        try {
            Map<String, Object> data = toMap(new JSONObject(dataJsonString));
            GeotagResult result = sdkInstance.addGeotag(data);
            if (result instanceof GeotagResult.SuccessWithDeviation) {
                throw new RuntimeException(
                        "addGeotag(): Unexpected GeotagResult type - GeotagResult.SuccessWithDeviation"
                );
            }
            return serializeJsSuccess(serializeGeotagResult(result));
        } catch (Exception e) {
            handleException(e);
            return serializeJsFailure(e);
        }
    }

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
            if (result instanceof GeotagResult.Success && !(result instanceof GeotagResult.SuccessWithDeviation)) {
                throw new RuntimeException(
                        "addGeotagWithExpectedLocation(): Unexpected GeotagResult type - GeotagResult.Success"
                );
            }
            return serializeJsSuccess(serializeGeotagResult(result));
        } catch (Exception e) {
            handleException(e);
            return serializeJsFailure(e);
        }
    }

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

    @JavascriptInterface
    public void setName(String deviceName) {
        sdkInstance.setDeviceName(deviceName);
    }

    @JavascriptInterface
    public void requestPermissionsIfNecessary() {
        sdkInstance.requestPermissionsIfNecessary();
    }

    static void handleException(Exception e) {
        e.printStackTrace();
        Log.e(TAG, e.toString());
    }

    /**
     * Serialization
     */

    private static final String KEY_TYPE = "type";
    private static final String KEY_VALUE = "value";
    private static final String KEY_ERROR = "error";

    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_FAILURE = "failure";

    static Location deserializeLocation(Map<String, Object> map) {
        try {
            Location location = new Location("HyperTrack");
            location.setLatitude((Double) map.get("latitude"));
            location.setLongitude((Double) map.get("longitude"));
            return location;
        } catch (Exception e) {
            HyperTrackJsApiJava.handleException(e);
            throw new RuntimeException(e);
        }
    }

    static Map<String, Object> serializeGeotagResult(GeotagResult result) throws Exception {
        if (result instanceof GeotagResult.SuccessWithDeviation) {
            return serializeGeotagResult((GeotagResult.SuccessWithDeviation) result);
        } else if (result instanceof GeotagResult.Success) {
            return serializeGeotagResult((GeotagResult.Success) result);
        } else if (result instanceof GeotagResult.Error) {
            return serializeGeotagResult((GeotagResult.Error) result);
        } else {
            throw new RuntimeException("Unknown GeotagResult type");
        }
    }

    private static Map<String, Object> serializeGeotagResult(GeotagResult.Success result) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, TYPE_SUCCESS);
        map.put(KEY_VALUE, serializeLocation(result.getDeviceLocation()));
        return map;
    }

    private static Map<String, Object> serializeGeotagResult(GeotagResult.SuccessWithDeviation result) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, TYPE_SUCCESS);
        map.put(KEY_VALUE, serializeLocationWithDeviation(result.getDeviceLocation(), result.getDeviationDistance()));
        return map;
    }

    private static Map<String, Object> serializeGeotagResult(GeotagResult.Error result) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, TYPE_FAILURE);
        map.put(KEY_VALUE, result.getReason().name());
        return map;
    }

    private static Map<String, Object> serializeLocation(Location location) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        return map;
    }

    private static Map<String, Object> serializeLocationWithDeviation(
            Location location,
            double deviation
    ) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("location", serializeLocation(location));
        map.put("deviation", deviation);
        return map;
    }

    /**
     * WebViewSerialization
     */

    static String serializeJsSuccess(Map<String, Object> data) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, TYPE_SUCCESS);
        map.put(KEY_VALUE, data);
        return toJSONObject(map).toString();
    }

    static String serializeJsFailure(Exception exception) {
        String error = "{\"error\":\"" + exception.toString() + "\"}";
        return "{\"type\":\"failure\", \"value\": " + error + "}";
    }

    static Map<String, Object> toMap(JSONObject jsonObject) throws Exception {
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

    private static JSONObject toJSONObject(Map<String, Object> map) throws Exception {
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

    private static JSONArray toJSONArray(List<Object> list) throws Exception {
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

    private static Map<String, Object> serializeExceptionToJsError(Exception exception) {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ERROR, exception.toString());
        return map;
    }
}
