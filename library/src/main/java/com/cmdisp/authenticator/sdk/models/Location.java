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
import android.support.annotation.Nullable;

public class Location implements Parcelable {
    private final String countryCode;
    private final String region;
    private final String city;
    private final Double latitude;
    private final Double longitude;

    private Location(Builder builder) {
        this.countryCode = builder.countryCode;
        this.region = builder.region;
        this.city = builder.city;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
    }

    /**
     * Get country code
     *
     * @return 2-letter ISO country code
     */
    @Nullable
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Get region
     */
    @Nullable
    public String getRegion() {
        return region;
    }

    /**
     * Get city
     */
    @Nullable
    public String getCity() {
        return city;
    }

    /**
     * Get latitude coordinates
     */
    @Nullable
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Get longitude coordinates
     */
    @Nullable
    public Double getLongitude() {
        return longitude;
    }

    public static final class Builder {
        private String countryCode;
        private String region;
        private String city;
        private Double latitude;
        private Double longitude;

        public Builder() {
        }

        public Builder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder setRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this;
        }

        public Builder setLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Location build() {
            if (latitude == null || longitude == null || (latitude == 0d && longitude == 0d)) {
                latitude = null;
                longitude = null;
            }
            return new Location(this);
        }
    }

    // Parcelable related:

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.countryCode);
        dest.writeString(this.region);
        dest.writeString(this.city);
        dest.writeValue(this.latitude);
        dest.writeValue(this.longitude);
    }

    private Location(Parcel in) {
        this.countryCode = in.readString();
        this.region = in.readString();
        this.city = in.readString();
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
}
