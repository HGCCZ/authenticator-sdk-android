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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.managers.EnvironmentManager;
import com.cmdisp.authenticator.example.managers.PreferenceManager;
import com.cmdisp.authenticator.example.push.PushConstants;
import com.cmdisp.authenticator.example.utils.StringUtil;
import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.callback.MainCallback;
import com.cmdisp.authenticator.sdk.models.AuthenticationRequest;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;
import com.cmdisp.authenticator.sdk.models.Environment;
import com.cmdisp.authenticator.sdk.models.Location;
import com.cmdisp.authenticator.sdk.models.RegistrationStatus;
import com.cmdisp.authenticator.sdk.models.Status;
import com.cmdisp.authenticator.sdk.models.Type;

import java.text.DateFormat;

/**
 * Activity which shows information about open authentication requests
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private View viewAuthReqPlaceholder;
    private View viewAuthReqInfo;
    private View viewAuthReqInstant;
    private TextView textViewPhoneNumber;
    private TextView textViewDate;
    private TextView textViewIp;
    private TextView textViewLocation;
    private TextView textViewTimer;
    private TextView textViewOtpTitle;
    private TextView textViewOtp;

    private Environment environment;
    private AuthenticationRequest authReq;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewAuthReqPlaceholder = findViewById(R.id.text_view_auth_req_placeholder);
        viewAuthReqInfo = findViewById(R.id.layout_auth_req_information);
        viewAuthReqInstant = findViewById(R.id.layout_auth_req_instant);

        textViewPhoneNumber = (TextView) findViewById(R.id.text_view_phone_number);
        textViewDate = (TextView) findViewById(R.id.text_view_date);
        textViewIp = (TextView) findViewById(R.id.text_view_ip);
        textViewLocation = (TextView) findViewById(R.id.text_view_location);
        textViewTimer = (TextView) findViewById(R.id.text_view_timer);
        textViewOtpTitle = (TextView) findViewById(R.id.text_view_otp_title);
        textViewOtp = (TextView) findViewById(R.id.text_view_otp);

        findViewById(R.id.button_authentication_approve).setOnClickListener(onAuthClickListener);
        findViewById(R.id.button_authentication_deny).setOnClickListener(onAuthClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPushReceiver();
        environment = EnvironmentManager.getEnvironment(this);
        getAuthenticationRequest();
        checkRegistrationStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterPushReceiver();
        stopCountdownTimer();
    }

    /**
     * Called when opened from the notification (because of the singleTask launch mode)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getAuthenticationRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                startActivity(new Intent(this, ScanActivity.class));
                return true;
            case R.id.action_unregister:
                // unregister the environment, useful for testing
                if (environment == null) return true;
                Authenticator.environmentClient().unregister(environment, new MainCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        EnvironmentManager.storeEnvironment(MainActivity.this, null, null);
                        environment = null;
                    }

                    @Override
                    public void onFailure(Exception e) { }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check for authentication requests
     */
    private void getAuthenticationRequest() {
        if (environment == null) return;

        Authenticator.environmentClient().getAuthenticationRequest(environment, new MainCallback<AuthenticationRequest>() {
            @Override
            public void onSuccess(AuthenticationRequest authReq) {
                if (authReq == null) {
                    showPlaceholder(true);
                } else {
                    setAuthenticationRequest(authReq);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error getting authentication request for environment", e);
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show the authentication request
     */
    private void setAuthenticationRequest(AuthenticationRequest authReq) {
        // cancel previous timer
        stopCountdownTimer();

        long timeRemaining = authReq.getExpired().getTime() - System.currentTimeMillis();

        // stop if expired
        if (timeRemaining <= 0) return;
        this.authReq = authReq;

        // start timer and schedule expiration of the request
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long msUntilFinished) {
                String text = getString(R.string.authentication_seconds_left, msUntilFinished/1000);
                textViewTimer.setText(text);
            }

            @Override
            public void onFinish() {
                textViewTimer.setText(getString(R.string.authentication_seconds_left, 0));
                Toast.makeText(MainActivity.this, R.string.auth_error_expired, Toast.LENGTH_SHORT).show();
                showPlaceholder(true);
            }
        }.start();

        // show info of the authentication request
        String phoneNumber = PreferenceManager.getPhoneNumber();
        textViewPhoneNumber.setText(phoneNumber);
        String date = DATE_TIME_FORMAT.format(authReq.getCreated());
        textViewDate.setText(date);
        String ipAddress = authReq.getIp();
        textViewIp.setText(ipAddress);

        Location loc = authReq.getLocation();
        if (loc == null) {
            textViewLocation.setText(R.string.unknown);
        } else {
            String location = StringUtil.join(", ", loc.getCity(), loc.getRegion(), loc.getCountryCode());
            textViewLocation.setText(location);
        }

        // show the instant or OTP views based on the authentication request type
        boolean isInstant = (authReq.getType() == Type.INSTANT);
        viewAuthReqInstant.setVisibility(isInstant ? View.VISIBLE : View.GONE);
        textViewOtpTitle.setVisibility(isInstant ? View.GONE : View.VISIBLE);
        textViewOtp.setVisibility(isInstant ? View.GONE : View.VISIBLE);
        textViewOtp.setText(authReq.getPin());
        showPlaceholder(false);
    }

    /**
     * Hide or show the 'no open requests' placeholder
     */
    private void showPlaceholder(boolean show) {
        viewAuthReqPlaceholder.setVisibility(show ? View.VISIBLE : View.GONE);
        viewAuthReqInfo.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Stop the countdown timer, if running
     */
    private void stopCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * Check if the device registration is still verified.
     * If not, reset, remove the environment and show the registration screen.
     */
    private void checkRegistrationStatus() {
        Authenticator.deviceClient().getRegistration(new MainCallback<DeviceRegistration>() {
            @Override
            public void onSuccess(DeviceRegistration deviceRegistration) {
                RegistrationStatus status = deviceRegistration.getStatus();
                if (status != RegistrationStatus.VERIFIED) {
                    Log.d(TAG, "Resetting login, verification status: " + status.name());
                    PreferenceManager.storeRegistered(false);
                    EnvironmentManager.storeEnvironment(MainActivity.this, null, null);

                    Intent intent = new Intent(MainActivity.this, StartupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error getting verification status", e);
            }
        });
    }

    /**
     * Start listening for push messages
     */
    private void registerPushReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PushConstants.ACTION_AUTH_REQ);
        intentFilter.addAction(PushConstants.ACTION_QR);
        intentFilter.setPriority(1);
        registerReceiver(pushBroadcastReceiver, intentFilter);
    }

    /**
     * Stop listening for push messages
     */
    private void unregisterPushReceiver() {
        unregisterReceiver(pushBroadcastReceiver);
    }

    /**
     * BroadcastReceiver to handle push messages:
     * <ul>
     *     <li>If authentication request push, get and show the request.</li>
     *     <li>If QR code push, start the QR code scanner activity</li>
     * </ul>
     * Finally abort the broadcast so no notification will be shown
     */
    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (PushConstants.ACTION_AUTH_REQ.equals(action)) {
                getAuthenticationRequest();
                abortBroadcast();
            } else if (PushConstants.ACTION_QR.equals(action)) {
                Intent qrIntent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(qrIntent);
                abortBroadcast();
            }
        }
    };

    private View.OnClickListener onAuthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPlaceholder(true);
            stopCountdownTimer();

            Status result;
            // determine the result based on the view being clicked
            if (v.getId() == R.id.button_authentication_approve) {
                result = Status.APPROVED;
            } else {
                result = Status.DENIED;
            }

            Authenticator.authClient().updateStatus(authReq, environment, result, new MainCallback<Status>() {
                @Override
                public void onSuccess(Status status) {
                    switch (status) {
                        case APPROVED:
                            Toast.makeText(MainActivity.this, R.string.auth_success_approved, Toast.LENGTH_SHORT).show();
                            break;
                        case DENIED:
                            Toast.makeText(MainActivity.this, R.string.auth_success_denied, Toast.LENGTH_SHORT).show();
                            break;
                        case EXPIRED:
                            Toast.makeText(MainActivity.this, R.string.auth_error_expired, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(MainActivity.this, R.string.auth_error_failed, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to authenticate request", e);
                }
            });
        }
    };
}
