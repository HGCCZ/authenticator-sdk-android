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
import android.support.annotation.RestrictTo;

import com.cmdisp.authenticator.sdk.api.callback.JsonObjectResponseCallback;
import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.exceptions.AuthRequestExpiredException;
import com.cmdisp.authenticator.sdk.exceptions.HttpException;
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Status;
import com.cmdisp.authenticator.sdk.models.Type;
import com.cmdisp.authenticator.sdk.util.JsonRequestBodyBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Request;
import okhttp3.RequestBody;

import static java.net.HttpURLConnection.HTTP_GONE;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AuthClientImpl implements AuthClient {
    private final RestClient restClient;

    public AuthClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getBaseUrl() {
        return restClient.getDeviceIdUrl();
    }

    @Override
    public void updateStatus(@NonNull final BaseAuthenticationRequest authReq, @NonNull Environment env,
                             @NonNull Status status, @Nullable Callback<Status> callback) {
        String envSecret = env.getSecret();
        if (authReq.getType() != Type.INSTANT) {
            throw new IllegalArgumentException("auth request type should be INSTANT");
        } else if (envSecret == null) {
            throw new IllegalArgumentException("environment secret may not be null");
        } else {
            updateStatus(authReq.getId(), envSecret, status, callback);
        }
    }

    @Override
    public void updateStatus(@NonNull final String authReqId, @NonNull String envSecret,
                             @NonNull Status status, @Nullable Callback<Status> callback) {
        if (status != Status.APPROVED && status != Status.DENIED) {
            throw new IllegalArgumentException("auth request status should be APPROVED or DENIED");
        }

        RequestBody body = new JsonRequestBodyBuilder()
                .add("auth_status", status.getValue())
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(getBaseUrl() + "/instant/" + authReqId)
                .put(body)
                .header(HeaderInterceptor.AUTHORIZATION_SECRET, envSecret);

        try {
            String payload = new JSONObject().put("auth_id", authReqId).toString();
            requestBuilder.header(HeaderInterceptor.AUTHORIZATION_PAYLOAD, payload);
        } catch (JSONException ignored) {}

        restClient.callAsync(requestBuilder.build(), new JsonObjectResponseCallback() {
            @Override
            protected void onResponse(int statusCode, JSONObject jsonObject) {
                if (callback == null) {
                    return;
                }

                String responseStatusStr = jsonObject.optString("auth_status");
                Status responseStatus = Status.fromString(responseStatusStr);
                callback.handleOnSuccess(responseStatus);
            }

            @Override
            protected void onFailure(Exception e) {
                if (callback == null) return;

                if (e instanceof HttpException && ((HttpException) e).code() == HTTP_GONE) {
                    // gone, authentication request has expired
                    e = new AuthRequestExpiredException();
                }
                callback.handleOnFailure(e);
            }
        });
    }
}
