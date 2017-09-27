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

package com.cmdisp.authenticator.sdk.api.callback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Response;

public abstract class JsonArrayResponseCallback extends BaseCallback {

    @Override
    protected void onResponse(Response response) throws IOException {
        //noinspection ConstantConditions
        String body = response.body().string();
        if (body.isEmpty()) {
            onFailure(new IOException("Response body is empty"));
            return;
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(body);
        } catch (JSONException e) {
            onFailure(new IOException("Could not parse JSON", e));
            return;
        }

        onResponse(response.code(), jsonArray);
    }

    protected abstract void onResponse(int statusCode, JSONArray jsonArray);
}
