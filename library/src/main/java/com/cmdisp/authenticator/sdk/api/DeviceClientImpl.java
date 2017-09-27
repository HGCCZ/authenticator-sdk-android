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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.cmdisp.authenticator.sdk.api.callback.JsonObjectResponseCallback;
import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.managers.DeviceManager;
import com.cmdisp.authenticator.sdk.models.DeviceInfo;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;
import com.cmdisp.authenticator.sdk.models.RegistrationStatus;
import com.cmdisp.authenticator.sdk.util.JsonRequestBodyBuilder;

import org.json.JSONObject;

import okhttp3.Request;
import okhttp3.RequestBody;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeviceClientImpl implements DeviceClient {
    private final RestClient restClient;
    private final String appKey;
    private final DeviceInfo deviceInfo;
    private final DeviceManager deviceManager;

    public DeviceClientImpl(RestClient restClient, Context context, String appKey, DeviceManager deviceManager) {
        this.restClient = restClient;
        this.appKey = appKey;
        this.deviceInfo = new DeviceInfo(context);
        this.deviceManager = deviceManager;

        restClient.setDeviceId(deviceManager.getDeviceId());
    }

    @Override
    public void getRegistration(@NonNull Callback<DeviceRegistration> callback) {
        Request request = new Request.Builder()
                .url(restClient.getDeviceIdUrl())
                .build();

        restClient.callAsync(request, new JsonObjectResponseCallback() {

            @Override
            protected void onResponse(int statusCode, JSONObject jsonObject) {
                callback.onSuccess(parseRegistration(jsonObject));
            }

            @Override
            protected void onFailure(Exception e) {
                callback.handleOnFailure(e);
            }
        });
    }

    @Override
    public void registerPhoneNumber(@NonNull String phoneNumber, @Nullable Callback<DeviceRegistration> callback) {
        updateRegistration(phoneNumber, null, null, null, callback);
    }

    @Override
    public void verifyCode(@NonNull String verificationCode, @Nullable Callback<DeviceRegistration> callback) {
        updateRegistration(null, verificationCode, null, null, callback);
    }

    @Override
    public void updateRegistrationToken(@NonNull String registrationToken, boolean pushEnabled, @Nullable Callback<DeviceRegistration> callback) {
        updateRegistration(null, null, registrationToken, pushEnabled, callback);
    }

    @Override
    public void updateRegistration(@Nullable Callback<DeviceRegistration> callback) {
        updateRegistration(null, null, null, null, callback);
    }

    private void updateRegistration(@Nullable String phoneNumber, @Nullable String verificationCode,
                                    @Nullable String registrationToken, @Nullable Boolean pushEnabled,
                                    @Nullable Callback<DeviceRegistration> callback) {

        RequestBody body = new JsonRequestBodyBuilder()
                .add("manufacturer", deviceInfo.getManufacturer())
                .add("os_version", deviceInfo.getOSVersion())
                .add("app_version", deviceInfo.getAppVersion())
                .add("app_id", deviceInfo.getAppId())
                .add("language_code", deviceInfo.getLanguage())
                .add("model", deviceInfo.getModel())
                .add("model_id", deviceInfo.getModelId())

                .add("platform", deviceInfo.getOSName())
                .add("push_token", registrationToken)
                .add("push_enabled", pushEnabled)
                .add("phone_number", phoneNumber)
                .add("verification_code", verificationCode)
                .add("app_key", appKey)
                .build();

        boolean newDevice = restClient.getDeviceId() == null;
        Request.Builder request = new Request.Builder();
        if (newDevice) {
            request.url(restClient.getDeviceUrl()).post(body);
        } else {
            request.url(restClient.getDeviceIdUrl()).put(body);
        }

        restClient.callAsync(request.build(), new JsonObjectResponseCallback() {

            @Override
            protected void onResponse(int statusCode, JSONObject jsonObject) {
                DeviceRegistration registration = parseRegistration(jsonObject);

                if (newDevice) {
                    String deviceId = registration.getId();
                    restClient.setDeviceId(deviceId);
                    deviceManager.storeDeviceId(deviceId);
                }

                if (callback != null) {
                    callback.handleOnSuccess(registration);
                }
            }

            @Override
            protected void onFailure(Exception e) {
                if (callback != null) {
                    callback.handleOnFailure(e);
                }
            }
        });
    }

    private DeviceRegistration parseRegistration(JSONObject jsonObject) {
        return new DeviceRegistration(
                jsonObject.optString("id"),
                jsonObject.optString("phone_number"),
                RegistrationStatus.fromString(jsonObject.optString("registration_status")),
                jsonObject.optString("push_token"),
                jsonObject.optBoolean("push_enabled")
        );
    }
}
