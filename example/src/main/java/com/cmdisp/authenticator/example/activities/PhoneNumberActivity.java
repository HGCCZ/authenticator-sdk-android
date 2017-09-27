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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmdisp.authenticator.example.R;
import com.cmdisp.authenticator.example.managers.PreferenceManager;
import com.cmdisp.authenticator.example.utils.CountryUtil;
import com.cmdisp.authenticator.sdk.Authenticator;
import com.cmdisp.authenticator.sdk.callback.MainCallback;
import com.cmdisp.authenticator.sdk.models.DeviceRegistration;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Activity for the user to enter his phone number
 */
public class PhoneNumberActivity extends AppCompatActivity {
    private static final String TAG = PhoneNumberActivity.class.getSimpleName();

    private EditText phoneNumberEditText;
    private TextInputLayout phoneNumberLayout;
    private Button nextButton;

    private PhoneNumberUtil phoneNumberUtil;
    private String userCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
        phoneNumberEditText = (EditText) findViewById(R.id.edit_text_phone_number);
        phoneNumberLayout = (TextInputLayout) findViewById(R.id.layout_phone_number);
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            String input = phoneNumberEditText.getText().toString();
            String phoneNumber = getPhoneNumber(input);
            if (phoneNumber == null) {
                phoneNumberLayout.setError(getString(R.string.phone_number_invalid));
            } else {
                registerUser(phoneNumber);
            }
        });

        userCountry = CountryUtil.getUserCountry(this);
        // generate example mobile phone number for the country of the user to use as a placeholder
        PhoneNumber phoneNumber = phoneNumberUtil.getExampleNumberForType(userCountry, PhoneNumberType.MOBILE);
        String examplePhoneNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
        if (examplePhoneNumber != null) {
            phoneNumberEditText.setHint(examplePhoneNumber);
        }
    }

    /**
     * Validate and format phone number
     * @return the phone number, or null if invalid
     */
    @Nullable
    private String getPhoneNumber(String phoneNumberStr) {
        PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneNumberUtil.parse(phoneNumberStr, userCountry);
        } catch (NumberParseException e) {
            Log.w(TAG, "Invalid phone number: " + phoneNumberStr);
            return null;
        }

        if (phoneNumberUtil.isValidNumber(phoneNumber)) {
            return phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
        } else {
            return null;
        }
    }

    /**
     * Register phone number, a verification code will be sent
     */
    private void registerUser(final String phoneNumber) {
        nextButton.setEnabled(false);
        PreferenceManager.storePhoneNumber(phoneNumber);
        Authenticator.deviceClient().registerPhoneNumber(phoneNumber, new MainCallback<DeviceRegistration>() {
            @Override
            public void onSuccess(DeviceRegistration deviceRegistration) {
                Intent intent = new Intent(PhoneNumberActivity.this, VerificationCodeActivity.class);
                startActivity(intent);
                nextButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error registering user: " + phoneNumber, e);
                Toast.makeText(PhoneNumberActivity.this, R.string.phone_number_failed, Toast.LENGTH_SHORT).show();
                nextButton.setEnabled(true);
            }
        });
    }
}
