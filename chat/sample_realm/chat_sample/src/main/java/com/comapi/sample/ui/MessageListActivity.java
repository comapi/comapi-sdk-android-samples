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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.comapi.sample.R;
import com.comapi.sample.comapi.ComapiController;
import com.comapi.sample.comapi.MainController;
import com.comapi.sample.events.InitialisationEvent;
import com.comapi.sample.store.model.DBChatMessage;
import com.comapi.sample.ui.adapters.MessageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;

/**
 * Activity displaying messages for a conversation. Allows to send new message to a conversation.
 *
 * @author Marcin Swierczek
 */
public class MessageListActivity extends AppCompatActivity implements ComapiController.UICallback {

    /**
     * Conversation unique identifier for which this Activity should display messages.
     */
    private String conversationId;

    /**
     * Conversation conversationName.
     */
    private String conversationName;

    /**
     * Adapter for the messages list view.
     */
    private MessageAdapter adapter;

    /**
     * Wraps all com.comapi.ComapiClient methods used by the sample app.
     */
    private MainController mainController;

    /**
     * Edit text to enter new message body.
     */
    private EditText messageEditText;

    SwipeRefreshLayout swipeToRefresh;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Retrieve details of conversation to display messages for.
        conversationName = getIntent().getStringExtra(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_NAME);
        conversationId = getIntent().getStringExtra(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_ID);

        // Close chat screen if conversation id unavailable
        if (TextUtils.isEmpty(conversationId)) {
            finish();
        }

        setupViews();
        bindData();
    }

    /**
     * Clears new message edit text and sends text message for active conversation.
     */
    public void sendMessage() {
        if (mainController != null) {
            final String message = messageEditText.getText().toString();
            if (!TextUtils.isEmpty(message.trim())) {
                messageEditText.setText("");
                mainController.getComapiController().getService().sendMessage(conversationId, message);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to menu option clicks
        switch (item.getItemId()) {
            // Go back to previous screen
            case android.R.id.home:
                navigateUpTo(new Intent(this, ConversationListActivity.class));
                return true;
            // Open Activity to display/add/remove participants of active conversation
            case R.id.manage_participants:
                manageParticipants();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens Activity to display/add/remove participants of active conversation
     */
    private void manageParticipants() {
        Intent intent = new Intent(this, ManageParticipantsActivity.class);
        Bundle bundle = new Bundle();
        //Pass details of Conversation to Activity controlling participants management.
        bundle.putString(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        bundle.putString(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_NAME, conversationName);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register this Activity to global EventBus to get SDK Initialisation sticky event with Comapi controller - see MessageListActivity#onEvent(Initialisation)
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister this Activity from global EventBus
        EventBus.getDefault().unregister(this);
        if (mainController != null) {
            mainController.getComapiController().removePagingCallback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    /**
     * Find and configure views.
     */
    private void setupViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_messages);
        setSupportActionBar(toolbar);
        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_messages);
        swipeToRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (mainController != null && conversationId != null) {
                            mainController.getComapiController().getService().getNextPage(conversationId);
                        }
                    }
                }
        );

        messageEditText = (EditText) findViewById(R.id.send_message_edit_text);

        // Button to send
        Button sendButton = (Button) findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set conversation conversationName as Toolbar title
        setTitle(conversationName);
    }

    /**
     * Create adapter for messages in active conversation.
     */
    private void bindData() {
        realm = Realm.getDefaultInstance();
        // RealmResults are "live" views, that are automatically kept up to date, even when changes happen
        // on a background thread. The RealmBaseAdapter will automatically keep track of changes and will
        // automatically refresh when a change is detected.
        adapter = new MessageAdapter(realm.where(DBChatMessage.class).equalTo(DBChatMessage.CONVERSATION_ID, conversationId).findAllSorted(DBChatMessage.SENT_ID));
        // Bind adapter with list view
        ListView listview = (ListView) findViewById(R.id.list_view_messages);
        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Display option menu with 'manage conversation participants' button.
        getMenuInflater().inflate(R.menu.participants_menu, menu);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(InitialisationEvent event) {
        // Get Controller for Comapi calls
        mainController = event.getController();
        // Listen for messaging events
        mainController.getComapiController().setPagingCallback(this);
    }

    @Override
    public void finished(boolean isSuccess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (swipeToRefresh.isRefreshing()) {
                    swipeToRefresh.setRefreshing(false);
                }
            }
        });
    }
}
