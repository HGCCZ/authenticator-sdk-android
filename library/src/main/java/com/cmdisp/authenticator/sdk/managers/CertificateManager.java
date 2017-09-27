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

package com.cmdisp.authenticator.sdk.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;

import com.cmdisp.authenticator.sdk.BuildConfig;
import com.cmdisp.authenticator.sdk.api.CertClient;
import com.cmdisp.authenticator.sdk.callback.Callback;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CertificateManager {
    private static final String TAG = CertificateManager.class.getSimpleName();
    private static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".cert_pinning"; // TODO: prefix with 'authenticator'?
    private static final String PREF_KEY_CERTS = "certificates";

    private long nextRefreshTimestamp;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private final SharedPreferences preferences;
    private final CertClient certClient;
    private final String initialCert;

    public CertificateManager(Context context, CertClient certClient, String initialCert) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        this.certClient = certClient;
        this.initialCert = initialCert;
    }

    /**
     * Get the certificates
     */
    public Set<String> getCertificates() {
        Set<String> certs = loadCertificates();
        if (certs == null || certs.isEmpty()) {
            return Collections.singleton(initialCert);
        } else {
            return certs;
        }
    }

    /**
     * Retrieve the current certificates from the API
     *
     * @param listener listener to notify when the certificates changed
     */
    public void updateCertificates(CertificatesUpdatedListener listener) {
        if (nextRefreshTimestamp > System.currentTimeMillis() || isRefreshing.getAndSet(true)) {
            Log.v(TAG, "Refresh not needed");
            return;
        }

        Log.v(TAG, "Refreshing certificates");
        certClient.getCertificates(new Callback<Set<String>>() {
            @Override
            public void onSuccess(Set<String> certificates) {
                Log.v(TAG, "Finished refreshing, got " + certificates.size() + " certificates");
                if (!certificates.equals(loadCertificates())) {
                    storeCertificates(certificates);
                    listener.onCertificatesUpdated(certificates);
                }
                nextRefreshTimestamp = System.currentTimeMillis() + DateUtils.HOUR_IN_MILLIS;
                isRefreshing.set(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Could not get certificates", e);
                nextRefreshTimestamp = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS;
                isRefreshing.set(false);
            }
        });
    }

    /**
     * Load the certificates from the storage
     */
    private Set<String> loadCertificates() {
        return preferences.getStringSet(PREF_KEY_CERTS, null);
    }

    /**
     * Write the certificates to the storage
     */
    private void storeCertificates(Set<String> certificatePins) {
        preferences.edit().putStringSet(PREF_KEY_CERTS, certificatePins).apply();
    }

    public interface CertificatesUpdatedListener {
        void onCertificatesUpdated(Set<String> certificates);
    }
}
