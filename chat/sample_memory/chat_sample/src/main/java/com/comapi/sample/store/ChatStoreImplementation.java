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

package com.comapi.sample.store;

import com.comapi.chat.model.ChatConversation;
import com.comapi.chat.model.ChatConversationBase;
import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;
import com.comapi.chat.model.ChatStore;
import com.comapi.sample.ui.model.UIMessageItem;

import java.util.List;

/**
 * ChatStore interface implementation that executes all changes to in-memory persistance storage in a single transaction after a call to {@link ChatStore#endTransaction()} has been made.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class ChatStoreImplementation extends ChatStore {

    /**
     * Chat data.
     */
    private final ChatStoreData data;

    /**
     * UI listeners for a conversation created and deleted events.
     */
    private final UIListener<ChatConversation> conversationListener;

    /**
     * UI listeners for a message created and deleted events and for message status updates.
     */
    private final UIListener<UIMessageItem> messageListener;

    /**
     * Object to queue store modification requests.
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Transaction transaction;

    /**
     * Recommended constructor.
     *
     * @param data                  Chat data.
     * @param conversationListeners UI listeners for a conversation created and deleted events.
     * @param messageListeners      UI listeners for a message created and deleted events and for message status updates.
     */
    public ChatStoreImplementation(ChatStoreData data, UIListener<ChatConversation> conversationListeners, UIListener<UIMessageItem> messageListeners) {
        this.conversationListener = conversationListeners;
        this.messageListener = messageListeners;
        this.data = data;
    }

    /**
     * Check if transaction was started with {@link ChatStore#beginTransaction()}
     */
    private void checkState() {
        if (transaction == null) {
            throw new RuntimeException("Transaction not started");
        }
    }

    /*

        Implementation of ChatStore interface which allows the SDK to internally manipulate the data which is accessed by the UI components of the app.

     */

    @Override
    public ChatConversationBase getConversation(String conversationId) {
        return data.getConversation(conversationId);
    }

    @Override
    public List<ChatConversationBase> getAllConversations() {
        return data.getAllConversations();
    }

    @Override
    public boolean upsert(ChatConversation conversation) {
        checkState();
        transaction.addUpsertConversation(conversation);
        return true;
    }

    @Override
    public boolean update(ChatConversationBase conversation) {
        checkState();
        transaction.addTaskUpdateConversation(conversation);
        return true;
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        checkState();
        transaction.addTaskDeleteConversation(conversationId);
        return true;
    }

    @Override
    public boolean upsert(ChatMessage message) {
        checkState();
        transaction.addTaskUpsertMessage(message);
        return true;
    }

    @Override
    public boolean update(ChatMessageStatus status) {
        checkState();
        transaction.addTaskUpdateStatus(status);
        return true;
    }

    @Override
    public boolean clearDatabase() {
        data.clearDatabase();
        return true;
    }

    @Override
    public boolean deleteAllMessages(String conversationId) {
        checkState();
        transaction.addTaskDeleteAllMessages(conversationId);
        return true;
    }

    @Override
    public boolean deleteMessage(String conversationId, String messageId) {
        checkState();
        transaction.addTaskDeleteMessage(conversationId, messageId);
        return true;
    }

    @Override
    public void beginTransaction() {
        this.transaction = new Transaction();
    }

    @Override
    public void endTransaction() {
        // Executes pending operations on underlying data.
        this.transaction.execute(data);
        // Informs conversation screen that the data may have changed.
        if (conversationListener != null) {
            conversationListener.setData(data.getConversationsUI());
        }
        // Informs message screen that the data may have changed.
        if (messageListener != null) {
            messageListener.setData(data.getSortedMessages(messageListener.getMetadata()));
        }
    }
}