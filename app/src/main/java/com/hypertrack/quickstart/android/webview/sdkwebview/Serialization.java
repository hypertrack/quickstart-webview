package com.hypertrack.quickstart.android.webview.sdkwebview;

import android.location.Location;

import com.hypertrack.sdk.GeotagResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @noinspection DataFlowIssue
 */
public class Serialization {

    private static final String KEY_TYPE = "type";
    private static final String KEY_VALUE = "value";

    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_FAILURE = "failure";

    /**
     * Serialization
     */

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

    static Map<String, Object> serializeGeotagResult(GeotagResult result) {
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

    private static Map<String, Object> serializeGeotagResult(GeotagResult.Success result) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_TYPE, TYPE_SUCCESS);
            map.put(KEY_VALUE, serializeLocation(result.getDeviceLocation()));
            return map;
        } catch (Exception e) {
            HyperTrackJsApiJava.handleException(e);
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> serializeGeotagResult(GeotagResult.SuccessWithDeviation result) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_TYPE, TYPE_SUCCESS);
            map.put(KEY_VALUE, serializeLocationWithDeviation(result.getDeviceLocation(), result.getDeviationDistance()));
            return map;
        } catch (Exception e) {
            HyperTrackJsApiJava.handleException(e);
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> serializeGeotagResult(GeotagResult.Error result) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_TYPE, TYPE_FAILURE);
            map.put(KEY_VALUE, result.getReason().name());
            return map;
        } catch (Exception e) {
            HyperTrackJsApiJava.handleException(e);
            throw new RuntimeException(e);
        }
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

}
