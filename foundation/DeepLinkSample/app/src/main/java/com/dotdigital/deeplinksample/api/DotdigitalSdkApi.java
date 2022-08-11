package com.dotdigital.deeplinksample.api;

import com.comapi.Callback;
import com.comapi.Comapi;
import com.comapi.Session;
import com.comapi.internal.network.ComapiResult;

import java.util.HashMap;
import java.util.Map;

public class DotdigitalSdkApi {

    static public void endSession(SimpleCallback callback) {
        Comapi.getShared().service().session().endSession(new Callback<ComapiResult<Void>>() {
            @Override
            public void success(ComapiResult<Void> result) {
                if (result != null && result.isSuccessful()) {
                    callback.success();
                } else {
                    callback.error("Error when logging out.");
                }
            }

            @Override
            public void error(Throwable t) {
                callback.error("Error when logging out.");
            }
        });
    }

    static public void startSession(SimpleCallback callback) {
        Comapi.getShared().service().session().startSession(new Callback<Session>() {
            @Override
            public void success(Session session) {
                if (session != null && session.isSuccessfullyCreated()) {
                    updateProfileWithEmail(session.getProfileId()+"@email.com", callback);
                } else {
                    callback.error("Error when logging in.");
                }
            }

            @Override
            public void error(Throwable t) {
                callback.error("Error when logging in.");
            }
        });
    }

    static private void updateProfileWithEmail(String email, SimpleCallback callback) {
        Map<String, Object> update = new HashMap<>();
        update.put("email", email);
        Comapi.getShared().service().profile().updateProfile(update, null, new Callback<ComapiResult<Map<String, Object>>>() {
            @Override
            public void success(ComapiResult<Map<String, Object>> session) {
                callback.success();
            }

            @Override
            public void error(Throwable t) {
                callback.error("Error when updating email");
            }
        });
    }
}
