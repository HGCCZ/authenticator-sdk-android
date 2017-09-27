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
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Status;
import com.cmdisp.authenticator.sdk.models.Type;

public interface AuthClient {
    /**
     * Update the open authentication request by changing it's status
     *
     * @param auth   authentication request of type {@link Type#INSTANT INSTANT} with status {@link Status#OPEN OPEN}
     * @param env    environment matching the authentication request
     * @param status new status, either {@link Status#APPROVED} or {@link Status#DENIED}
     * @see #updateStatus(BaseAuthenticationRequest, Environment, Status, Callback)
     */
    void updateStatus(@NonNull BaseAuthenticationRequest auth, @NonNull Environment env,
                      @NonNull Status status, @Nullable Callback<Status> callback);

    /**
     * Update the open authentication request by changing it's status
     *
     * @param authId    authentication request id of type {@link Type#INSTANT INSTANT} with status {@link Status#OPEN OPEN}
     * @param envSecret environment secret matching the authentication request
     * @param status    new status, either {@link Status#APPROVED} or {@link Status#DENIED}
     */
    void updateStatus(@NonNull String authId, @NonNull String envSecret,
                      @NonNull Status status, @Nullable Callback<Status> callback);
}
