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
import android.util.Log;

import com.cmdisp.authenticator.sdk.api.callback.BaseCallback;
import com.cmdisp.authenticator.sdk.api.callback.JsonArrayResponseCallback;
import com.cmdisp.authenticator.sdk.api.callback.SuccessCallback;
import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.exceptions.EnvironmentInvalidException;
import com.cmdisp.authenticator.sdk.exceptions.HttpException;
import com.cmdisp.authenticator.sdk.models.AuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.DefaultEnvironment;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Location;
import com.cmdisp.authenticator.sdk.models.Type;
import com.cmdisp.authenticator.sdk.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EnvironmentClientImpl implements EnvironmentClient {
    private static final String TAG = EnvironmentClientImpl.class.getSimpleName();
    private static final DateFormat DATE_FORMAT_ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
    private final RestClient restClient;

    public EnvironmentClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getBaseUrl() {
        return restClient.getDeviceIdUrl() + "/environment";
    }

    @Override
    public void getEnvironments(@NonNull Callback<List<Environment>> callback) {
        Request request = new Request.Builder()
                .url(getBaseUrl())
                .build();

        restClient.callAsync(request, new JsonArrayResponseCallback() {
            @Override
            protected void onResponse(int statusCode, JSONArray jsonArray) {
                int length = jsonArray.length();
                List<Environment> environments = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    if (jsonObject == null) {
                        Log.wtf(TAG, "JsonObject shouldn't be null");
                        continue;
                    }

                    Environment env = new DefaultEnvironment(
                            jsonObject.optString("id"),
                            jsonObject.optString("name"),
                            null,
                            jsonObject.optString("icon_url")
                    );
                    environments.add(env);
                }

                callback.handleOnSuccess(environments);
            }

            @Override
            protected void onFailure(Exception e) {
                callback.handleOnFailure(e);
            }
        });
    }

    @Override
    public void register(@NonNull String id, @NonNull String secret, @Nullable Callback<Void> callback) {
        Request request = new Request.Builder()
                .url(getBaseUrl() + "/" + id)
                .put(RestClient.EMPTY_BODY)
                .header(HeaderInterceptor.AUTHORIZATION_SECRET, secret)
                .build();

        restClient.callAsync(request, new BaseCallback() {

            @Override
            protected void onResponse(Response response) throws IOException {
                //noinspection ConstantConditions
                response.body().close();
                if (callback != null) {
                    callback.handleOnSuccess(null);
                }
            }

            @Override
            protected void onFailure(Exception e) {
                if (callback == null) return;

                if (e instanceof HttpException && ((HttpException) e).code() == HTTP_UNAUTHORIZED) {
                    // unauthorized, environment is invalid
                    e = new EnvironmentInvalidException();
                }
                callback.handleOnFailure(e);
            }
        });
    }

    @Override
    public void unregister(@NonNull Environment env, @Nullable Callback<Void> callback) {
        unregister(env.getId(), env.getSecret(), callback);
    }

    @Override
    public void unregister(@NonNull String id, @NonNull String secret, @Nullable Callback<Void> callback) {
        Request request = new Request.Builder()
                .url(getBaseUrl() + "/" + id)
                .delete()
                .header(HeaderInterceptor.AUTHORIZATION_SECRET, secret)
                .build();

        restClient.callAsync(request, new SuccessCallback() {
            @Override
            public void onSuccess(int statusCode) {
                if (callback != null) {
                    callback.handleOnSuccess(null);
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

    @Override
    public void getAuthenticationRequest(@NonNull final Environment env, @NonNull Callback<AuthenticationRequest> callback) {
        getAuthenticationRequest(env.getId(), env.getSecret(), callback);
    }

    @Override
    public void getAuthenticationRequest(@NonNull String id, @NonNull String secret, @NonNull Callback<AuthenticationRequest> callback) {
        Request request = new Request.Builder()
                .url(getBaseUrl() + "/" + id + "/auth")
                .get()
                .header(HeaderInterceptor.AUTHORIZATION_SECRET, secret)
                .build();

        restClient.callAsync(request, new JsonArrayResponseCallback() {
            @Override
            protected void onResponse(int statusCode, JSONArray jsonArray) {
                if (jsonArray.length() > 1) {
                    Log.wtf(TAG, "There should 1 request at max");
                }

                JSONObject jsonObject = jsonArray.optJSONObject(0);
                if (jsonObject == null) {
                    callback.handleOnSuccess(null);
                    return;
                }

                Date created = null;
                String dateStr = jsonObject.optString("created_at");
                if (dateStr != null) {
                    try {
                        created = DATE_FORMAT_ISO_8601.parse(dateStr);
                    } catch (ParseException e) {
                        Log.e(TAG, "Could not parse date: " + dateStr, e);
                    }
                }

                JSONObject jsonLocation = jsonObject.optJSONObject("geoip");
                Location location = null;
                if (jsonLocation != null) {
                    location = new Location.Builder()
                            .setCountryCode(JsonUtil.optString(jsonLocation, "country_code"))
                            .setRegion(JsonUtil.optString(jsonLocation, "region"))
                            .setCity(JsonUtil.optString(jsonLocation, "city"))
                            .setLatitude(JsonUtil.optDouble(jsonLocation, "latitude"))
                            .setLongitude(JsonUtil.optDouble(jsonLocation, "longitude"))
                            .build();
                }

                AuthenticationRequest authentication = new AuthenticationRequest.Builder()
                        .setId(JsonUtil.optString(jsonObject, "id"))
                        .setEnvironmentId(id)
                        .setType(Type.fromString(jsonObject.optString("auth_type")))
                        .setPin(JsonUtil.optString(jsonObject, "pin"))
                        .setIp(JsonUtil.optString(jsonObject, "ip"))
                        .setLocation(location)
                        .setExpiry(jsonObject.optInt("expiry", 60))
                        .setCreated(created)
                        .build();

                callback.handleOnSuccess(authentication);
            }

            @Override
            protected void onFailure(Exception e) {
                callback.handleOnFailure(e);
            }
        });
    }
}
