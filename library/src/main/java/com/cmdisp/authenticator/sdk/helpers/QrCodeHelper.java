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

/**
 * Helps to parse the environment information from the QR code.
 * Format of the content of the QR code is "{environmentUUID},{secret}"
 */
public class QrCodeHelper {
    private static final int UUID_LENGTH = 36;
    private String content;
    private int separatorIndex;

    /**
     * @param content content of the scanned QR code
     */
    public QrCodeHelper(String content) {
        this.content = content;
        this.separatorIndex = content.indexOf(',');
    }

    /**
     * Gives an indication whether the QR code is a valid Authenticator QR code by checking the format
     */
    public boolean isValidQrCode() {
        // give an indication by checking if the first part has the length of a UUID
        return separatorIndex == UUID_LENGTH;
    }

    /**
     * Get the environment ID
     */
    public String getId() {
        if (isValidQrCode()) {
            return content.substring(0, separatorIndex);
        } else {
            return null;
        }
    }

    /**
     * Get the environment secret
     */
    public String getSecret() {
        if (isValidQrCode()) {
            return content.substring(separatorIndex + 1);
        } else {
            return null;
        }
    }
}
