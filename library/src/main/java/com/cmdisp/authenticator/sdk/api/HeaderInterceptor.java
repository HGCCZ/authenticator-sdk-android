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

package com.cmdisp.authenticator.sdk.api;

import android.os.Build;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.cmdisp.authenticator.sdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Intercepts every request and adds the Authorization and User-Agent header
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class HeaderInterceptor implements Interceptor {
    static final String AUTHORIZATION_SECRET = "Authorization-Secret";
    static final String AUTHORIZATION_PAYLOAD = "Authorization-Payload";

    private static final String TAG = HeaderInterceptor.class.getSimpleName();
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final int EXPIRY_TIME = 60;

    private final String userAgent;

    HeaderInterceptor(String appName, String appVersion) {
        String androidVersion = Build.VERSION.RELEASE;
        String manufacturer = Build.MANUFACTURER;
        String deviceModel = Build.MODEL;
        String locale = Locale.getDefault().toString();

        userAgent = String.format("%s/%s (Android %s; %s; %s; %s)", appName,
                appVersion, androidVersion, manufacturer, deviceModel, locale);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder()
                .addHeader(HEADER_USER_AGENT, userAgent);

        String secret = request.header(AUTHORIZATION_SECRET);
        if (secret != null) {
            String payload = request.header(AUTHORIZATION_PAYLOAD);
            String sig = createSignature(secret, payload, request.body());

            builder.removeHeader(AUTHORIZATION_SECRET);
            builder.removeHeader(AUTHORIZATION_PAYLOAD);
            builder.header(HEADER_AUTHORIZATION, "Bearer " + sig);
        }

        return chain.proceed(builder.build());
    }

    private static String createSignature(String secret, String payload, RequestBody body) throws IOException {
        long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        try {
            JSONObject jsonHeader = new JSONObject()
                    .put("alg", "HS256");

            JSONObject jsonPayload = (payload == null ? new JSONObject() : new JSONObject(payload))
                    .put("iat", now)
                    .put("nbf", now)
                    .put("exp", now + EXPIRY_TIME);

            if (body != null && body.contentLength() > 0) {
                Buffer bodyBuffer = new Buffer();
                body.writeTo(bodyBuffer);
                byte[] bodyBytes = bodyBuffer.readByteArray();

                byte[] bodySig = SecurityUtil.hmacSHA256(bodyBytes, secret);
                jsonPayload.put("sig", bytesToHex(bodySig));
            }

            return SecurityUtil.createJWT(jsonHeader, jsonPayload, secret);
        } catch (JSONException e) {
            Log.wtf(TAG, "Error creating JSON object for JWT", e);
            return null;
        }
    }

    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String bytesToHex(byte[] data) {
        int length = data.length;
        char[] hexChars = new char[length << 1];

        // two characters form the hex value
        for (int i = 0, j = 0; i < length; i++) {
            hexChars[j++] = HEX_ARRAY[(0xF0 & data[i]) >>> 4];
            hexChars[j++] = HEX_ARRAY[0x0F & data[i]];
        }
        return new String(hexChars);
    }
}
