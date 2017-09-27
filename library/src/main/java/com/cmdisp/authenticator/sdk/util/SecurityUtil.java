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

package com.cmdisp.authenticator.sdk.util;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {
    private static final String TAG = SecurityUtil.class.getSimpleName();
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private SecurityUtil() {
    }

    /**
     * Create JSON Web Token
     *
     * @param jsonHeader  JSON to use as header content
     * @param jsonPayload JSON to use as payload content
     * @param secret      secret used to create signature
     */
    public static String createJWT(JSONObject jsonHeader, JSONObject jsonPayload, String secret) {
        String encodedHeader = base64encode(jsonHeader.toString());
        String encodedPayload = base64encode(jsonPayload.toString());

        byte[] signatureData = (encodedHeader + "." + encodedPayload).getBytes();
        byte[] signature = hmacSHA256(signatureData, secret);
        String encodedSignature = base64encode(signature);

        return encodedHeader + '.' + encodedPayload + '.' + encodedSignature;
    }

    /**
     * Create URL-safe base64-encoded string
     *
     * @param input String to encode
     * @see #base64encode(byte[])
     */
    private static String base64encode(String input) {
        return base64encode(input.getBytes(UTF_8));
    }

    /**
     * Create URL-safe base64-encoded string
     *
     * @param bytes bytes to encode
     */
    private static String base64encode(byte[] bytes) {
        int flags = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;
        return Base64.encodeToString(bytes, flags);
    }

    /**
     * Compute RFC 2104-compliant HMAC signature
     *
     * @param data   The data to be signed
     * @param secret The signing key
     * @return The RFC 2104-compliant HMAC signature
     */
    public static byte[] hmacSHA256(byte[] data, String secret) {
        if (data == null) throw new IllegalArgumentException("Missing data to calculate the new hash");
        if (secret == null) throw new IllegalArgumentException("Missing secret to calculate mac");
        String macType = "HmacSHA256";

        try {
            Key key = new SecretKeySpec(secret.getBytes(), macType);
            Mac mac = Mac.getInstance(macType);
            mac.init(key);

            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Failed to generate HMAC-SHA256", e);
        }

        return null;
    }
}
