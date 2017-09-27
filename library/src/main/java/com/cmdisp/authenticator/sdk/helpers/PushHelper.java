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

package com.cmdisp.authenticator.sdk.helpers;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.cmdisp.authenticator.sdk.api.EnvironmentClient;
import com.cmdisp.authenticator.sdk.callback.Callback;
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Type;

import java.util.Map;

/**
 * Helps to retrieve authentication information from a push message
 */
public class PushHelper {
    private final PushData data;
    private final PushType type;

    /**
     * @param gcmData data from the GCM push message
     */
    public PushHelper(@NonNull Bundle gcmData) {
        this(new GcmPushData(gcmData));
    }

    /**
     * @param fcmData data from the FCM push message
     */
    public PushHelper(@NonNull Map<String, String> fcmData) {
        this(new FcmPushData(fcmData));
    }

    private PushHelper(PushData data) {
        this.data = data;
        String rawType = data.getString("type");
        this.type = PushType.fromString(rawType);
    }

    /**
     * Get the type of this push message
     */
    public PushType getType() {
        return type;
    }

    /**
     * Create authentication request from the push message
     * <p>
     * Retrieve the full authentication using {@link EnvironmentClient#getAuthenticationRequest(String, String, Callback)}
     */
    public BaseAuthenticationRequest getAuthenticationRequest() {
        Type authType;
        switch (type) {
            case INSTANT:
                authType = Type.INSTANT;
                break;
            case OTP:
                authType = Type.OTP;
                break;
            default:
                return null;
        }

        String authId = data.getString("authId");
        String envId = data.getString("envId");
        return new BaseAuthenticationRequest(authId, envId, authType);
    }

    /**
     * Get the message body from the push message
     */
    public String getMessage() {
        return data.getString("message");
    }

    public enum PushType {
        INSTANT("auth_instant"),
        OTP("auth_otp"),
        QR("auth_qr"),
        PLAIN(null);

        private final String value;

        PushType(String value) {
            this.value = value;
        }

        private static PushType fromString(String text) {
            if (text == null) return PushType.PLAIN;

            for (PushType t : PushType.values()) {
                if (text.equals(t.value)) {
                    return t;
                }
            }

            return PushType.PLAIN;
        }
    }

    private interface PushData {
        String getString(String key);
    }

    private static class GcmPushData implements PushData {
        private Bundle bundle;

        GcmPushData(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public String getString(String key) {
            return bundle.getString(key);
        }
    }

    private static final class FcmPushData implements PushData {
        private Map<String, String> map;

        FcmPushData(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public String getString(String key) {
            return map.get(key);
        }
    }
}
