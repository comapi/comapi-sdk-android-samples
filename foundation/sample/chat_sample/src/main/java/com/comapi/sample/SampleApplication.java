/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.sample;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.comapi.Callback;
import com.comapi.Comapi;
import com.comapi.ComapiClient;
import com.comapi.ComapiConfig;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogLevel;
import com.comapi.sample.comapi.AuthChallengeHandler;
import com.comapi.sample.comapi.ComapiController;
import com.comapi.sample.comapi.EventsHandler;
import com.comapi.sample.comapi.PushHandler;
import com.comapi.sample.events.Initialisation;
import com.google.firebase.FirebaseApp;

import org.greenrobot.eventbus.EventBus;

/**
 * Application class, Comapi SDK initialisation should be called in {@link SampleApplication#onCreate} method
 *
 * @author Marcin Swierczek
 */
public class SampleApplication extends Application implements Callback<ComapiClient> {

    private ComapiController comapiController;

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialise Firebase
        FirebaseApp.initializeApp(this);

        // Initialise Comapi
        initComapi();
    }

    private void initComapi() {

        //Create class to encapsulate all calls to Comapi APIs
        comapiController = new ComapiController();

        // PUT YOUR API KEY HERE
        final String apiSpaceId = "";

        if (TextUtils.isEmpty(apiSpaceId)) {
            Log.e(Const.TAG, "Put your Api Space Id in SampleApplication#initComapi()");
            return;
        }

        // Asynchronously initialise Comapi SDK client (retrieve it in callback)
        Comapi.initialise(
                this, new ComapiConfig()
                        // Set the id of the app space, the device belongs to
                        .apiSpaceId(apiSpaceId)
                        // Sets handler for authentication challenges (SDK asking for JWT token)
                        .authenticator(new AuthChallengeHandler(getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE)))
                        // Display all available logs to the console, don't log to the file
                        .logConfig(new LogConfig().setFileLevel(LogLevel.OFF).setConsoleLevel(LogLevel.DEBUG).setNetworkLevel(LogLevel.DEBUG))
                        // If FCM has been set up you can intercept the push messages here. You will need to put google-services.json file obtained on https://console.firebase.google.com/ into this project for FCM to work.
                        .pushMessageListener(new PushHandler())
                        // Sets receiver of messaging live events
                        .messagingListener(new EventsHandler(comapiController))
                // Sets callback to retrieve Comapi client instance
                , this);
    }

    @Override
    public void success(ComapiClient client) {
        Log.i(Const.TAG, "Comapi Foundation initialised successfully.");
        // We will call Comapi client method only through our Comapi Controller
        comapiController.setClient(client);
        // Post global sticky event to pass Comapi Controller instance to Activities after SDK finishes initialising.
        EventBus.getDefault().postSticky(new Initialisation(comapiController));
    }

    @Override
    public void error(Throwable t) {
        Log.e(Const.TAG, "Comapi Foundation initialised with error.", t);
        // Post global sticky event to tell Activities that the Comapi client is unavailable due to initialisation error.
        EventBus.getDefault().postSticky(new Initialisation(null));
    }
}
