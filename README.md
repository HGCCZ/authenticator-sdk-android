# Authenticator SDK for Android

[![Bintray](https://img.shields.io/bintray/v/cmdisp/authenticator/authenticator-sdk.svg)](https://bintray.com/cmdisp/authenticator/authenticator-sdk/_latestVersion)
[![license](https://img.shields.io/github/license/cmdisp/authenticator-sdk-android.svg)](https://github.com/cmdisp/authenticator-sdk-android)


SDK guide for Android developers

## Setup

### Prerequisites

- [Register](https://dashboard.auth.cmtelecom.com/) an Authenticator environment
- GCM/FCM Sender ID (can be retrieved from the [Google Developers Console](https://developers.google.com/mobile/add?platform=android&cntapi=gcm) or [Firebase Console](https://console.firebase.google.com/))

### Configuration

The library is hosted in the Maven repository Bintray. Add the dependency to your app's `build.gradle`:

```groovy
dependencies {
    compile 'com.cmdisp.authenticator:authenticator-sdk:1.0.0'
}
```



## Usage

### Initialization / startup

1. [Initialize the Authenticator SDK](#initialize-the-sdk)

Initialization of the SDK is required before calling any other method

### Device registration

Check if registered on startup, and if not:
1. [Send the registration token](#update-registration-token) (obtained from GCM/FCM)
2. [Register the user's phone number](#register-phone-number)
3. [Verify the verification code](#verify-code)

Regularly check if the registration is still valid and prompt to re-register if not.

### Register environment

1. Push message of [type QR](#get-push-type) arrives
2. Show QR code scanner
3. Wait for the user to scan the QR code
4. Retrieve the info from the scanned QR code
5. [Register the environment](#register-environment-1)

### Authentication requests

1. Obtain authentication request
  - [From push message](#get-authentication-request-1) (basic)
  - [Retrieve manually](#get-authentication-request) (full)
2. Check authentication request type
3. *Optional*: get full authentication request (in case of push)
4. Show authentication request information
5. Instant: show approve/deny buttons
   OTP: show one-time password
6. Wait for the user to respond or till it's expired
   - [Update the status to approved or denied](#update-status)
7. Hide authentication request




## Initialization
### Initialize the SDK

Initialization of the SDK is required before calling any other method in `Authenticator `. The initialization process should happen as early as possible, initializing in the `Application` class ensures the SDK is always initialized. It's recommended to pass the `Application Context` as an argument.

Obtain the app key from the [App manager](https://appmanager.cmtelecom.com)

1. Default configuration

   ```java
   Authenticator.init(Context context, String appKey)
   ```

2. Custom configuration (change app name, app version or API url's)

   ```java
   Authenticator.init(Config config, String appKey)
   ```

#### Example

```java
public class MyApplication extends Application {
    private static final String APP_KEY = "YOUR_APP_KEY_HERE";
  
    @Override
    public void onCreate() {
        super.onCreate();
      
        // default configuration
        Authenticator.init(this, APP_KEY);
      
        // OR custom configuration
        Authenticator.Config config = new Authenticator.Config(this, APP_KEY);
        Authenticator.init(config);
    }
}
```

Declare your `Application` class in the `<application />` tag in the Android Manifest:

```xml
<application [icon, label, theme, etc.] android:name=".MyApplication" />
```



## Devices

Obtain the client to manage devices using:

```java
DeviceClient deviceClient = Authenticator.deviceClient();
```

### Register phone number

Register a user by his phone number, a verification code will be sent via SMS.

```java
DeviceClient.registerPhoneNumber(String phoneNumber, Callback<DeviceRegistration> callback)
```

> The phone number must be specified in international format (E.164)

#### Example

```java
deviceClient.registerPhoneNumber(phoneNumber, new Callback<DeviceRegistration>() {
    @Override
    public void onSuccess(DeviceRegistration deviceRegistration) {
        // phone number registered
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```

### Verify code

Verify the phone number of the user using the code received via SMS.

```java
DeviceClient.verifyCode(String verificationCode, Callback<DeviceRegistration> callback)
```

#### Example

```java
deviceClient.verifyCode(verificationCode, new Callback<DeviceRegistration>() {
    @Override
    public void onSuccess(DeviceRegistration deviceRegistration) {
        switch (deviceRegistration.getStatus()) {
            case VERIFIED:
                // handle phone number verified
                break;
            default:
                // handle not verified
                break;
        }
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```

### Update registration token

Update the push registration token, do this after the code was retrieved or updated (obtain it from GCM/FCM). Set whether to enable push messages or fall back to SMS.

```java
DeviceClient.updateRegistrationToken(String registrationToken, boolean pushEnabled, Callback<DeviceRegistration> callback)
```

#### Example

```java
deviceClient.updateRegistrationToken(registrationToken, pushEnabled, new Callback<DeviceRegistration>() {
    @Override
    public void onSuccess(DeviceRegistration deviceRegistration) {
        // updated registration token
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```

### Update device info

Update information about the app and the device. It's not needed to do this manually, the SDK will automatically call this regularly.

```java
DeviceClient.updateRegistration(Callback<DeviceRegistration> callback)
```

#### Example

```java
deviceClient.updateRegistration(new Callback<DeviceRegistration>() {
    @Override
    public void onSuccess(DeviceRegistration deviceRegistration) {
        // updated device info
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```



## Environments

Obtain the client to manage environments using:

```java
EnvironmentClient environmentClient = Authenticator.environmentClient();
```

### Get environments

Get a list of all registered environments, for example to get changes (updated name or icon).

```java
EnvironmentClient.getEnvironments(Callback<List<Environment>> callback)
```

#### Example

```java
environmentClient.getEnvironments(new Callback<List<Environment>>() {
    @Override
    public void onSuccess(List<Environment> environmentList) {
        // handle environments
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```

### Register environment

Create the link between this device and an environment using the identifier and secret obtained from the QR code using the `QrCodeHelper`.

```java
EnvironmentClient.register(String envId, String envSecret, Callback<Void> callback)
```

> The `Exception `  passed to the `Callback` could is of type `EnvironmentInvalidException` if the provided environment information was invalid (id, secret or both)
>

#### Example

```java
QrCodeHelper qrCodeHelper = new QrCodeHelper(qrCodeContent);
if (!qrCodeHelper.isValidQrCode()) {
    return; // QR code is invalid
}

String environmentId = qrCodeHelper.getId();
String environmentSecret = qrCodeHelper.getSecret();
environmentClient.register(environmentId, environmentSecret, new Callback<Void>() {
    @Override
    public void onSuccess(Void aVoid) {
        // persist the environment id and secret
    }

    @Override
    public void onFailure(Exception e) {
        if (e instanceof EnvironmentInvalidException) {
            // handle exception
        } else {
            // handle exception
        }
    }
});
```

### Unregister environment

Delete the link between this device and the environment.

```java
EnvironmentClient.unregister(Environment environment, Callback<Void> callback)
// or
EnvironmentClient.unregister(String id, String secret, Callback<Void> callback)
```

#### Example

```java
environmentClient.unregister(environment, new Callback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            // delete persisted environment
        }

        @Override
        public void onFailure(Exception e) {
            // handle exception
        }
    });
}
```

### Get authentication request

Get the current open authentication request for an environment.

```java
getAuthenticationRequest(Environment environment, Callback<AuthenticationRequest> callback)
// or
getAuthenticationRequest(String environmentId, String environmentSecret, Callback<AuthenticationRequest> callback)
```

> The `AuthenticationRequest`  passed to the `Callback` is null when there is no open authentication request.

#### Example

```java
environmentClient.getAuthenticationRequest(environment, new Callback<AuthenticationRequest>() {
    @Override
    public void onSuccess(AuthenticationRequest authReq) {
        if (authReq == null) {
            // no open authentication request
            return;
        }
        // handle authentication request
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```



## Authentication requests

Obtain the client to manage authentication requests using:

```java
AuthClient authClient = Authenticator.authClient();
```

### Update status

Set the status of an open authentication request of type *instant* to either `Status.APPROVED` or `Status.DENIED`.

```java
AuthClient.updateStatus(BaseAuthenticationRequest authReq, Environment env, Status status, Callback<Status> callback)
// or
AuthClient.updateStatus(String authReqId, String environmentSecret, Status status, Callback<Status> callback)
```

> The `Status `  passed to the `Callback` is the status passed to `updateStatus(...)` or `Status.EXPIRED` in case the request was expired and the status could not be updated.

#### Example

```java
authClient.updateStatus(authReq, environment, status, new Callback<Status>() {
    @Override
    public void onSuccess(Status status) {
        switch (status) {
            case APPROVED:
                // handle approved
                break;
            case DENIED:
                // handle denied
                break;
            case EXPIRED:
                // handle expired
                break;
        }
    }

    @Override
    public void onFailure(Exception e) {
        // handle exception
    }
});
```



## Push & notifications

A helper is included to help extract authentication request information from push messages.

```java
new PushHelper(Bundle extras)
```

### Get push type

Get the type of the content in the push message.

```java
PushHelper.getType() : PushType
```

| Name               | Description                              |
| ------------------ | ---------------------------------------- |
| `PushType.INSTANT` | New instant authentication request       |
| `PushType.OTP`     | New one-time password                    |
| `PushType.QR`      | Request to scan a QR code                |
| `PushType.PLAIN`   | A regular (non-Authenticator) push message |

### Get authentication request

Get the basic authentication request, in case of type `INSTANT` or `OTP`. Get the full authentication request (with additional info) using the `EnvironmentClient`.

```java
PushHelper.getAuthenticationRequest() : BaseAuthenticationRequest
```

### Get message

Get the message body of the push message.

```java
PushHelper.getMessage() : String
```

### Example

```java
public class PushMessageListener implements HybridNotificationListener {
    
    @Override
    public void onReceiveHybridNotification(Context context, Notification notification) {
        PushHelper pushHelper = new PushHelper(notification.getExtras());
        String message = pushHelper.getMessage();

        switch (pushHelper.getType()) {
            case INSTANT:
            case OTP:
              BaseAuthenticationRequest authReq = pushHelper.getAuthenticationRequest();
              // handle authentication request
              break;
            case QR:
              // handle scan QR code
              break;
            default:
              // handle plain text message
              break;
        }
    }
}
```
