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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.managers.PreferenceManager;
import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.callback.MainCallback;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;
import com.cmdisp.authenticator.sdk.models.RegistrationStatus;

public class VerificationCodeActivity extends AppCompatActivity {
    private static final String TAG = VerificationCodeActivity.class.getSimpleName();

    private EditText verificationCodeEditText;
    private TextInputLayout verificationCodeLayout;
    private Button verifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        verificationCodeEditText = (EditText) findViewById(R.id.edit_text_verification_code);
        verificationCodeLayout = (TextInputLayout) findViewById(R.id.layout_verification_code);
        verifyButton = (Button) findViewById(R.id.verify_button);
        verifyButton.setOnClickListener(v -> {
            String verificationCode = verificationCodeEditText.getText().toString();
            verifyCode(verificationCode);
        });
    }

    /**
     * Check if the verification code is valid
     */
    private void verifyCode(String verificationCode) {
        if (verificationCode == null || verificationCode.isEmpty()) {
            verificationCodeLayout.setError(getString(R.string.verification_code_invalid));
            return;
        }

        verifyButton.setEnabled(false);
        Authenticator.deviceClient().verifyCode(verificationCode, new MainCallback<DeviceRegistration>() {
            @Override
            public void onSuccess(DeviceRegistration deviceRegistration) {
                RegistrationStatus status = deviceRegistration.getStatus();

                switch (status) {
                    case VERIFIED:
                        PreferenceManager.storeRegistered(true);
                        Intent intent = new Intent(VerificationCodeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    default:
                        Log.wtf(TAG, "Verification status is " + status.name());
                        verificationCodeLayout.setError(getString(R.string.verification_code_invalid));
                        break;
                }

                verifyButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error registering user", e);
                verificationCodeLayout.setError(getString(R.string.verification_code_failed));
                verifyButton.setEnabled(true);
            }
        });
    }
}
