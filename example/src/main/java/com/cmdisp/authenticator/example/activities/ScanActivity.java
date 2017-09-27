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

package com.cmdisp.authenticator.example.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.managers.EnvironmentManager;
import com.cmdisp.authenticator.example.push.PushConstants;
import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.callback.MainCallback;
import com.cmdisp.authenticator.sdk.exceptions.EnvironmentInvalidException;
import com.cmdisp.authenticator.sdk.helpers.QrCodeHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Activity which asks for the camera permission and shows a QR Scanner.
 * Information in this QR code is used to register the environment.
 */
public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA = 0;

    private ZXingScannerView qrScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        qrScannerView = new ZXingScannerView(this);
        qrScannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
        qrScannerView.setBorderColor(ContextCompat.getColor(this, R.color.green_dark));
        qrScannerView.setBorderStrokeWidth(10);
        qrScannerView.setIsBorderCornerRounded(true);
        qrScannerView.setSquareViewFinder(true);
        qrScannerView.setLaserEnabled(false);
        qrScannerView.setMaskColor(Color.TRANSPARENT);
        setContentView(qrScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
        unregisterBroadcastReceiver();
    }

    private void startCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // camera permissions is already available
            qrScannerView.setResultHandler(this);
            qrScannerView.startCamera();
        } else {
            // camera permission has not been granted
            requestPermission();
        }
    }

    private void stopCamera() {
        qrScannerView.setResultHandler(null);
        qrScannerView.stopCamera();
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.scanner_permission_title)
                    .setMessage(R.string.scanner_permission_rationale)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissionNow())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                    .show();
        } else {
            requestPermissionNow();
        }
    }

    private void requestPermissionNow() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // try again with an additional explanation
                    requestPermission();
                } else {
                    // user checked 'never ask again'
                    Toast.makeText(this, R.string.scanner_permission_rationale, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Called when a QR code is detected
     */
    @Override
    public void handleResult(Result rawResult) {
        String text = rawResult.getText();
        QrCodeHelper qrCodeHelper = new QrCodeHelper(text);

        // stop if the QR code is certainly not valid
        if (!qrCodeHelper.isValidQrCode()) {
            Log.d(TAG, "Invalid QR code: " + text);
            showErrorAndResume(R.string.scanner_code_invalid);
            return;
        }

        String envId = qrCodeHelper.getId();
        String envSecret = qrCodeHelper.getSecret();

        Authenticator.environmentClient().register(envId, envSecret, new MainCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // save the environment and go to the main activity
                EnvironmentManager.storeEnvironment(ScanActivity.this, envId, envSecret);
                navigateActivityUp();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error registering environment", e);
                if (e instanceof EnvironmentInvalidException) {
                    showErrorAndResume(R.string.scanner_code_invalid);
                } else {
                    showErrorAndResume(R.string.error);
                }
            }
        });
    }

    private void showErrorAndResume(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
        qrScannerView.resumeCameraPreview(ScanActivity.this);
    }

    /**
     * Go to the parent activity by starting it if necessary. Because if this activity
     * was started from the notification, the parent activity isn't started.
     */
    protected void navigateActivityUp() {
        // this could be started from the notification, so create parent activity if it doesn't exist
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    /**
     * Start listening for scan QR push messages
     */
    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(PushConstants.ACTION_QR);
        intentFilter.setPriority(1);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Stop listening for push messages
     */
    private void unregisterBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Stop the scan QR broadcast to prevent the notification from showing up
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (PushConstants.ACTION_QR.equals(action)) {
                abortBroadcast();
            }
        }
    };
}
