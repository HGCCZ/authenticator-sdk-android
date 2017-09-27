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

package com.cmdisp.authenticator.example.notification;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.managers.EnvironmentManager;
import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.callback.MainCallback;
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Status;

/**
 * Started when a button in the notification was clicked
 */
public class InstantActionIntentService extends IntentService {
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_AUTH_REQ = "auth_req";

    private static final String TAG = InstantActionIntentService.class.getSimpleName();

    public InstantActionIntentService() {
        super("InstantActionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }

        Status action = (Status) intent.getSerializableExtra(EXTRA_ACTION);
        BaseAuthenticationRequest authReq = intent.getParcelableExtra(EXTRA_AUTH_REQ);

        if (authReq == null) return;

        // dismiss the notification
        NotificationHandler.cancelAuthReqNotification(this);

        Environment env = EnvironmentManager.getEnvironment(this);
        Authenticator.authClient().updateStatus(authReq, env, action,
                new UpdateStatusCallback(getApplicationContext()));
    }

    private static class UpdateStatusCallback extends MainCallback<Status> {
        private Context context;

        private UpdateStatusCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onSuccess(Status status) {
            switch (status) {
                case APPROVED:
                    Toast.makeText(context, R.string.auth_success_approved, Toast.LENGTH_SHORT).show();
                    break;
                case DENIED:
                    Toast.makeText(context, R.string.auth_success_denied, Toast.LENGTH_SHORT).show();
                    break;
                case EXPIRED:
                    Toast.makeText(context, R.string.auth_error_expired, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, "Error authenticating request", e);
            Toast.makeText(context, R.string.auth_error_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
