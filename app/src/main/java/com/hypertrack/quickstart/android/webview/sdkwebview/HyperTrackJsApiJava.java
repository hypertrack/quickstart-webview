package com.hypertrack.quickstart.android.webview.sdkwebview;

import android.location.Location;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.hypertrack.sdk.GeotagResult;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONObject;

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
            Map<String, Object> data = WebViewSerialization.toMap(new JSONObject(dataJsonString));
            GeotagResult result = sdkInstance.addGeotag(data);
            if (result instanceof GeotagResult.SuccessWithDeviation) {
                throw new RuntimeException(
                        "addGeotag(): Unexpected GeotagResult type - GeotagResult.SuccessWithDeviation"
                );
            }
            return WebViewSerialization.serializeGeotagResultForJs(result);
        } catch (Exception e) {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    @JavascriptInterface
    public String addGeotagWithExpectedLocation(String dataJsonString, String expectedLocationJsonString) {
        try {
            if (expectedLocationJsonString == null) {
                throw new RuntimeException("You must provide expected location");
            }
            Map<String, Object> data = WebViewSerialization.toMap(new JSONObject(dataJsonString));
            Map<String, Object> expectedLocationMap = WebViewSerialization.toMap(new JSONObject(expectedLocationJsonString));
            Location expectedLocation = Serialization.deserializeLocation(expectedLocationMap);
            GeotagResult result = sdkInstance.addGeotag(data, expectedLocation);
            if (result instanceof GeotagResult.Success && !(result instanceof GeotagResult.SuccessWithDeviation)) {
                throw new RuntimeException(
                        "addGeotagWithExpectedLocation(): Unexpected GeotagResult type - GeotagResult.Success"
                );
            }
            return WebViewSerialization.serializeGeotagResultForJs(result);
        } catch (Exception e) {
            handleException(e);
            throw new RuntimeException(e);
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
            Map<String, Object> metadata = WebViewSerialization.toMap(new JSONObject(metadataJsonString));
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
}
