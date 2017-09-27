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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.activities.MainActivity;
import com.cmdisp.authenticator.example.activities.ScanActivity;
import com.cmdisp.authenticator.example.managers.EnvironmentManager;
import com.cmdisp.authenticator.sdk.models.BaseAuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Status;
import com.cmdisp.authenticator.sdk.models.Type;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHandler {
    private static final int TYPE_DEFAULT = 1;
    private static final int TYPE_AUTH_REQ = 2;
    private static final int TYPE_QR = 3;

    // generate request codes for pending intents, so the intents are not "equal"
    private static AtomicInteger requestCodeGen = new AtomicInteger(0);

    private NotificationHandler() {
    }

    /**
     * Show notification containing the authentication request
     */
    static void showAuthReq(Context context, BaseAuthenticationRequest authReq, String message) {
        Environment env = EnvironmentManager.getEnvironment(context);

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pMainIntent = PendingIntent.getActivity(context, requestCodeGen.incrementAndGet(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(env.getName())
                .setContentIntent(pMainIntent)
                .setSmallIcon(R.drawable.lock_open_outline)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        if (authReq.getType() == Type.INSTANT) {
            // create actions for the approve/deny buttons in the notification
            Intent approveIntent = new Intent(context, InstantActionIntentService.class);
            approveIntent.putExtra(InstantActionIntentService.EXTRA_ACTION, Status.APPROVED);
            approveIntent.putExtra(InstantActionIntentService.EXTRA_AUTH_REQ, authReq);
            PendingIntent pApproveIntent = PendingIntent.getService(context, requestCodeGen.incrementAndGet(), approveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent denyIntent = new Intent(context, InstantActionIntentService.class);
            denyIntent.putExtra(InstantActionIntentService.EXTRA_ACTION, Status.DENIED);
            denyIntent.putExtra(InstantActionIntentService.EXTRA_AUTH_REQ, authReq);
            PendingIntent pDenyIntent = PendingIntent.getService(context, requestCodeGen.incrementAndGet(), denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentText(context.getString(R.string.notification_auth))
                    .addAction(R.drawable.check_small, context.getString(R.string.approve), pApproveIntent)
                    .addAction(R.drawable.close_small, context.getString(R.string.deny), pDenyIntent);
        } else {
            builder.setContentText(message);
        }

        NotificationManagerCompat.from(context).notify(TYPE_AUTH_REQ, builder.build());
    }

    /**
     * Show notification for the user to scan the QR code
     */
    static void showQR(Context context) {
        Intent intent = new Intent(context, ScanActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, requestCodeGen.incrementAndGet(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_qr))
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.lock_open_outline)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(TYPE_QR, notification);
    }

    /**
     * Show a regular notification
     */
    static void showDefault(Context context, String message) {
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.lock_open_outline)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(TYPE_DEFAULT, notification);
    }

    static void cancelAuthReqNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(TYPE_AUTH_REQ);
    }
}
