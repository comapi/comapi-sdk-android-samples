package com.comapi.sample.store;

import android.support.v4.util.Pair;

import com.comapi.chat.model.ChatConversation;
import com.comapi.chat.model.ChatConversationBase;
import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;

import java.util.LinkedList;

/**
 * This class implements a single persistance store transaction. The SDK internally performs db updates in transactions so the sets of changes are applied together to keep consistency.
 * After creating the instance you can queue db updates in form of Tasks. Calling {@link this#execute(ChatStoreData)} will execute the queue of tasks on provided data.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class Transaction extends LinkedList<Transaction.Task> {

    /**
     * Single operation on a database.
     *
     * @param <T> Data object to insert/update/delete.
     */
    abstract class Task<T> {

        /**
         * Data object to insert/update/delete when the transaction task will be executed.
         */
        T object;

        /**
         * Recommended constructor.
         *
         * @param object Data object to insert/update/delete when the transaction task will be executed.
         */
        Task(T object) {
            this.object = object;
        }

        /**
         * execute this task modifying stored chat data.
         *
         * @param store Chat data on which the operation should be executed.
         */
        abstract public void execute(ChatStoreData store);
    }

    /**
     * Transaction task to insert or update conversation.
     */
    private class TaskUpsertConversation extends Task<ChatConversation> {

        TaskUpsertConversation(ChatConversation object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.upsert(object);
        }
    }

    /**
     * Transaction task to insert or update message.
     */
    private class TaskUpsertMessage extends Task<ChatMessage> {

        TaskUpsertMessage(ChatMessage object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.upsert(object);
        }
    }

    /**
     * Transaction task to update a message with a status.
     */
    private class TaskUpdateStatus extends Task<ChatMessageStatus> {

        TaskUpdateStatus(ChatMessageStatus object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.upsert(object);
        }
    }

    /**
     * Transaction task to delete conversation.
     */
    private class TaskDeleteConversation extends Task<String> {

        TaskDeleteConversation(String object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.deleteConversation(object);
            store.deleteAllMessages(object);
        }
    }

    /**
     * Transaction task to delete a message.
     */
    private class TaskDeleteMessage extends Task<Pair<String, String>> {

        TaskDeleteMessage(Pair<String, String> object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.deleteMessage(object.first, object.second);
        }
    }

    /**
     * Transaction task to insert or update conversation.
     */
    private class TaskDeleteAllMessages extends Task<String> {

        TaskDeleteAllMessages(String object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.deleteAllMessages(object);
        }
    }

    /**
     * Transaction task to insert or update conversation.
     */
    private class TaskUpdateConversation extends Task<ChatConversationBase> {

        TaskUpdateConversation(ChatConversationBase object) {
            super(object);
        }

        @Override
        public void execute(ChatStoreData store) {
            store.update(object);
        }
    }

    /**
     * Add task to update a message status.
     *
     * @param status New status of a chat message.
     */
    void addTaskUpdateStatus(ChatMessageStatus status) {
        add(new TaskUpdateStatus(status));
    }

    /**
     * Add task to insert or update a conversation.
     *
     * @param conversation New conversation data.
     */
    void addUpsertConversation(ChatConversation conversation) {
        add(new TaskUpsertConversation(conversation));
    }

    /**
     * Add task to insert or update a message.
     *
     * @param message New message data.
     */
    void addTaskUpsertMessage(ChatMessage message) {
        add(new TaskUpsertMessage(message));
    }

    /**
     * Add task to delete a conversation.
     *
     * @param conversationId Unique conversation id.
     */
    void addTaskDeleteConversation(String conversationId) {
        add(new TaskDeleteConversation(conversationId));
    }

    /**
     * Add task to delete a message.
     *
     * @param conversationId Unique conversation id.
     * @param messageId      Unique messageId.
     */
    void addTaskDeleteMessage(String conversationId, String messageId) {
        add(new TaskDeleteMessage(new Pair<>(conversationId, messageId)));
    }

    /**
     * Add task to delete all messages in a conversation.
     *
     * @param conversationId Conversation unique id.
     */
    void addTaskDeleteAllMessages(String conversationId) {
        add(new TaskDeleteAllMessages(conversationId));
    }

    /**
     * Add task to update conversation data.
     *
     * @param conversation New conversation data.
     */
    void addTaskUpdateConversation(ChatConversationBase conversation) {
        add(new TaskUpdateConversation(conversation));
    }

    /**
     * Execute pending tasks one by one on chat store data.
     *
     * @param store Class encapsulating the chat data.
     */
    void execute(ChatStoreData store) {
        while (!isEmpty()) {
            Task toDo = this.poll();
            if (toDo != null) {
                toDo.execute(store);
            }
        }
    }
}
