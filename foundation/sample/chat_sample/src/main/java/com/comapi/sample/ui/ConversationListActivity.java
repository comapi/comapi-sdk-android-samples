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

package com.comapi.sample.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.comapi.sample.Const;
import com.comapi.sample.R;
import com.comapi.sample.comapi.ComapiController;
import com.comapi.sample.events.Initialisation;
import com.comapi.sample.events.LoginEvent;
import com.comapi.sample.ui.holders.ConversationViewHolder;
import com.comapi.sample.ui.listeners.ConversationListener;
import com.comapi.sample.ui.model.ConversationItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity displaying all conversations in which registered user participates. Allows to send new message to a conversation.
 *
 * @author Marcin Swierczek
 */
public class ConversationListActivity extends AppCompatActivity implements CreateConversationDialog.CreateConversation, RegisterDialog.RegisterInterface, ConversationListener, DialogInterface.OnDismissListener {

    private static final String TAG_CREATE_CONVERSATION_DIALOG = "conDialog";
    public static final String KEY_INTENT_BUNDLE_CONVERSATION_ID = "cId";
    public static final String KEY_INTENT_BUNDLE_CONVERSATION_NAME = "cN";
    private static final String TAG_REGISTER_DIALOG = "regDialog";

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private ComapiController controller;

    /**
     * Conversations, no duplicates.
     */
    private Map<String, ConversationItem> dataMap;

    /**
     * Conversations, no duplicates.
     */
    private ArrayList<ConversationItem> data;

    /**
     * Adapter for conversations list view.
     */
    private ArrayAdapter<ConversationItem> adapter;

    /**
     * Overlay with progress bar displayed when loading data.
     */
    private ViewGroup overlay;

    /**
     * Shared preferences to store profile id for which the app should authenticate Comapi session.
     */
    SharedPreferences settings;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        setupViews();
        initAdapter();

