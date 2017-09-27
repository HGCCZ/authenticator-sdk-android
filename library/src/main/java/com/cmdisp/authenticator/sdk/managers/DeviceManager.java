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

import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.BuildConfig;
import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    private static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".device_info";
    private static final String PREF_KEY_DEVICE_ID = "device_id";

    private final SharedPreferences preferences;
    private long nextUpdateTimestamp;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);

    public DeviceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getDeviceId() {
        return preferences.getString(PREF_KEY_DEVICE_ID, null);
    }

    public void storeDeviceId(String deviceId) {
        preferences.edit().putString(PREF_KEY_DEVICE_ID, deviceId).apply();
    }

    void updateRegistration() {
        if (nextUpdateTimestamp > System.currentTimeMillis()) {
            // the registration was updated recently
            return;
        }
        if (!preferences.contains(PREF_KEY_DEVICE_ID)) {
            // device is not registered
            return;
        }
        if (isUpdating.getAndSet(true)) {
            // the registration is currently getting updated
            return;
        }

        Authenticator.deviceClient().updateRegistration(new Callback<DeviceRegistration>() {
            @Override
            public void onSuccess(DeviceRegistration registration) {
                nextUpdateTimestamp = System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS;
                isUpdating.set(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error updating device info", e);
                nextUpdateTimestamp = System.currentTimeMillis() + DateUtils.HOUR_IN_MILLIS;
                isUpdating.set(false);
            }
        });
    }
}
