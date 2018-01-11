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

package com.comapi.sample.comapi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.comapi.Callback;
import com.comapi.QueryBuilder;
import com.comapi.Session;
import com.comapi.chat.ChatResult;
import com.comapi.chat.ComapiChatClient;
import com.comapi.chat.listeners.ParticipantsListener;
import com.comapi.chat.model.ChatParticipant;
import com.comapi.internal.network.ComapiResult;
import com.comapi.internal.network.model.conversation.ConversationCreate;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Role;
import com.comapi.internal.network.model.conversation.Roles;
import com.comapi.internal.network.model.events.conversation.ParticipantAddedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantRemovedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantUpdatedEvent;
import com.comapi.sample.Const;
import com.comapi.sample.ui.ManageParticipantsActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rx.Observable;

/**
 * Controller class wrapping Comapi Client and holds UI listeners.
 *
 * @author Marcin Swierczek
 */
public class ComapiController {

    /**
     * Comapi Chat SDK client obtained from initialisation in SampleApplication#onCreate
     */
    private ComapiChatClient client;

    /**
     * UI callback for comapiChatClient.messaging().previousMessages() call. Used to hide swipe to refresh widget when paging API finishes.
     */
    private UICallback pagingCallback;

    /**
     * UI callback for comapiChatClient.messaging().synchroniseStore() call. Used to hide swipe to refresh widget when API finishes.
     */
    private UICallback synchroniseCallback;

    /**
     * UI callback for comapiChatClient.messaging().getParticipants() call. Used to hide swipe to refresh widget when API finishes.
     */
    private ManageParticipantsActivity.UiParticipantsListener uiParticipantsListener;

    /**
     * Listener for changes to participants list for a conversation. Used to update a screen with conversation participants.
     */
    private ParticipantsListener comapiParticipantsListener;

    /**
     * Creates an Comapi service API wrapper
     */
    public ServiceAPIWrapper getService() {
        return new ServiceAPIWrapper();
    }

    /**
     * Sets ComapiChatClient obtained from  SDK initialisation
     */
    void setClient(ComapiChatClient client) {
        this.client = client;
    }

    /**
     * Gets ComapiChatClient obtained from  SDK initialisation
     */
    ComapiChatClient getClient() {
        return client;
    }

    /**
     * Sets UI callback for comapiChatClient.messaging().previousMessages() call. Used to hide swipe to refresh widget when paging API finishes.
     */
    public void setPagingCallback(UICallback pagingCallback) {
        this.pagingCallback = pagingCallback;
    }

    /**
     * Removes UI callback for comapiChatClient.messaging().previousMessages() call.
     */
    public void removePagingCallback() {
        this.pagingCallback = null;
    }

    /**
     * Sets UI callback for comapiChatClient.messaging().synchroniseStore() call. Used to hide swipe to refresh widget for list of conversations when API finishes.
     */
    public void setSynchroniseCallback(UICallback synchroniseCallback) {
        this.synchroniseCallback = synchroniseCallback;
    }

    /**
     * Removes UI callback for comapiChatClient.messaging().synchroniseStore() call.
     */
    public void removeSynchroniseCallback() {
        this.synchroniseCallback = null;
    }

    /**
     * Sets listener for participants added/removed from conversation events.
     */
    public void setListener(String conversationId, final ManageParticipantsActivity.UiParticipantsListener uiParticipantsListener) {

        this.uiParticipantsListener = uiParticipantsListener;

        // create SDK listener
        comapiParticipantsListener = new ParticipantsListener() {
            @Override
            public void onParticipantAdded(ParticipantAddedEvent event) {
                if (uiParticipantsListener != null) {
                    // notify UI about newly added participant
                    uiParticipantsListener.add(event.getProfileId());
                }
            }

            @Override
            public void onParticipantUpdated(ParticipantUpdatedEvent event) {
                // sample app won't display and use any details about conversation participants, ignoring this callback
            }

            @Override
            public void onParticipantRemoved(ParticipantRemovedEvent event) {
                if (uiParticipantsListener != null) {
                    // notify UI about removed participant
                    uiParticipantsListener.remove(event.getProfileId());
                }
            }
        };

        // pass listener object to Comapi client
        client.addListener(comapiParticipantsListener);
        // query participants in a conversation after listener has been added, will update ManageParticipantsActivity with most recent data
        getService().checkParticipants(conversationId);
    }

    /**
     * Removes UI callback for comapiChatClient.messaging().getParticipants() call.
     */
    public void removeParticipantsListener() {
        // Remove listener from Comapi client
        client.removeListener(comapiParticipantsListener);
        // Remove UI listener from app controller
        this.uiParticipantsListener = null;
    }

    // Class wrapping Comapi client APIs
    public class ServiceAPIWrapper {

        /**
         * Send message to the conversation participants.
         *
         * @param conversationId Conversation unique id.
         * @param message        Message body to send.
         */
        public void sendMessage(String conversationId, String message) {

            if (getClient() != null) {
                // Send message to a conversation participants
                getClient().service().messaging().sendMessage(conversationId, message, new Callback<ChatResult>() {

                    @Override
                    public void success(ChatResult result) {
                        if (result.isSuccessful()) {
                            Log.i(Const.TAG, "Successfully sent message");
                        } else {
                            Log.e(Const.TAG, "Error sending message: ");
                        }
                    }

                    @Override
                    public void error(Throwable t) {
                        Log.e(Const.TAG, "Error sending message: " + t.getLocalizedMessage());
                    }
                });
            }
        }

