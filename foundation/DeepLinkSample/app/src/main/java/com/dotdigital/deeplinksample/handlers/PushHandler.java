package com.dotdigital.deeplinksample.handlers;

import android.util.Log;

import com.comapi.ComapiClient;
import com.comapi.PushDetails;
import com.comapi.internal.push.PushMessageListener;
import com.dotdigital.deeplinksample.constants.AppConstants;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

public class PushHandler implements PushMessageListener {

    @Override
    public void onMessageReceived(RemoteMessage message) {

        try {
            PushDetails result = ComapiClient.parsePushMessage(message);
            Log.i(AppConstants.APP_TAG, "Received push message with data = " + (result.getData() != null ? result.getData().toString() : "null") + " and deep link = " + result.getUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}