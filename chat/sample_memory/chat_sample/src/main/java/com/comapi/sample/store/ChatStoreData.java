package com.comapi.sample.store;

import com.comapi.Session;
import com.comapi.chat.ComapiChatClient;
import com.comapi.chat.model.ChatConversation;
import com.comapi.chat.model.ChatConversationBase;
import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;
import com.comapi.sample.ui.model.UIMessageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Class to store all conversations and messages. There should be a single instance of this class and all the transactions should perform updates synchronously.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class ChatStoreData {

    /**
     * Profile id of an user that is currently authenticated in the SDK.
     */
    private String userProfileId;

    /**
     * Conversations by conversation id.
     */
    private final Map<String /*conversationId*/, ChatConversation> conversationData;

    /**
     * Messages by conversation id and message id. UIMessageItem is just a convenience wrapper around {@link ChatMessage} object taking care of UI specific String formating etc.
     */
    private final Map<String /*conversationId*/, Map<String /*messageId*/, UIMessageItem>> messageData;

    /**
     * Recommended constructor.
     */
    public ChatStoreData() {
        conversationData = new HashMap<>();
        messageData = new HashMap<>();
    }

    /**
     * Set user profileId after successful authentication. This value will be used to determine if the message being saved was sent from this account or received from someone else.
     *
     * @param client ComapiChatClient obtained from SDK initialisation
     */
    public void setProfileId(ComapiChatClient client) {
        // Get authentication session details from SDK client.
        Session session = client != null ? client.getSession() : null;
        // Set profile id registered with currently running session.
        this.userProfileId = session != null ? session.getProfileId() : null;
    }

    /**
     * Get saved conversation.
     *
     * @param conversationId conversation unique id.
     * @return Saved conversation.
     */
    public ChatConversationBase getConversation(String conversationId) {
        return conversationData.get(conversationId);
    }

    /**
     * Get all saved conversations.
     *
     * @return All saved conversations.
     */
    public List<ChatConversationBase> getAllConversations() {
        final List<ChatConversationBase> list = new ArrayList<>();
        list.addAll(conversationData.values());
        return list;
    }

    /**
     * Insert or update conversation.
     *
     * @param conversation Conversation to insert or update.
     */
    public void upsert(ChatConversation conversation) {
        conversationData.put(conversation.getConversationId(), conversation);
    }

    /**
     * Update conversation.
     *
     * @param conversation Conversation to update.
     */
    public void update(ChatConversationBase conversation) {

        ChatConversation c = conversationData.get(conversation.getConversationId());
        if (c != null && c != conversation) {
            // Create new builder for a new ChatConversation instance.
            // 1. Populate it with all the values from previously stored chat conversation with same conversation id.
            // 2. Update the conversation with all the data received as a parameter.
            // ChatConversationBase has a subset of fields of ChatConversation so only some of them will be updated here (relevant from the point of view of SDK).
            ChatConversation.Builder builder = ChatConversation.builder().populate(c).populate(conversation);
            conversationData.put(conversation.getConversationId(), builder.build());
        }
    }

    /**
     * Delete conversation from the persistence store.
     *
     * @param conversationId Conversation unique id.
     */
    public void deleteConversation(String conversationId) {
        // delete conversation from persistance store
        conversationData.remove(conversationId);
    }

    /**
     * Insert or update message.
     *
     * @param message Message to insert or update.
     */
    public void upsert(ChatMessage message) {

        // create map entry for a conversation if doesn't exist
        Map<String, UIMessageItem> perConversation = messageData.get(message.getConversationId());
        if (perConversation == null) {
            perConversation = new HashMap<>();
            messageData.put(message.getConversationId(), perConversation);
        }

        // create map entry for a message if doesn't exist
        UIMessageItem perMessage = perConversation.get(message.getMessageId());
        if (perMessage == null) {
            perMessage = new UIMessageItem();
            perConversation.put(message.getMessageId(), perMessage);
        }

        // Update app message data with received Comapi Chat message
        perMessage.setMessage(message, message.getFromWhom().getId().equals(userProfileId));
    }

    /**
     * Insert or update message delivery status.
     *
     * @param status Message status to insert or update.
     */
    public void upsert(ChatMessageStatus status) {

        // create map entry for a conversation if doesn't exist
        Map<String, UIMessageItem> perConversation = messageData.get(status.getConversationId());
        if (perConversation == null) {
            perConversation = new HashMap<>();
            messageData.put(status.getConversationId(), perConversation);
        }

        // ignore if message doesn't exist, the delivery status will come together with the message.
        UIMessageItem perMessage = perConversation.get(status.getMessageId());
        if (perMessage == null) {
            return;
        }

        // Update app message data with received Comapi Chat message status
        perMessage.updateStatus(status);
    }

    /**
     * Clear all content in persistance store.
     */
    public void clearDatabase() {
        conversationData.clear();
        messageData.clear();
    }

    /**
     * Delete all messages in a conversation.
     *
     * @param conversationId Conversation unique id.
     */
    public void deleteAllMessages(String conversationId) {
        messageData.remove(conversationId);
    }

    /**
     * Delete message.
     *
     * @param conversationId Conversation unique id.
     * @param messageId      Message unique id.
     */
    public void deleteMessage(String conversationId, String messageId) {
        final Map<String, UIMessageItem> map = messageData.get(conversationId);
        if (map != null) {
            map.remove(messageId);
        }
    }

    /**
     * Get list of conversations for ui.
     *
     * @return List of conversations for ui.
     */
    public List<ChatConversation> getConversationsUI() {
        return new ArrayList<>(conversationData.values());
    }

    /**
     * Get list of messages in conversation for ui. Messages will be sorted based on compareTo method implementation in UIMessageItem (according to sentEventId and sentOn values in Comapi ChatMessage)
     * The Class {@link ChatMessage} implements {@link ChatMessage#compareTo(ChatMessage)} method so sorted collection like TreeSet will order messages form oldest to the most recent.
     *
     * @return List of sorted messages for ui.
     */
    public TreeSet<UIMessageItem> getSortedMessages(String conversationId) {
        Map<String, UIMessageItem> msgList = messageData.get(conversationId);
        return new TreeSet<>(msgList != null ? msgList.values() : new TreeSet<UIMessageItem>());
    }
}