        /**
         * Add participant to a conversation.
         *
         * @param context        Application context
         * @param conversationId Conversation unique id.
         * @param participantId  Profile id of a new participant to add.
         */
        public void addParticipant(final Context context, final String conversationId, final String participantId) {

            final List<Participant> participants = new ArrayList<>();
            participants.add(Participant.builder().setId(participantId).setIsParticipant().build());

            // first check if user exist for registered ApiSpace
            getClient().service().profile().queryProfiles(new QueryBuilder().addEqual("id", participantId).build(), new Callback<ComapiResult<List<Map<String, Object>>>>() {

                @Override
                public void success(ComapiResult<List<Map<String, Object>>> result) {
                    if (result.isSuccessful() && !result.getResult().isEmpty() && result.getResult() != null) {
                        // user exists, we can add him to the conversation participants list
                        getClient().service().messaging().addParticipants(conversationId, participants, null);
                    } else {
                        Toast.makeText(context.getApplicationContext(), "User doesn't exist.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void error(Throwable t) {
                    Log.e(Const.TAG, "Error querying participants: " + t.getLocalizedMessage());
                }
            });
        }

        /**
         * Query conversation participants and notify UI listener.
         */
        private void checkParticipants(final String conversationId) {
            // query conversation participants
            client.service().messaging().getParticipants(conversationId, new Callback<List<ChatParticipant>>() {
                @Override
                public void success(List<ChatParticipant> participantList) {
                    if (uiParticipantsListener != null) {
                        for (ChatParticipant p : participantList) {
                            // notify UI about found participant
                            uiParticipantsListener.add(p.getParticipantId());
                        }
                    }
                }

                @Override
                public void error(Throwable t) {
                    // Error occurred
                }
            });
        }

        /**
         * Remove participant from a conversation.
         *
         * @param conversationId Conversation unique id.
         * @param participantId  Profile id of a participant to remove.
         */
        public void removeParticipant(String conversationId, String participantId) {
            List<String> participants = new ArrayList<>();
            participants.add(participantId);
            getClient().service().messaging().removeParticipants(conversationId, participants, null);
        }

        /**
         * Create new conversation.
         *
         * @param name Public name of a new conversation.
         */
        public void createConversation(String name) {
            getClient().service().messaging().createConversation(createConversationRequest(name), null);
        }

        /**
         * Start authenticated session in Comapi SDK. The profile id will be obtained from JWT token during authentication challenge.
         *
         * @param callback Callback with the call result.
         */
        public void startSession(Callback<Session> callback) {
            getClient().service().session().startSession(callback);
        }

        /**
         * Synchronise local persistence store with Comapi Servers
         */
        public void synchronise() {

            getClient().service().messaging().synchroniseStore(new Callback<ChatResult>() {

                @Override
                public void success(ChatResult result) {
                    // notify UI listener
                    if (synchroniseCallback != null) {
                        synchroniseCallback.finished(result.isSuccessful());
                    }
                    Session s = client.getSession();
                    if (!s.isSuccessfullyCreated()) {
                        startSession(null);
                    }
                }

                @Override
                public void error(Throwable t) {
                    // notify UI listener
                    Log.e(Const.TAG, t.getLocalizedMessage());
                    if (synchroniseCallback != null) {
                        synchroniseCallback.finished(false);
                    }
                }
            });
        }

        /**
         * Get next message page.
         *
         * @param conversationId Conversation unique id.
         */
        public void getNextPage(String conversationId) {

            getClient().service().messaging().getPreviousMessages(conversationId, new Callback<ChatResult>() {

                @Override
                public void success(ChatResult result) {
                    // notify UI listener
                    if (pagingCallback != null) {
                        pagingCallback.finished(result.isSuccessful());
                    }
                }

                @Override
                public void error(Throwable t) {
                    Log.e(Const.TAG, t.getLocalizedMessage());
                    // notify UI listener
                    if (pagingCallback != null) {
                        pagingCallback.finished(false);
                    }
                }
            });
        }

        /**
         * Create a request with the details of a new conversation to create.
         *
         * @param name Public name of a new conversation.
         * @return Request with the details of a new conversation to create.
         */
        private ConversationCreate createConversationRequest(String name) {

            return ConversationCreate.builder()
                    .setId(UUID.randomUUID().toString()) // let the id be a random UUID
                    .setName(name) // Visible conversation name
                    .setPublic(false)
                    .setRoles(new Roles(
                            Role.builder() // Owner of this conversation will have all available permissions
                                    .setCanAddParticipants(true)
                                    .setCanRemoveParticipants(true)
                                    .setCanSend(true)
                                    .build(),
                            Role.builder() // Participants in this conversation will have all available permissions
                                    .setCanAddParticipants(true)
                                    .setCanRemoveParticipants(true)
                                    .setCanSend(true)
                                    .build()))
                    .build();
        }
    }

    /**
     * interface for UI callbacks
     */
    public interface UICallback {

        /**
         * Tell UI that the Comapi call was finished
         *
         * @param isSuccess True if call was successful
         */
        void finished(boolean isSuccess);
    }

    //TEST
    public Observable<File> getLogs(Context context, String fileName) {
        File file = null;
        try {
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
        }
        return client.copyLogs(file);
    }

    public Observable<String> getLogs() {
        return client.getLogs();
    }
}