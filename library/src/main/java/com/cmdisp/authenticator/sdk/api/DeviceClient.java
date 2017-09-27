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
import android.support.annotation.Nullable;

import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;

public interface DeviceClient {

    /**
     * Get the
     */
    void getRegistration(@NonNull Callback<DeviceRegistration> callback);

    /**
     * Register the user by his phone number
     *
     * @param phoneNumber phone number of the user
     */
    void registerPhoneNumber(@NonNull String phoneNumber, @Nullable Callback<DeviceRegistration> callback);

    /**
     * Verify the phone number of the user using the code received via SMS
     *
     * @param verificationCode code received by the user
     */
    void verifyCode(@NonNull String verificationCode, @Nullable Callback<DeviceRegistration> callback);

    /**
     * Update the push information
     *
     * @param registrationToken the registration (push) token obtained from GCM/FCM
     * @param pushEnabled whether to use push messages or fall back to SMS
     */
    void updateRegistrationToken(@NonNull String registrationToken, boolean pushEnabled, @Nullable Callback<DeviceRegistration> callback);

    /**
     * Update the device info
     */
    void updateRegistration(@Nullable Callback<DeviceRegistration> callback);
}
