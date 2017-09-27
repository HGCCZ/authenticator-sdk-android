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

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.util.Base64;

import com.cmdisp.authenticator.sdk.api.callback.JsonArrayResponseCallback;
import com.cmdisp.authenticator.sdk.callback.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CertClient {
    /**
     * Public key used to verify the response from the API
     */
    private static final String PUBLIC_KEY =
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsH+SP53J8v5qORDS4I6S" +
                    "3tuFGuw/RtariaYEg+he2jlgXfpD+MQCtyWmAtTvZtOwFxsDJvLr9O1iDlsKAvMT" +
                    "BZ0UVkixfWGg0Uo5+Qty23WnNH74CHDv2aD1A7GmTjC8ixC5QOjSFqHHP3j8OHtA" +
                    "6TQNTfpWHiZD7jHZRc1dRh+ga/SPrVeTqIDFrs7QfclXnricX/3VoPgWPL7GWy0z" +
                    "HjpBqVP106ETh0tDWFAiZ9ie/eDXUt9s/hHbuJVe4zeOsiZ2TGmZfx2Lf1w57c9E" +
                    "tEuPhsBiWt+HBiq1tqMT5DJZ3hj+vM/mgzaWRQhzz661E+D61R2/jhLf9Hx6SBBB" +
                    "MQIDAQAB";

    private final String url;
    private final OkHttpClient client;

    public CertClient(String baseApiUrl) {
        url = baseApiUrl  + "/certificate";
        client = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                .build();
    }

    /**
     * Get certificates asynchronously
     */
    public void getCertificates(@NonNull final Callback<Set<String>> listener) {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new JsonArrayResponseCallback() {
            @Override
            protected void onResponse(int statusCode, JSONArray jsonArray) {
                try {
                    Set<String> certificates = parseCertificates(jsonArray);
                    listener.onSuccess(certificates);
                } catch (GeneralSecurityException | JSONException e) {
                    listener.onFailure(e);
                }
            }

            @Override
            protected void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    /**
     * Get certificates synchronously
     */
    public Set<String> getCertificates() throws IOException, GeneralSecurityException {
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();

        @SuppressWarnings("ConstantConditions")
        String body = response.body().string();

        if (!response.isSuccessful()) {
            throw new IOException(String.format(Locale.ENGLISH, "Call failed, status code: %d\nURL: %s\nResponse body: %s",
                    response.code(), response.request().url(), body));
        }

        if (body.isEmpty()) {
            throw new IOException("Response body is empty");
        }

        try {
            return parseCertificates(new JSONArray(body));
        } catch (JSONException e) {
            throw new IOException("Could not parse JSON", e);
        }
    }

    /**
     * Parse the JSON response to a list of certificates
     */
    private static Set<String> parseCertificates(@NonNull JSONArray jsonArray) throws GeneralSecurityException, JSONException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(getPublicKey());

        int length = jsonArray.length();
        Set<String> certificates = new HashSet<>(length);
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String data = jsonObject.optString("data", null);
            String sig = jsonObject.optString("signature", null);

            if (data == null || sig == null) continue;

            byte[] dataBytes = Base64.decode(data, Base64.DEFAULT);
            byte[] sigBytes = Base64.decode(sig, Base64.DEFAULT);

            signature.update(dataBytes);
            if (signature.verify(sigBytes)) {
                JSONObject dataObject = new JSONObject(new String(dataBytes));
                String algorithm = dataObject.optString("algorithm").replace("-", "");
                String hash = dataObject.optString("spki_hash");
                certificates.add(algorithm + '/' + hash);
            }
        }

        return certificates;
    }

    /**
     * Parse the public key
     */
    private static PublicKey getPublicKey() throws GeneralSecurityException {
        byte[] keyBytes = Base64.decode(PUBLIC_KEY, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
