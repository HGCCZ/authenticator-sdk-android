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

public class PreferenceManager {
    private static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".preferences";
    private static final String KEY_REGISTERED = "registered";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    private static SharedPreferences sharedPreferences;

    private PreferenceManager() {
    }

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isRegistered() {
        return sharedPreferences.getBoolean(KEY_REGISTERED, false);
    }

    public static void storeRegistered(boolean registered) {
        sharedPreferences.edit().putBoolean(KEY_REGISTERED, registered).apply();
    }

    public static String getPhoneNumber() {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null);
    }

    public static void storePhoneNumber(String phoneNumber) {
        sharedPreferences.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply();
    }
}
