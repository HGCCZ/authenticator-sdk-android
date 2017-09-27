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
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * AuthenticationRequest request with extra optional information on top of the minimum required set
 */
public class AuthenticationRequest extends BaseAuthenticationRequest implements Parcelable {
    private final String pin;
    private final String ip;
    private final Location location;
    private final int expiry;
    private final Date created;

    private AuthenticationRequest(Builder builder) {
        super(builder.id, builder.environmentId, builder.type);
        this.pin = builder.pin;
        this.ip = builder.ip;
        this.location = builder.location;
        this.expiry = builder.expiry;
        this.created = builder.created;
    }

    /**
     * Get PIN in case of {@link Type#OTP}
     */
    @Nullable
    public String getPin() {
        return pin;
    }

    /**
     * Get the IP of the requester
     */
    @Nullable
    public String getIp() {
        return ip;
    }

    /**
     * Get the location of the requester, based on the {@link #getIp() IP address}
     */
    @Nullable
    public Location getLocation() {
        return location;
    }

    /**
     * Get expiry (time the authentication request is open)
     *
     * @return expiry in milliseconds
     */
    public int getExpiry() {
        return expiry;
    }

    /**
     * Get date the request was created
     */
    @NonNull
    public Date getCreated() {
        return created;
    }

    /**
     * Get date the request will expire
     */
    @NonNull
    public Date getExpired() {
        return new Date(created.getTime() + (expiry * 1000));
    }

    public static final class Builder {
        private String id;
        private String environmentId;
        private Type type;
        private String pin;
        private String ip;
        private Location location;
        private int expiry;
        private Date created;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setEnvironmentId(String environmentId) {
            this.environmentId = environmentId;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setPin(String pin) {
            this.pin = pin;
            return this;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder setExpiry(int expiry) {
            this.expiry = expiry;
            return this;
        }

        public Builder setCreated(Date created) {
            this.created = created;
            return this;
        }

        public AuthenticationRequest build() {
            return new AuthenticationRequest(this);
        }
    }

    // Parcelable related:

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.pin);
        dest.writeString(this.ip);
        dest.writeParcelable(this.location, flags);
        dest.writeInt(this.expiry);
        dest.writeLong(this.created != null ? this.created.getTime() : -1);
    }

    private AuthenticationRequest(Parcel in) {
        super(in);
        this.pin = in.readString();
        this.ip = in.readString();
        this.location = in.readParcelable(Location.class.getClassLoader());
        this.expiry = in.readInt();
        long tmpCreated = in.readLong();
        this.created = tmpCreated == -1 ? null : new Date(tmpCreated);
    }

    public static final Parcelable.Creator<AuthenticationRequest> CREATOR = new Parcelable.Creator<AuthenticationRequest>() {
        @Override
        public AuthenticationRequest createFromParcel(Parcel source) {
            return new AuthenticationRequest(source);
        }

        @Override
        public AuthenticationRequest[] newArray(int size) {
            return new AuthenticationRequest[size];
        }
    };
}