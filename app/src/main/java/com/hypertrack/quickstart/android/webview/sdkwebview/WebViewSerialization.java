package com.hypertrack.quickstart.android.webview.sdkwebview;

import com.hypertrack.sdk.GeotagResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WebViewSerialization {

    private static final String TAG = "HyperTrackJsApiJava";

    /**
     * WebViewSerialization
     */

    static String serializeGeotagResultForJs(GeotagResult result) {
        try {
            Map<String, Object> map = Serialization.serializeGeotagResult(result);
            return toJSONObject(map).toString();
        } catch (Exception e) {
            HyperTrackJsApiJava.handleException(e);
            throw new RuntimeException(e);
        }
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


}
