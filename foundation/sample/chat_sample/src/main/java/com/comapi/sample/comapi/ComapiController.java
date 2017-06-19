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
import com.comapi.ComapiClient;
import com.comapi.QueryBuilder;
import com.comapi.Session;
import com.comapi.internal.network.ComapiResult;
import com.comapi.internal.network.model.conversation.ConversationCreate;
import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Role;
import com.comapi.internal.network.model.conversation.Roles;
import com.comapi.internal.network.model.conversation.Scope;
import com.comapi.internal.network.model.messaging.MessageReceived;
import com.comapi.internal.network.model.messaging.MessageSentResponse;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.MessagesQueryResponse;
import com.comapi.internal.network.model.messaging.Part;
import com.comapi.sample.Const;
import com.comapi.sample.events.LoginEvent;
import com.comapi.sample.ui.listeners.ConversationListener;
import com.comapi.sample.ui.listeners.MessageListener;
import com.comapi.sample.ui.listeners.ParticipantsListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller wrapping Comapi Client.
 *
 * @author Marcin Swierczek
 */
public class ComapiController {

    /**
     * Comapi Client instance, contains public interfaces of Comapi Foundation APIs.
     */
    private ComapiClient client;

    /**
     * Listeners to changes in the list of participants.
     * The key in the map is a conversation id.
     */
    private Map<String, ParticipantsListener> participantsListenerMap;

    /**
     * Listeners for new conversations.
     */
    private List<ConversationListener> conversationListeners;

    /**
     * Listeners for new messages.
     * The key in the map is a conversation id.
     */
    private Map<String, MessageListener> messageListeners;

    /**
     * Recommended constructor.
     */
    public ComapiController() {
        participantsListenerMap = new HashMap<>();
        conversationListeners = new ArrayList<>();
        messageListeners = new HashMap<>();
    }

    /**
     * Sets Comapi client when SDK finishes initialisation.
     *
     * @param client Comapi SDK client.
     */
    public void setClient(ComapiClient client) {
        this.client = client;
    }

    /**
     * Gets profile id registered in Comapi SDK.
     *
     * @return Profile id registered in Comapi SDK.
     */
    public String getProfileId() {
        return client != null ? client.getSession().getProfileId() : null;
    }

    /**
     * Send message to the conversation participants.
     *
     * @param conversationId Conversation unique id.
     * @param message        Message body to send.
     */
    public void sendMessage(String conversationId, String message) {

        if (client != null) {

            // Send message to a conversation participants
            client.service().messaging().sendMessage(conversationId, createMessageToSend(message), new Callback<ComapiResult<MessageSentResponse>>() {
                @Override
                public void success(ComapiResult<MessageSentResponse> result) {
                    Log.i(Const.TAG, "Successfully sent message");
                }

                @Override
                public void error(Throwable t) {
                    Log.e(Const.TAG, "Error sending message: " + t.getLocalizedMessage());
                }
            });
        }
    }

    /**
     * Create new message object for Comapi Client APIs that can be send to conversation participants.
     *
     * @param message Message body.
     * @return New message object for Comapi Client APIs.
     */
    private MessageToSend createMessageToSend(final String message) {

        // Message can contain many part. We will be creating only one. It contains plain text with message body.
        Part bodyPart = Part.builder()
                .setData(message)
                .setName("body")
                .setSize(message.length())
                .setType("text/plain")
                .build();

        // Build message object.
        return MessageToSend.builder().addPart(bodyPart).build();
    }

    /**
     * Listen for events of adding or removing participants in a conversation.
     *
     * @param conversationId Conversation unique id.
     * @param listener       Listener instance to register.
     */
    public void setParticipantsListener(final String conversationId, ParticipantsListener listener) {
        participantsListenerMap.put(conversationId, listener);
    }

    /**
     * Remove listener for adding and removing participants in a conversation.
     *
     * @param conversationId Conversation unique id.
     */
    public void removeParticipantsListener(String conversationId) {
        participantsListenerMap.remove(conversationId);
    }

    /**
     * Trigger participants query for a given conversation. Registered ParticipantsListener's will be notified so that appropriate Activity can populate list view.
     *
     * @param conversationId Conversation unique id.
     */
    public void getParticipants(final String conversationId) {
        client.service().messaging().getParticipants(conversationId, new Callback<ComapiResult<List<Participant>>>() {
            @Override
            public void success(ComapiResult<List<Participant>> result) {
                Log.i(Const.TAG, "Notifying listeners about conversation participants.");
                for (Participant participant : result.getResult()) {
                    notifyParticipantAdded(conversationId, participant.getId());
                }
            }

            @Override
            public void error(Throwable t) {
                Log.e(Const.TAG, "Error getting conversation participants: " + t.getLocalizedMessage());
            }
        });
    }

