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

package com.cmdisp.authenticator.sdk.exceptions;

import java.io.IOException;

import okhttp3.Response;

/**
 * HTTP call was unsuccessful
 */
public class HttpException extends IOException {
    private final int code;
    private final String message;
    private final String url;

    public HttpException(Response response) {
        this(response.code(), response.message(), response.request().url().toString());
    }

    public HttpException(int code, String message, String url) {
        super("HTTP " + code + " " + message + " for " + url);
        this.code = code;
        this.message = message;
        this.url = url;
    }

    /**
     * HTTP status code
     */
    public int code() {
        return code;
    }

    /**
     * HTTP status message
     */
    public String message() {
        return message;
    }

    /**
     * URL
     */
    public String url() {
        return url;
    }
}
