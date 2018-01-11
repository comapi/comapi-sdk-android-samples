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
import com.comapi.chat.ChatConfig;
import com.comapi.chat.ComapiChat;
import com.comapi.chat.ComapiChatClient;
import com.comapi.chat.StoreCallback;
import com.comapi.chat.StoreFactory;
import com.comapi.chat.model.ChatStore;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogLevel;
import com.comapi.sample.comapi.AuthChallengeHandler;
import com.comapi.sample.comapi.MainController;
import com.comapi.sample.events.InitialisationEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Application class, Comapi SDK initialisation should be called in {@link SampleApplication#onCreate} method.
 * This class also implements a callback interface and receives a Comapi Chat SDK client object.
 *
 * @author Marcin Swierczek
 */
public class SampleApplication extends Application implements Callback<ComapiChatClient> {

    private MainController mainMainController;

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialise Firebase if you want to enable comapi push functionality. You will also need to include fcm dependencies and configuration json.
        //FirebaseApp.initializeApp(this);

        // Initialise Comapi
        initComapi();
    }

    private void initComapi() {

        //Create class to encapsulate all calls to Comapi APIs. Is also an registration point for UI listeners.
        mainMainController = new MainController();

        // PUT YOUR API KEY HERE
        final String apiSpaceId = "";

        if (TextUtils.isEmpty(apiSpaceId)) {
            Log.e(Const.TAG, "Put your Api Space Id in SampleApplication#initComapi()");
            return;
        }

        // Asynchronously initialise Comapi SDK client (retrieve it in callback)
        ComapiChat.initialise(
                this, new ChatConfig()
                        // Set the id of the app space, the device belongs to
                        .apiSpaceId(apiSpaceId)
                        // Sets handler for authentication challenges (SDK asking for JWT token)
                        .authenticator(new AuthChallengeHandler(getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE)))
                        // Display all available logs to the console, don't log to the file
                        .logConfig(new LogConfig().setFileLevel(LogLevel.OFF).setConsoleLevel(LogLevel.DEBUG).setNetworkLevel(LogLevel.DEBUG))
                        // Sets persistence store factory. ChatStore instance will queue db changes and perform them when {@link ChatStore#endTransaction} has been called.
                        .store(new StoreFactory<ChatStore>() {
                            @Override
                            protected void build(StoreCallback<ChatStore> callback) {
                                callback.created(mainMainController.getNewStoreTransaction());
                            }
                        })
                        .fcmEnabled(false) // Disable comapi push functionality
                // Sets callback to retrieve Comapi client instance
                , this);
    }

    @Override
    public void success(ComapiChatClient client) {
        Log.i(Const.TAG, "Comapi Chat initialised successfully.");
        // We will call Comapi client methods only through MainController
        mainMainController.setClient(client);
        // Post global sticky event and pass Comapi Controller instance to Activities after SDK finishes initialising.
        EventBus.getDefault().postSticky(new InitialisationEvent(mainMainController));
    }

    @Override
    public void error(Throwable t) {
        Log.e(Const.TAG, "Comapi Chat initialised with error.", t);
        // Post global sticky event to tell Activities that the Comapi client is unavailable due to initialisation error.
        EventBus.getDefault().postSticky(new InitialisationEvent(null));
    }
}
