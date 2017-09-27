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

package com.cmdisp.authenticator.sdk;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cmdisp.authenticator.sdk.api.AuthClient;
import com.cmdisp.authenticator.sdk.api.AuthClientImpl;
import com.cmdisp.authenticator.sdk.api.CertClient;
import com.cmdisp.authenticator.sdk.api.DeviceClient;
import com.cmdisp.authenticator.sdk.api.DeviceClientImpl;
import com.cmdisp.authenticator.sdk.api.EnvironmentClient;
import com.cmdisp.authenticator.sdk.api.EnvironmentClientImpl;
import com.cmdisp.authenticator.sdk.api.RestClient;
import com.cmdisp.authenticator.sdk.managers.CertificateManager;
import com.cmdisp.authenticator.sdk.managers.DeviceManager;
import com.cmdisp.authenticator.sdk.managers.DeviceRegistrationLifecycleCallback;

/**
 * Entry point to the SDK. It's required to {@link #init(Context, String) initialize} before calling any other method.
 */
public class Authenticator {
    private static final String TAG = Authenticator.class.getSimpleName();
    private static final String DEFAULT_API_URL = "https://api.auth.cmtelecom.com/authenticator/v1.0";
    private static final String DEFAULT_CERTIFICATE = "sha256/opbrnmGQhRgt/hnidpLFyJZBjDLo3tN/cIA4YafwQcs=";

    private static RestClient restClient;

    private static AuthClient authClient;
    private static DeviceClient deviceClient;
    private static EnvironmentClient environmentClient;

    private Authenticator() {
    }

    /**
     * Initialize the Authenticator SDK using the default configuration
     *
     * @param context the application Context
     * @param appKey  the app key obtained from the
     *                <a href="https://appmanager.cmtelecom.com/">App manager</a>
     * @see #init(Config)
     */
    public static void init(@NonNull Context context, @NonNull String appKey) {
        init(new Config(context, appKey));
    }

    /**
     * Initialize the Authenticator SDK
     */
    public static void init(@NonNull Config config) {
        if (restClient != null) {
            throw new IllegalStateException("Authenticator is already initialized");
        }

        CertClient certClient = new CertClient(config.apiUrl);
        CertificateManager certManager = new CertificateManager(config.context, certClient, config.initialCertificate);
        DeviceManager deviceManager = new DeviceManager(config.context);

        restClient = new RestClient(config.appName, config.appVersion, config.apiUrl, certManager);
        authClient = new AuthClientImpl(restClient);
        deviceClient = new DeviceClientImpl(restClient, config.context, config.appKey, deviceManager);
        environmentClient = new EnvironmentClientImpl(restClient);

        Context appContext = config.context.getApplicationContext();
        if (appContext instanceof Application) {
            Application application = (Application) appContext;
            application.registerActivityLifecycleCallbacks(new DeviceRegistrationLifecycleCallback(deviceManager));
        }
    }

    /**
     * Get the authentication API client
     */
    @NonNull
    public static AuthClient authClient() {
        requireInitialization();
        return authClient;
    }

    /**
     * Get the device API client
     */
    @NonNull
    public static DeviceClient deviceClient() {
        requireInitialization();
        return deviceClient;
    }

    /**
     * Get the environment API client
     */
    @NonNull
    public static EnvironmentClient environmentClient() {
        requireInitialization();
        return environmentClient;
    }

    private static void requireInitialization() {
        if (restClient == null) {
            throw new IllegalStateException("Authenticator is not initialized, initialize it before using it");
        }
    }

    /**
     * The Authenticator SDK configuration
     */
    public static class Config {
        private Context context;
        private String appKey;
        private String appName;
        private String appVersion;
        private String apiUrl = DEFAULT_API_URL;
        private String initialCertificate = DEFAULT_CERTIFICATE;

        /**
         * Configuration with the app name and version retrieved from the package
         *
         * @param context the application Context
         * @param appKey  the app key obtained from the
         *                <a href="https://appmanager.cmtelecom.com/">App manager</a>
         */
        public Config(@NonNull Context context, @NonNull String appKey) {
            try {
                String packageName = context.getPackageName();
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);

                this.context = context;
                this.appKey = appKey;
                this.appName = context.getApplicationInfo().loadLabel(pm).toString();
                this.appVersion = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.wtf(TAG, "Could not get package info", e);
                throw new IllegalStateException("Could not get package info", e);
            }
        }

        /**
         * Set a custom app name to be used in the user-agent
         */
        @NonNull
        public Config setAppName(@NonNull String appName) {
            this.appName = appName;
            return this;
        }

        /**
         * Set a custom app version to be used in the user-agent
         */
        @NonNull
        public Config setAppVersion(@NonNull String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        /**
         * Set a custom authenticator API url (e.g. test or staging)
         */
        @NonNull
        public Config setApiUrl(@NonNull String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        /**
         * Set the initial certificate to use for certificate pinning
         */
        @NonNull
        public Config setInitialCertificate(@NonNull String initialCertificate) {
            this.initialCertificate = initialCertificate;
            return this;
        }
    }
}
