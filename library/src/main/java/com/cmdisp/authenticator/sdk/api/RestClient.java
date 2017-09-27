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

import android.support.annotation.RestrictTo;
import android.util.Log;

import com.cmdisp.authenticator.sdk.BuildConfig;
import com.cmdisp.authenticator.sdk.managers.CertificateManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RestClient implements CertificateManager.CertificatesUpdatedListener {
    /** PUT and POST requests require a body, if you don't need one, use this empty body */
    static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);

    private OkHttpClient client;
    private final String apiUrl;
    private final CertificateManager certManager;
    private String deviceId;

    public RestClient(String appName, String appVersion, String apiUrl, CertificateManager certManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor(appName, appVersion))
                .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS));

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg))
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }

        this.client = builder.build();
        this.apiUrl = apiUrl;
        this.certManager = certManager;

        setCertificates(certManager.getCertificates());
        certManager.updateCertificates(this);
    }

    /**
     * Get the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set the device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Update the certificate pinner to pin the given certificates
     */
    private void setCertificates(Set<String> certsSet) {
        String[] certsArr = certsSet.toArray(new String[certsSet.size()]);
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(getHost(apiUrl), certsArr)
                .build();
        client = client.newBuilder().certificatePinner(certificatePinner).build();
    }

    /**
     * Certificates updated callback from the {@link CertificateManager}
     */
    @Override
    public void onCertificatesUpdated(Set<String> certificates) {
        setCertificates(certificates);
    }

    /**
     * Execute an HTTP call (synchronously)
     */
    Response callSync(Request request) throws IOException {
        certManager.updateCertificates(this);
        return client.newCall(request).execute();
    }

    /**
     * Enqueue an HTTP call (asynchronously)
     */
    void callAsync(Request request, Callback callback) {
        certManager.updateCertificates(this);
        client.newCall(request).enqueue(callback);
    }

    /**
     * Get the base device URL
     */
    String getDeviceUrl() {
        return apiUrl + "/device";
    }

    /**
     * Get the base URL including the device identifier as set by {@link #setDeviceId(String)}
     */
    String getDeviceIdUrl() {
        return getDeviceUrl() + "/" + deviceId;
    }

    /**
     * Get hostname from a URL
     */
    private static String getHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not get the host from the url: " + url, e);
        }
    }
}
