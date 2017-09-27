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
import com.cmdisp.authenticator.sdk.models.AuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Environment;

import java.util.List;

public interface EnvironmentClient {
    /**
     * Get all currently configured environments
     */
    void getEnvironments(@NonNull Callback<List<Environment>> callback);

    /**
     * Link environment to this device using info required from the QR code
     *
     * @param id     environment id
     * @param secret environment secret
     */
    void register(@NonNull String id, @NonNull String secret, @Nullable Callback<Void> callback);

    /**
     * Remove environment link to this device
     *
     * @param env environment to delete
     * @see #unregister(String, String, Callback)
     */
    void unregister(@NonNull Environment env, @Nullable Callback<Void> callback);

    /**
     * Remove environment link to this device
     *
     * @param id     environment id
     * @param secret environment secret
     */
    void unregister(@NonNull String id, @NonNull String secret, @Nullable Callback<Void> callback);

    /**
     * Get current authentication request (if any) for a specific environment
     *
     * @param env environment to get authentication requests for
     * @see #getAuthenticationRequest(String, String, Callback)
     */
    void getAuthenticationRequest(@NonNull Environment env, @NonNull Callback<AuthenticationRequest> callback);

    /**
     * Get current authentication request (if any) for a specific environment
     *
     * @param id     environment id
     * @param secret environment secret
     */
    void getAuthenticationRequest(@NonNull String id, @NonNull String secret, @NonNull Callback<AuthenticationRequest> callback);
}