        settings = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (controller != null) {
            controller.removeConversationListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    /**
     * Find and configure views.
     */
    private void setupViews() {

        // Setup Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_conversations);
        setSupportActionBar(toolbar);

        // Setup floating action button for creating new conversations
        FloatingActionButton crateConversationFab = (FloatingActionButton) findViewById(R.id.create_conversation_fab);
        crateConversationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCreateConversationDialog();
            }
        });

        // Find progress bar overlay
        overlay = (ViewGroup) findViewById(R.id.overlay);
    }

    /**
     * Display dialog to get name for conversation to create.
     */
    private void displayCreateConversationDialog() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment previous = getSupportFragmentManager().findFragmentByTag(TAG_CREATE_CONVERSATION_DIALOG);
        if (previous != null) {
            ft.remove(previous);
        }
        ft.addToBackStack(null);

        CreateConversationDialog newFragment = new CreateConversationDialog();
        newFragment.show(ft, TAG_CREATE_CONVERSATION_DIALOG);
    }

    /**
     * Display dialog to get profile id for the app to authenticate with Comapi services.
     */
    private void displayRegisterDialog() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment previous = getSupportFragmentManager().findFragmentByTag(TAG_REGISTER_DIALOG);
        if (previous != null) {
            ft.remove(previous);
        }
        ft.addToBackStack(null);

        RegisterDialog newFragment = new RegisterDialog();
        newFragment.show(ft, TAG_REGISTER_DIALOG);
    }

    /**
     * Initialise data adapter for conversations list view.
     */
    private void initAdapter() {

        data = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new ArrayAdapter<ConversationItem>(this, 0, data) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                // References to this row views
                ConversationViewHolder viewHolder;

                // Inflate layout only once
                if (convertView == null) {
                    convertView = getLayoutInflater()
                            .inflate(R.layout.conversation_list_row, null, false);
                    // Store references to views in this list row
                    viewHolder = new ConversationViewHolder();
                    viewHolder.name =
                            (TextView) convertView.findViewById(R.id.name);
                    convertView.setTag(viewHolder);

                } else {
                    viewHolder = (ConversationViewHolder) convertView.getTag();
                }

                ConversationItem item = getItem(position);
                if (item != null) {
                    // Get references to views in this list row
                    viewHolder.name.setText(item.getName());
                }

                return convertView;
            }
        };

        // Bind adapter with list view
        ListView listview = (ListView) findViewById(R.id.list_view_conversations);
        listview.setAdapter(adapter);

        // Set action when list element will be clicked. Open chat for a given conversation.
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ConversationItem item = data.get(position);
                openChat(item.getId(), item.getName());
            }
        });
    }

    /**
     * Open Activity with list of messages in a given conversation.
     */
    private void openChat(String conversationId, String conversationName) {
        if (!TextUtils.isEmpty(conversationId)) {
            Intent intent = new Intent(this, MessageListActivity.class);
            Bundle bundle = new Bundle();
            // Pass information about the chosen conversation
            bundle.putString(KEY_INTENT_BUNDLE_CONVERSATION_ID, conversationId);
            bundle.putString(KEY_INTENT_BUNDLE_CONVERSATION_NAME, conversationName);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(Initialisation event) {

        /*
         SDK finished initialising.
         */

        // Get Controller for Comapi calls
        controller = event.getController();
        // Get profile id for which Comapi SDK was authenticating session.
        String profileId = settings.getString(Const.PREFS_KEY_PROFILE_ID, null);
        if (TextUtils.isEmpty(profileId)) {
            // If user wasn't logged in display popup to ask for profile id to login as.
            displayRegisterDialog();
        } else {
            // If user with profile id was authenticated display it as a subtitle in the Toolbar.
            toolbar.setSubtitle(getString(R.string.logged_in_as)+" \'" + profileId + "\'");
            // Show loading data progress bar
            overlay.setVisibility(View.VISIBLE);
            // Listen for messaging events
            controller.setConversationListener(this);
            // Trigger conversation query. Query conversations in which logged in user participates.
            controller.getConversations();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(LoginEvent event) {

        /*
         User logged in event.
         */

        if (event.isSuccess()) {
            // Show loading data progress bar
            overlay.setVisibility(View.VISIBLE);
            // Listen for messaging events
            controller.setConversationListener(this);
            // Trigger conversation query. Query conversations in which logged in user participates.
            controller.getConversations();
            // If user with profile id was authenticated display it as a subtitle in the Toolbar.
            toolbar.setSubtitle(getString(R.string.logged_in_as)+" \'" + controller.getProfileId() + "\'");
        }
    }

    @Override
    public void createConversation(String name) {
        // Dialog triggered creating new conversation.
        if (controller != null && !TextUtils.isEmpty(name)) {
            controller.createConversation(name);
        }
    }

    @Override
    public void onAddConversation(String conversationId, String conversationName) {

        /*
         Add new conversation in which logged in user participates.
         */
        dataMap.put(conversationId, new ConversationItem(conversationId, conversationName));
        data.clear();
        data.addAll(dataMap.values());
        adapter.notifyDataSetChanged();
        // Hide progress bar
        overlay.setVisibility(View.GONE);
    }

    @Override
    public void onEmptyConversationList() {
        dataMap.clear();
        data.clear();
        adapter.notifyDataSetChanged();
        // Hide progress bar
        overlay.setVisibility(View.GONE);
    }

    @Override
    public void register(String profileId) {

        /*
         No conversation in which logged in user participates.
         */

        // Save profileId for AuthChallengeHandler to use when creating JWT token.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Const.PREFS_KEY_PROFILE_ID, profileId);
        editor.apply();

        // Show progress bar
        overlay.setVisibility(View.VISIBLE);

        // Start authenticated session. com.comapi.sample.comapi.AuthChallengeHandler will create JWT token with subject = profileId saved in SharedPreferences
        controller.startSession();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // if dialog to log in user was dismissed then close the app
        if (dialog instanceof RegisterDialog && !settings.contains("profileId")) {
            finish();
        }
    }
}
