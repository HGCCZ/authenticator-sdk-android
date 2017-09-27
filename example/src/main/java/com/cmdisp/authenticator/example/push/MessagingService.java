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

package com.cmdisp.authenticator.example.push;

import android.content.Intent;

import com.cmdisp.authenticator.sdk.helpers.PushHelper;
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PushHelper pushHelper = new PushHelper(remoteMessage.getData());

        Intent broadcast;
        switch (pushHelper.getType()) {
            case INSTANT:
            case OTP:
                BaseAuthenticationRequest authReq = pushHelper.getAuthenticationRequest();
                broadcast = new Intent(PushConstants.ACTION_AUTH_REQ);
                broadcast.putExtra(PushConstants.EXTRA_AUTH_REQ, authReq);
                break;
            case QR:
                broadcast = new Intent(PushConstants.ACTION_QR);
                break;
            default:
                broadcast = new Intent(PushConstants.ACTION_PLAIN);
                break;
        }

        broadcast.putExtra(PushConstants.EXTRA_MESSAGE, pushHelper.getMessage());
        // notify the broadcast receivers in order of their priority
        sendOrderedBroadcast(broadcast, null);
    }
}
