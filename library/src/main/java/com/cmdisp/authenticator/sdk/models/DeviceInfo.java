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

package com.cmdisp.authenticator.sdk.models;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

public class DeviceInfo {
    private final Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }

    /**
     * Get device manufacturer
     */
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * Get device model
     */
    public String getModel() {
        return Build.MODEL;
    }

    /**
     * Get model type
     */
    public String getModelId() {
        return Build.PRODUCT;
    }

    /**
     * Get the OS name
     */
    public String getOSName() {
        return "Android";
    }

    /**
     * Get the OS version
     */
    public String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Get ID of the app
     */
    public String getAppId() {
        return context.getPackageName();
    }

    /**
     * Get version of the app
     */
    public String getAppVersion() {
        try {
            String packageName = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            Log.wtf(DeviceInfo.class.getSimpleName(), "Could not get package info", e);
            return null;
        }
    }

    /**
     * Get language of the device
     */
    public String getLanguage() {
        return Locale.getDefault().getLanguage();
    }
}
