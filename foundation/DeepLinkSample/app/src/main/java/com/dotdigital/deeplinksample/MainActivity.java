package com.dotdigital.deeplinksample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comapi.Callback;
import com.comapi.Comapi;
import com.comapi.ComapiClient;
import com.comapi.PushHandleResult;
import com.comapi.Session;
import com.dotdigital.deeplinksample.api.DotdigitalSdkApi;
import com.dotdigital.deeplinksample.api.SimpleCallback;
import com.dotdigital.deeplinksample.constants.AppConstants;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private BroadcastReceiver mMessageReceiver;
    private TextView tv;
    private ToggleButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        aksPermission();

        ComapiClient client;
        try {
            client = Comapi.getShared();
            handleSdkInitialised();
            handlePushNotification(client, getIntent());
        } catch (Exception e) {
            createBroadcastReceiver();
        }
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        try {
            handlePushNotification(Comapi.getShared(), i);
        } catch (Exception e) {
            tv.setText("Error handling notification");
        }
    }

    @Override
    protected void onDestroy() {
        removeBroadcastReceiver();
        super.onDestroy();
    }

    protected void setViews() {
        tv = findViewById(R.id.textView);
        btn = findViewById(R.id.button);
        btn.setOnCheckedChangeListener(this);
    }

    @SuppressLint("ObsoleteSdkInt")
    void aksPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
    }

    private void createBroadcastReceiver() {
        this.mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), AppConstants.INITIALISED)) {
                    ComapiClient client = Comapi.getShared();
                    if (client != null) {
                        handleSdkInitialised();
                        handlePushNotification(client, getIntent());
                    } else {
                        tv.setText("Error initialising sdk");
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(AppConstants.INITIALISED));
    }

    protected void removeBroadcastReceiver() {
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    private void startSession() {
        tv.setText("Session is starting ...");
        btn.setEnabled(false);
        DotdigitalSdkApi.startSession(new SimpleCallback() {
            @Override
            public void success() {
                tv.setText("Session started for profile "+Comapi.getShared().getSession().getProfileId());
                btn.setEnabled(true);
            }

            @Override
            public void error(String msg) {
                tv.setText("Error starting session");
                btn.setEnabled(true);
            }
        });
    }

    private void endSession() {
        tv.setText("Session is ending ...");
        btn.setEnabled(false);
        DotdigitalSdkApi.endSession(new SimpleCallback() {
            @Override
            public void success() {
                tv.setText("Session has ended");
                btn.setEnabled(true);
            }

            @Override
            public void error(String msg) {
                tv.setText("Error ending session");
                btn.setEnabled(true);
            }
        });
    }

    private void handleSdkInitialised() {
        btn.setEnabled(true);
        if (isSessionCreated()) {
            btn.setChecked(true);
            tv.setText("Session started for profile "+Comapi.getShared().getSession().getProfileId());
        } else {
            btn.setChecked(false);
            tv.setText("SDK initialised. Session not started.");
        }
    }

    private boolean isSessionCreated() {
        Session session = Comapi.getShared().getSession();
        return session != null && session.isSuccessfullyCreated();
    }

    private void handlePushNotification(@NonNull ComapiClient client, Intent intent) {
        client.handlePushNotification(this, intent, true, new Callback<PushHandleResult>() {
            @Override
            public void success(PushHandleResult result) {
                Log.i(AppConstants.APP_TAG, "handlePush, push data is " + (result.getData() != null ? result.getData().toString() : "null"));
                Log.i(AppConstants.APP_TAG, "handlePush, push url is  " + result.getUrl());
                Log.i(AppConstants.APP_TAG, "handlePush, click was recorded? " + result.isClickRecorded());
                Log.i(AppConstants.APP_TAG, "handlePush, deep link was opened? " + result.isDeepLinkCalled());
            }

            @Override
            public void error(Throwable t) {
                Log.e(AppConstants.APP_TAG, "handlePush, " + t.getMessage());
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!buttonView.isPressed()) {
            return;
        }
        if (isChecked && !isSessionCreated()) {
            startSession();
        } else if (isSessionCreated()) {
            endSession();
        }
    }
}