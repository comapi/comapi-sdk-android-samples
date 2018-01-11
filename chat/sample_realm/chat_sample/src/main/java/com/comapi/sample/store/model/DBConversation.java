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

import android.support.annotation.Nullable;

import com.comapi.chat.model.ChatConversation;
import com.comapi.chat.model.ChatConversationBase;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Realm database object describing a chat conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class DBConversation extends RealmObject {

    public static final String CONVERSATION_ID = "conversationId";
    public static final String IS_DELETED = "isDeleted";
    public static final String UPDATED_ON = "updatedOn";

    @PrimaryKey
    private String conversationId;
    private String name;
    private Long firstLocalEventId;
    private Long lastLocalEventId;
    private Long latestRemoteEventId;
    private Long updatedOn;
    private String eTag;
    @SuppressWarnings("unused")
    private boolean isDeleted;

    /**
     * Recommended constructor.
     */
    public DBConversation() {
        // Set the initial value of soft deletion flag
        isDeleted = false;
    }

    /**
     * Get unique conversation id.
     *
     * @return Unique conversation id.
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Set unique conversation id.
     *
     * @param conversationId Unique conversation id.
     * @return Chat conversation instance with the value being set.
     */
    public DBConversation setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * Get custom conversation name.
     *
     * @return Get custom conversation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set custom conversation name.
     *
     * @param name Conversation custom name.
     * @return Chat conversation instance with the value being set.
     */
    public DBConversation setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set conversation soft deleted flag.
     *
     * @param deleted True if conversation was soft deleted.
     * @return Chat conversation instance with the value being set.
     */
    public DBConversation setDeleted(boolean deleted) {
        isDeleted = deleted;
        return this;
    }

    /**
     * Set first local event id. Field required by the chat SDK. Field required and managed by the chat SDK (app is only storing this value and passing to SDK).
     *
     * @param firstLocalEventId First local event id.
     * @return Chat conversation instance with the value being set.
     */
    private DBConversation setFirstLocalEventId(Long firstLocalEventId) {
        this.firstLocalEventId = firstLocalEventId;
        return this;
    }

    /**
     * Set last local event id. Field required by the chat SDK. Field required and managed by the chat SDK (app is only storing this value and passing to SDK).
     *
     * @param lastLocalEventId Last local event id.
     * @return Chat conversation instance with the value being set.
     */
    private DBConversation setLastLocalEventId(Long lastLocalEventId) {
        this.lastLocalEventId = lastLocalEventId;
        return this;
    }

    /**
     * Set first local event id. Field required by the chat SDK. Field required and managed by the chat SDK (app is only storing this value and passing to SDK).
     *
     * @param latestRemoteEventId Last remote event id.
     * @return Chat conversation instance with the value being set.
     */
    private DBConversation setLatestRemoteEventId(Long latestRemoteEventId) {
        this.latestRemoteEventId = latestRemoteEventId;
        return this;
    }

    /**
     * Set UTC time when conversation updated on. Field required by the chat SDK. Field required and managed by the chat SDK (app is only storing this value and passing to SDK).
     *
     * @param updatedOn UTC time when conversation updated on.
     * @return Chat conversation instance with the value being set.
     */
    private DBConversation setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    /**
     * ETag for server to check if local version of the data is the same as the one the server side. Field required and managed by the chat SDK (app is only storing this value and passing to SDK).
     *
     * @param eTag ETag for server to check if local version of the data is the same as the one the server side.
     * @return Chat conversation instance with the value being set.
     */
    private DBConversation setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Adapt Realm db object to Comapi Chat SDK interface.
     *
     * @param dbConversation Realm db object for conversation details.
     * @return Chat conversation instance with the value being set.
     */
    public static ChatConversationBase adapt(@Nullable DBConversation dbConversation) {

        if (dbConversation == null) {
            return null;
        }

        return ChatConversationBase
                .baseBuilder()
                .setConversationId(dbConversation.conversationId)
                .setLastLocalEventId(dbConversation.lastLocalEventId)
                .setFirstLocalEventId(dbConversation.firstLocalEventId)
                .setLastRemoteEventId(dbConversation.latestRemoteEventId)
                .setUpdatedOn(dbConversation.updatedOn)
                .setETag(dbConversation.eTag)
                .build();
    }

    /**
     * Adapt Comapi Chat SDK conversation object to Realm db object.
     *
     * @param c Comapi Chat SDK conversation.
     * @return Chat conversation instance with the value being set.
     */
    public static DBConversation adapt(ChatConversation c) {
        return new DBConversation()
                .setDeleted(false)
                .setConversationId(c.getConversationId())
                .setUpdatedOn(c.getUpdatedOn())
                .setFirstLocalEventId(c.getFirstLocalEventId())
                .setLatestRemoteEventId(c.getLastRemoteEventId())
                .setLastLocalEventId(c.getLastLocalEventId())
                .setETag(c.getETag())
                .setName(c.getName());
    }

    /**
     * Update the realm db object with the details from Comapi Chat SDK.
     *
     * @param c Comapi Chat SDK conversation.
     */
    public void updateFrom(ChatConversationBase c) {
        setFirstLocalEventId(c.getFirstLocalEventId());
        setLastLocalEventId(c.getLastLocalEventId());
        setETag(c.getETag());
        setLatestRemoteEventId(c.getLastRemoteEventId());
        setUpdatedOn(c.getUpdatedOn());
        setDeleted(false);
    }

    /**
     * Adapt list of realm db conversation object to Comapi Chat SDK.
     *
     * @param conversations List of realm db conversation objects.
     * @return List of Comapi Chat SDK conversation objects.
     */
    public static List<ChatConversationBase> adapt(RealmResults<DBConversation> conversations) {
        List<ChatConversationBase> result = new ArrayList<>();
        for (DBConversation c : conversations) {
            result.add(adapt(c));
        }
        return result;
    }
}