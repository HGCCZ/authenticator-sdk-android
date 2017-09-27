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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * AuthenticationRequest request containing a minimum set of required information
 */
public class BaseAuthenticationRequest implements Parcelable {
    private final String id;
    private final String environmentId;
    private final Type type;

    public BaseAuthenticationRequest(String id, String environmentId, Type type) {
        this.id = id;
        this.environmentId = environmentId;
        this.type = type;
    }

    /**
     * Get the unique identifier
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Get the unique identifier of the Get {@link Environment}
     */
    @NonNull
    public String getEnvironmentId() {
        return environmentId;
    }

    /**
     * Get type
     */
    @NonNull
    public Type getType() {
        return type;
    }

    // Parcelable related:

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.environmentId);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }

    protected BaseAuthenticationRequest(Parcel in) {
        this.id = in.readString();
        this.environmentId = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : Type.values()[tmpType];
    }

    public static final Parcelable.Creator<BaseAuthenticationRequest> CREATOR = new Parcelable.Creator<BaseAuthenticationRequest>() {
        @Override
        public BaseAuthenticationRequest createFromParcel(Parcel source) {
            return new BaseAuthenticationRequest(source);
        }

        @Override
        public AuthenticationRequest[] newArray(int size) {
            return new AuthenticationRequest[size];
        }
    };
}