    /**
     * Add participant to a conversation.
     *
     * @param conversationId Conversation unique id.
     * @param participantId  Profile id of a new participant to add.
     */
    public void addParticipant(final Context context, final String conversationId, final String participantId) {
        final List<Participant> participants = new ArrayList<>();
        participants.add(Participant.builder().setId(participantId).setIsParticipant().build());
        client.service().profile().queryProfiles(new QueryBuilder().addEqual("id", participantId).build(), new Callback<ComapiResult<List<Map<String, Object>>>>() {
            @Override
            public void success(ComapiResult<List<Map<String, Object>>> result) {
                if (result.isSuccessful() && !result.getResult().isEmpty() && result.getResult() != null) {
                    client.service().messaging().addParticipants(conversationId, participants, null);
                } else {
                    Toast.makeText(context.getApplicationContext(), "User doesn't exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void error(Throwable t) {

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
        client.service().messaging().removeParticipants(conversationId, participants, null);
    }

    /**
     * Notify registered listeners about participant of a conversation to add to the list.
     *
     * @param conversationId Conversation unique id.
     * @param participantId  Profile id of a participant to add.
     */
    void notifyParticipantAdded(String conversationId, String participantId) {
        ParticipantsListener listener = participantsListenerMap.get(conversationId);
        if (listener != null) {
            listener.onAddParticipant(participantId);
        }
    }

    /**
     * Notify registered listeners about participant of a conversation to remove from the list.
     *
     * @param conversationId Conversation unique id.
     * @param participantId  Profile id of a participant to remove.
     */
    void notifyParticipantRemoved(String conversationId, String participantId) {
        ParticipantsListener listener = participantsListenerMap.get(conversationId);
        if (listener != null) {
            listener.onRemoveParticipant(participantId);
        }
    }

    /**
     * Create new conversation.
     *
     * @param name Public name of a new conversation.
     */
    public void createConversation(String name) {
        client.service().messaging().createConversation(createConversationRequest(name), null);
    }

    /**
     * Create a request with the details of a new conversation to create.
     *
     * @param name Public name of a new conversation.
     * @return Request with the details of a new conversation to create.
     */
    private ConversationCreate createConversationRequest(String name) {
        return ConversationCreate.builder()
                .setId(UUID.randomUUID().toString())
                .setName(name)
                .setPublic(false)
                .setRoles(new Roles(
                        Role.builder()
                                .setCanAddParticipants()
                                .setCanRemoveParticipants()
                                .setCanSend()
                                .build(),
                        Role.builder()
                                .setCanAddParticipants()
                                .setCanRemoveParticipants()
                                .setCanSend()
                                .build()))
                .build();
    }

    /**
     * Trigger conversation query for which logged in user is a participant or owner. Registered ConversationListener's will be notified so that appropriate Activity can populate list view.
     */
    public void getConversations() {

        client.service().messaging().getConversations(Scope.PARTICIPANT, new Callback<ComapiResult<List<ConversationDetails>>>() {
            @Override
            public void success(ComapiResult<List<ConversationDetails>> result) {

                Log.i(Const.TAG, "Notifying listeners about conversations.");

                if (!result.getResult().isEmpty()) {
                    for (ConversationDetails conversation : result.getResult()) {
                        notifyConversationAdded(conversation.getId(), conversation.getName());
                    }
                } else {
                    notifyConversationsEmpty();
                }
            }

            @Override
            public void error(Throwable t) {
                Log.e(Const.TAG, "Error getting conversations: " + t.getLocalizedMessage());
            }
        });
    }

    public void getMessages(final String conversationId) {

        client.service().messaging().queryMessages(conversationId, null, 100, new Callback<ComapiResult<MessagesQueryResponse>>() {
            @Override
            public void success(ComapiResult<MessagesQueryResponse> result) {

                Log.i(Const.TAG, "Notifying listeners about messages in a conversation " + conversationId);

                if (result.getResult().getMessages() != null && !result.getResult().getMessages().isEmpty()) {
                    for (MessageReceived msg : result.getResult().getMessages()) {
                        notifyMessageAdded(msg.getConversationId(), msg.getSentEventId(), msg.getFromWhom().getId(), msg.getFromWhom().getId().equals(getProfileId()), msg.getParts().get(0).getData());
                    }
                    return;
                }

                notifyMessagesEmpty(conversationId);
            }

            @Override
            public void error(Throwable t) {
                Log.e(Const.TAG, "Error getting messages: " + t.getLocalizedMessage());
            }
        });
    }

    /**
     * Listen for events of a conversation to add.
     *
     * @param listener Listener instance to register.
     */
    public void setConversationListener(final ConversationListener listener) {
        conversationListeners.add(listener);
    }

    /**
     * Remove listener for conversations to add.
     *
     * @param listener Listener instance to remove.
     */
    public void removeConversationListener(final ConversationListener listener) {
        conversationListeners.remove(listener);
    }

    /**
     * Notify registered listeners about a conversation to add to the list.
     *
     * @param conversationId   Conversation unique id.
     * @param conversationName Conversation name.
     */
    private void notifyConversationAdded(String conversationId, String conversationName) {
        if (!conversationListeners.isEmpty()) {
            for (ConversationListener listener : conversationListeners) {
                if (listener != null) {
                    listener.onAddConversation(conversationId, conversationName);
                }
            }
        }
    }

    /**
     * Notify registered listeners about an empty conversation list in query response.
     */
    private void notifyConversationsEmpty() {
        if (!conversationListeners.isEmpty()) {
            for (ConversationListener listener : conversationListeners) {
                if (listener != null) {
                    listener.onEmptyConversationList();
                }
            }
        }
    }

    /**
     * Listen for events of a message to add.
     *
     * @param listener Listener instance to register.
     */
    public void setMessageListener(final String conversationId, final MessageListener listener) {
        messageListeners.put(conversationId, listener);
    }

    /**
     * Remove listener for messages to add.
     *
     * @param conversationId Conversation unique id for which listener instance should be removed.
     */
    public void unregisterMessageListener(final String conversationId) {
        messageListeners.remove(conversationId);
    }

    /**
     * Notify registered listeners about a message to add to the list.
     *
     * @param conversationId      Conversation unique id. Received message belongs to this conversation.
     * @param conversationEventId Monotonically increasing in a single conversation number of event assigned to received message.
     * @param from                Message sender profile id.
     * @param isMyOwn             True if send by the user logged in in the app.
     * @param body                Plain text message body.
     */
    void notifyMessageAdded(String conversationId, Long conversationEventId, String from, boolean isMyOwn, String body) {
        MessageListener listener = messageListeners.get(conversationId);
        if (listener != null) {
            listener.onAddMessage(conversationEventId, from, isMyOwn, body);
        }
    }

    /**
     * Notify registered listeners about an empty message list in query response.
     */
    private void notifyMessagesEmpty(String conversationId) {
        MessageListener listener = messageListeners.get(conversationId);
        if (listener != null) {
            listener.onEmptyMessageList();
        }
    }

    /**
     * Gets conversation details and notifies ConversationListeners.
     *
     * @param conversationId Conversation unique id.
     */
    void getConversation(final String conversationId) {

        client.service().messaging().getConversation(conversationId, new Callback<ComapiResult<ConversationDetails>>() {
            @Override
            public void success(ComapiResult<ConversationDetails> result) {

                Log.i(Const.TAG, "Notifying listeners about a conversation " + conversationId);

                if (result.isSuccessful()) {
                    notifyConversationAdded(result.getResult().getId(), result.getResult().getName());
                }
            }

            @Override
            public void error(Throwable t) {
                Log.e(Const.TAG, "Error getting conversation: " + t.getLocalizedMessage());
            }
        });
    }

    /**
     * Start authenticated session in Comapi SDK. The profile id will be obtained from JWT token during authentication challenge.
     */
    public void startSession() {

        client.service().session().startSession(new Callback<Session>() {
            @Override
            public void success(Session result) {

                Log.i(Const.TAG, "Successfully started session");

                // Post login finished event to EventBus 
                EventBus.getDefault().postSticky(new LoginEvent(result.isSuccessfullyCreated()));
            }

            @Override
            public void error(Throwable t) {

                Log.e(Const.TAG, "Error starting session: " + t.getLocalizedMessage());

                // Post login finished event to EventBus 
                EventBus.getDefault().postSticky(new LoginEvent(client.getSession().isSuccessfullyCreated()));
            }
        });
    }
}