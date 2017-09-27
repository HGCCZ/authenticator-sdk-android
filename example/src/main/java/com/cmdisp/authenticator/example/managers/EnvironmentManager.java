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

package com.cmdisp.authenticator.example.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.cmdisp.authenticator.example.BuildConfig;
import com.cmdisp.authenticator.sdk.models.DefaultEnvironment;
import com.cmdisp.authenticator.sdk.models.Environment;

public class EnvironmentManager {
    private static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".environment";
    private static final String KEY_ENV_ID = "environment_id";
    private static final String KEY_ENV_SECRET = "environment_secret";

    /**
     * Get the persisted environment
     */
    public static Environment getEnvironment(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String environmentId = sharedPreferences.getString(KEY_ENV_ID, null);
        String environmentSecret = sharedPreferences.getString(KEY_ENV_SECRET, null);

        if (environmentId == null || environmentSecret == null) {
            return null;
        } else {
            return new DefaultEnvironment(environmentId, null, environmentSecret, null);
        }
    }

    /**
     * Persist the environment
     *
     * @param envId The environment ID to store, of null to remove
     * @param envSecret The environment secret to store, of null to remove
     */
    public static void storeEnvironment(Context context, String envId, String envSecret) {
        // TODO: store secret secure in production, using the keystore for example
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_ENV_ID, envId)
                .putString(KEY_ENV_SECRET, envSecret)
                .apply();
    }
}
