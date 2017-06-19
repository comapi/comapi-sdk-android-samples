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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.comapi.sample.R;
import com.comapi.sample.comapi.ComapiController;
import com.comapi.sample.events.Initialisation;
import com.comapi.sample.ui.holders.ParticipantViewHolder;
import com.comapi.sample.ui.listeners.ParticipantsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Activity displaying participants for a conversation. Allows to add and remove participants.
 *
 * @author Marcin Swierczek
 */
public class ManageParticipantsActivity extends AppCompatActivity implements ParticipantsListener {

    /**
     * Participants ids passed to adapter.
     */
    private ArrayList<String> data;

    /**
     * Adapter for participants list view
     */
    private ArrayAdapter<String> adapter;

    /**
     * Edit text to type in profile id of a user to add as participant to conversation.
     */
    private EditText participantEditText;

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private ComapiController controller;

    /**
     * Conversation unique identifier for which this Activity should display messages.
     */
    private String conversationId;

    /**
     * Overlay with progress bar displayed when loading data.
     */
    private ViewGroup overlay;
    private String conversationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_participants);

        // Retrieve details of conversation to display participants for.
        conversationId = getIntent().getStringExtra(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_ID);
        conversationName = getIntent().getStringExtra(ConversationListActivity.KEY_INTENT_BUNDLE_CONVERSATION_NAME);

        setupViews();
        bindData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister this Activity from global EventBus
        EventBus.getDefault().unregister(this);
        // Remove listener for participants in active Conversation
        if (controller != null) {
            controller.removeParticipantsListener(conversationId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register this Activity to global EventBus to get SDK Initialisation sticky event with Comapi controller - see ManageParticipantsActivity#onEvent(Initialisation)
        EventBus.getDefault().register(this);

    }

    private void setupViews() {

        // Set conversation name in description
        TextView nameTextView = (TextView) findViewById(R.id.conversation_name);
        nameTextView.setText(conversationName);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_manage_participants);
        setSupportActionBar(toolbar);

        // Find edit text to type in profile id of participant to add
        participantEditText = (EditText) findViewById(R.id.add_participants_edit_text);

        // Setup button to confirm adding participant with provided profile id.
        final Button addButton = (Button) findViewById(R.id.add_participants_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String participantId = participantEditText.getText().toString();
                if (!TextUtils.isEmpty(participantId)) {
                    // Send request to add participant to this conversation.
                    controller.addParticipant(ManageParticipantsActivity.this.getApplicationContext(), conversationId, participantId);
                    participantEditText.setText("");
                }
            }
        });

        // 'Done' button is to close the activity
        Button createButton = (Button) findViewById(R.id.done_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Find progress bar overlay
        overlay = (ViewGroup) findViewById(R.id.overlay);
    }

    private void bindData() {

        ListView listView = (ListView) findViewById(R.id.list_of_participants);
        listView.setEmptyView(findViewById(android.R.id.empty));

        data = new ArrayList<>();
        //adapter = new ArrayAdapter<>(this,  android.R.layout.simple_list_item_1, data);
        adapter = new ArrayAdapter<String>(this, 0, data) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                ParticipantViewHolder viewHolder;

                // Inflate only once
                if (convertView == null) {
                    convertView = getLayoutInflater()
                            .inflate(R.layout.participants_list_row, null, false);
                    viewHolder = new ParticipantViewHolder();
                    viewHolder.profileId =
                            (TextView) convertView.findViewById(R.id.profileId);
                    viewHolder.delete =
                            (ImageView) convertView.findViewById(R.id.delete);
                    convertView.setTag(viewHolder);

                } else {
                    viewHolder = (ParticipantViewHolder) convertView.getTag();
                }

                String item = getItem(position);
                if (item != null) {
                    viewHolder.profileId.setText(item);
                    if (item.equals(controller.getProfileId())) {
                        viewHolder.delete.setVisibility(View.INVISIBLE);
                    } else {
                        viewHolder.delete.setVisibility(View.VISIBLE);
                        viewHolder.delete.setTag(item);
                        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                controller.removeParticipant(conversationId, (String) v.getTag());
                            }
                        });
                    }
                }

                return convertView;
            }
        };
        listView.setAdapter(adapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(Initialisation event) {

        /*
         Comapi SDK finished initialising. If event.getController() is not null then initialisation was successful.
         */

        controller = event.getController();
        // Listen to add/remove participants events.
        controller.setParticipantsListener(conversationId, this);
        // Show loading data progress bar
        overlay.setVisibility(View.VISIBLE);
        // Trigger participants query for the opened conversation.
        controller.getParticipants(conversationId);
    }

    @Override
    public void onAddParticipant(String participant) {

        /*
         Participant was added to Conversation. Triggered by e.g. socket event.
         */

        if (!data.contains(participant)) {
            data.add(participant);
            overlay.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRemoveParticipant(String participant) {

        /*
         Participant was removed from Conversation. Triggered e.g. by socket event.
         */

        data.remove(participant);
        adapter.notifyDataSetChanged();
    }
}
