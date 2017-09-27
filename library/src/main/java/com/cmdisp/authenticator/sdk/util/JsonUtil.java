/*
 * Copyright (c) 2017 CM Telecom B.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cmdisp.authenticator.sdk.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Backport {@link JSONObject} and {@link JSONArray} features added in API 19
 * <p>
 * Based on: <a href="http://stackoverflow.com/a/22912023">StackOverflow answer</a>
 */
public class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Convert Map to JSONObject recursively
     */
    public static JSONObject mapToJson(Map<?, ?> data) {
        JSONObject object = new JSONObject();

        for (Map.Entry<?, ?> entry : data.entrySet()) {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            try {
                object.put(key, wrap(entry.getValue()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    /**
     * Convert Collection to JSONArray recursively
     */
    public static JSONArray collectionToJson(Collection data) {
        JSONArray jsonArray = new JSONArray();
        if (data != null) {
            for (Object aData : data) {
                jsonArray.put(wrap(aData));
            }
        }
        return jsonArray;
    }

    /**
     * Convert array to JSONArray recursively
     */
    public static JSONArray arrayToJson(Object data) throws JSONException {
        if (!data.getClass().isArray()) {
            throw new JSONException("Not a primitive data: " + data.getClass());
        }
        final int length = Array.getLength(data);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < length; ++i) {
            jsonArray.put(wrap(Array.get(data, i)));
        }

        return jsonArray;
    }

    private static Object wrap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }

        try {
            if (o instanceof Collection) {
                return collectionToJson((Collection) o);
            }
            if (o.getClass().isArray()) {
                return arrayToJson(o);
            }
            if (o instanceof Map) {
                return mapToJson((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            } else {
                return o.toString();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String optString(JSONObject json, String key) {
        return optString(json, key, null);
    }

    public static String optString(JSONObject json, String key, String fallback) {
        if (isNull(json, key)) {
            return fallback;
        } else {
            return json.optString(key, fallback);
        }
    }

    public static Double optDouble(JSONObject json, String key) {
        return optDouble(json, key, null);
    }

    public static Double optDouble(JSONObject json, String key, Double fallback) {
        if (isNull(json, key)) {
            return fallback;
        } else {
            try {
                return json.getDouble(key);
            } catch (JSONException e) {
                return fallback;
            }
        }
    }

    public static boolean isNull(JSONObject json, String key) {
        return json == null || json.isNull(key);
    }
}
