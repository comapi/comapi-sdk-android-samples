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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.comapi.sample.R;
import com.comapi.sample.comapi.ComapiController;
import com.comapi.sample.events.Initialisation;
import com.comapi.sample.ui.holders.MessageViewHolder;
import com.comapi.sample.ui.listeners.MessageListener;
import com.comapi.sample.ui.model.MessageItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Activity displaying messages for a conversation. Allows to send new message to a conversation.
 *
 * @author Marcin Swierczek
 */
public class MessageListActivity extends AppCompatActivity implements MessageListener {

    /**
     * Conversation unique identifier for which this Activity should display messages.
     */
    private String conversationId;

    /**
     * Conversation conversationName.
     */
    private String conversationName;

    /**
     * Messages sorted by conversation event id (monotonically increasing with the order of message sent in conversation).
     */
    private TreeMap<Long, MessageItem> data;

    /**
     * Messages passed to adapter obtained from ordered values of {@link MessageListActivity#data}.
     */
    private ArrayList<MessageItem> dataArray;

    /**
     * Adapter for the messages list view.
     */
    private ArrayAdapter<MessageItem> adapter;

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private ComapiController controller;

    /**
     * Edit text to enter new message body.
     */
    private EditText messageEditText;

    /**
     * Overlay with progress bar displayed when loading data.
     */
    private ViewGroup overlay;

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
        setTitle(conversationName);
    }

    /**
     * Clears new message edit text and sends text message for active conversation.
     */
    public void sendMessage() {
        if (controller != null) {
            final String message = messageEditText.getText().toString();
            if (!TextUtils.isEmpty(message.trim())) {
                messageEditText.setText("");
                controller.sendMessage(conversationId, message);
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
        if (controller != null) {
            // Remove socket message listener for active Conversation
            controller.unregisterMessageListener(conversationId);
        }
    }

    /**
     * Find and configure views.
     */
    private void setupViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_messages);
        setSupportActionBar(toolbar);

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

        // Find progress bar overlay
        overlay = (ViewGroup) findViewById(R.id.overlay);
    }

    /**
     * Create adapter for messages in active conversation.
     */
    private void bindData() {

        data = new TreeMap<>();
        dataArray = new ArrayList<>();

        adapter = new ArrayAdapter<MessageItem>(this, 0, dataArray) {

            @Override
            public int getViewTypeCount() {

                /*
                 There are two types of rows in message list view - received and sent messages
                 */

                return 2;
            }

            @Override
            public int getItemViewType(int position) {

                /*
                 Return one of two list view item types - received or sent messages.
                 */

                MessageItem item = getItem(position);
                return (item != null && item.getSentByMe()) ? 0 : 1;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                // Message data to populate row view in the list
                MessageItem item = getItem(position);
                // References to this row views
                MessageViewHolder viewHolder;

                if (item != null) {

                    // Inflate layout only once
                    if (convertView == null) {

                        LayoutInflater inflater = LayoutInflater.from(getContext());

                        // Different list row layout for sent and received messages
                        if (item.getSentByMe()) {
                            convertView = inflater.inflate(R.layout.bubble_right, parent, false);
                        } else {
                            convertView = inflater.inflate(R.layout.bubble_left, parent, false);
                        }

                        // Store references to views in this list row
                        viewHolder = new MessageViewHolder();
                        viewHolder.body = (TextView) convertView.findViewById(R.id.body);
                        viewHolder.sender = (TextView) convertView.findViewById(R.id.sender);
                        convertView.setTag(viewHolder);

                    } else {
                        // Get references to views in this list row
                        viewHolder = (MessageViewHolder) convertView.getTag();
                    }

                    // Sets message body into chat bubble
                    viewHolder.body.setText(item.getBody());

                    // Sets sender pro
                    if (viewHolder.sender != null) {
                        viewHolder.sender.setText(item.getSender());
                    }

                } else {
                    return super.getView(position, convertView, parent);
                }

                return convertView;
            }
        };

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
    public void onEvent(Initialisation event) {

        // Get Controller for Comapi calls
        controller = event.getController();
        // Listen for messaging events
        controller.setMessageListener(conversationId, this);
        // Show loading data progress bar
        overlay.setVisibility(View.VISIBLE);
        // Trigger message query for the opened conversation.
        controller.getMessages(conversationId);
    }

    @Override
    public void onAddMessage(Long conversationEventId, String from, boolean isMyOwn, String body) {

        /*
         A new message was received from socket or message query
         */

        // Add data and sort messages
        data.put(conversationEventId, new MessageItem(body, from, isMyOwn));
        // Populate adapter data with sorted messages
        dataArray.clear();
        dataArray.addAll(data.values());
        // Notify adapter to refresh list view
        adapter.notifyDataSetChanged();
        // Hide progress bar
        overlay.setVisibility(View.GONE);
    }

    @Override
    public void onEmptyMessageList() {

        /*
         No messages in conversation so hide progress bar.
         */

        // Clear data store.
        data.clear();
        dataArray.clear();
        // Notify adapter to refresh list view
        adapter.notifyDataSetChanged();
        // Hide progress bar
        overlay.setVisibility(View.GONE);
    }
}
