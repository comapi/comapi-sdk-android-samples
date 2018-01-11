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

package com.comapi.sample.store.model;

import android.text.TextUtils;

import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;
import com.comapi.chat.model.LocalMessageStatus;
import com.comapi.internal.network.model.messaging.Part;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Realm database object describing a chat message.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DBChatMessage extends RealmObject {

    public static final String CONVERSATION_ID = "conversationId";
    public static final String MESSAGE_ID = "messageId";
    public static final String SENT_ID = "sentId";

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @PrimaryKey
    private String messageId;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Required
    @Index
    private String conversationId;

    private String body;
    private boolean isMyOwn;
    private String sender;
    private Long timestamp;
    private RealmList<DBMessageStatus> statuses;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private long sentId;

    /**
     * Set message unique identifier.
     *
     * @param messageId Message unique identifier.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Set unique conversation id.
     *
     * @param conversationId Unique conversation id.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * Get message text body.
     *
     * @return Message text body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Set message text body.
     *
     * @param body Message text body.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Is the message sent by the logged in user.
     *
     * @return True if the message sent by the logged in user.
     */
    public boolean isMyOwn() {
        return isMyOwn;
    }

    /**
     * Set if message was sent by the logged in user.
     *
     * @param myOwn True if message was sent by the logged in user.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setMyOwn(boolean myOwn) {
        isMyOwn = myOwn;
        return this;
    }

    /**
     * Profile id of the message sender.
     *
     * @return Profile id of the message sender.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Set profile id of the message sender.
     *
     * @param sender Profile id of the message sender.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setSender(String sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Get UTC time when the message was sent.
     *
     * @return UTC time when the message was sent.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Set UTC time when the message was sent.
     *
     * @param timestamp UTC time when the message was sent.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Set message sent event id used to order messages in a conversation.
     *
     * @param sentId Message sent event id.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage setSentId(long sentId) {
        this.sentId = sentId;
        return this;
    }

    /**
     * Adapt Comapi Chat SDK message object to Realm object interface.
     *
     * @param m             Comapi Chat SDK message object.
     * @param userProfileId Logged in user profile id.
     * @return Chat message instance with the value being set.
     */
    public static DBChatMessage adapt(ChatMessage m, String userProfileId) {

        // Get the text message body from the first Part. The SDK by default is putting the text body into part with name "body" and type "text/plain".
        String body = null;
        if (m.getParts() != null && !m.getParts().isEmpty()) {
            for (Part p : m.getParts()) {
                if (TextUtils.equals("body", p.getName())) {
                    body = m.getParts().get(0).getData();
                    break;
                }
            }
        }

        // Check if message was sent from logged in user.
        boolean isMyOwn = m.getFromWhom().getId().equals(userProfileId);
        DBChatMessage message = new DBChatMessage();

        if (isMyOwn) {
            message.statuses = new RealmList<>();
            if (m.getStatusUpdates() == null) {
                message.statuses.add(new DBMessageStatus(userProfileId, LocalMessageStatus.sending));
            } else {
                for (ChatMessageStatus status : m.getStatusUpdates()) {
                    message.statuses.add(new DBMessageStatus(status));
                }
            }
        }

        return message.setConversationId(m.getConversationId())
                .setBody(body)
                .setTimestamp(m.getSentOn())
                .setMyOwn(isMyOwn)
                .setMessageId(m.getMessageId())
                .setSender(m.getFromWhom().getId())
                .setSentId(m.getSentEventId());
    }

    /**
     * Update message with a new status.
     *
     * @param status Comapi Chat SDK message status.
     * @return Chat message instance with the value being set.
     */
    public DBChatMessage updateStatus(ChatMessageStatus status) {
        if (statuses == null) {
            statuses = new RealmList<>();
        }
        statuses.add(new DBMessageStatus(status));
        return this;
    }

    /**
     * Get statuses for this message.
     *
     * @return Statuses for this message.
     */
    public List<DBMessageStatus> getStatusUpdates() {
        return statuses;
    }
}