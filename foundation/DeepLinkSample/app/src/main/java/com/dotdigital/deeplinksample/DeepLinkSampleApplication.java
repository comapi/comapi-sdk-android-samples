package com.dotdigital.deeplinksample;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comapi.Callback;
import com.comapi.Comapi;
import com.comapi.ComapiClient;
import com.comapi.ComapiConfig;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogLevel;
import com.dotdigital.deeplinksample.constants.*;
import com.dotdigital.deeplinksample.handlers.AuthChallengeHandler;
import com.dotdigital.deeplinksample.handlers.PushHandler;
import com.google.firebase.FirebaseApp;

public class DeepLinkSampleApplication extends Application implements Callback<ComapiClient> {

    @Override
    public void onCreate() {
        super.onCreate();

        /*
            Remember to generate google-services.json file on https://console.firebase.google.com and put it in to the app folder for Firebase push messaging to work
        */

        //Initialise Firebase (you will need this if using foundation push functionality)
        FirebaseApp.initializeApp(this);

        // Initialise Foundation SDK
        initDotdigitalFoundationSDK();
    }

    private void initDotdigitalFoundationSDK() {

        if (TextUtils.isEmpty(DotdigitalConstants.API_SPACE_ID)) {
            Log.e(AppConstants.APP_TAG, "Put your Api Space Id in DotdigitalConstants class");
            return;
        }

        if (TextUtils.isEmpty(DotdigitalConstants.PROFILE_ID)) {
            Log.e(AppConstants.APP_TAG, "Put your profile id in DotdigitalConstants class");
            return;
        }

        if (TextUtils.isEmpty(DotdigitalConstants.AUDIENCE) || TextUtils.isEmpty(DotdigitalConstants.ISSUER) || TextUtils.isEmpty(DotdigitalConstants.SECRET)) {
            Log.e(AppConstants.APP_TAG, "Put your authentication details in DotdigitalConstants class");
            return;
        }

        // Asynchronously initialise Foundation SDK client (retrieve it in callback)
        Comapi.initialiseShared(
                this, new ComapiConfig()
                        // Set the id of the app space, the device belongs to
                        .apiSpaceId(DotdigitalConstants.API_SPACE_ID)
                        // Sets handler for authentication challenges (SDK asking for JWT token)
                        .authenticator(new AuthChallengeHandler())
                        // Display all available logs to the console, don't log to the file
                        .logConfig(new LogConfig().setFileLevel(LogLevel.OFF).setConsoleLevel(LogLevel.DEBUG).setNetworkLevel(LogLevel.DEBUG))
                        // If FCM has been set up you can intercept the push messages here. You will need to put google-services.json file obtained on https://console.firebase.google.com/ into this project for FCM to work.
                        .pushMessageListener(new PushHandler())
                // Sets callback to retrieve Foundation client instance
                , this);
    }

    private void notifyActivitySdkInitialised() {
        Intent intent = new Intent(AppConstants.INITIALISED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void success(ComapiClient result) {
        Log.i(AppConstants.APP_TAG, "Dotdigital Foundation initialised successfully.");
        notifyActivitySdkInitialised();
    }

    @Override
    public void error(Throwable t) {
        Log.e(AppConstants.APP_TAG, "Error initialising Dotdigital Foundation.");
        notifyActivitySdkInitialised();
    }
}
