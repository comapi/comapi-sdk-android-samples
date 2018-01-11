package com.comapi.sample.comapi;

import android.util.Log;

import com.comapi.Callback;
import com.comapi.Session;
import com.comapi.chat.ComapiChatClient;
import com.comapi.chat.model.ChatConversation;
import com.comapi.sample.Const;
import com.comapi.sample.events.LoginEvent;
import com.comapi.sample.store.ChatStoreData;
import com.comapi.sample.store.ChatStoreImplementation;
import com.comapi.sample.store.UIListener;
import com.comapi.sample.ui.model.UIMessageItem;

import org.greenrobot.eventbus.EventBus;

/**
 * Access to local persistence store
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class MainController {

    private final ChatStoreData data;

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private final ServiceController controller;

    /**
     * Listener for conversations for UI to display
     */
    private UIListener<ChatConversation> conversationListener;

    /**
     * Listener for messages for UI to display
     */
    private UIListener<UIMessageItem> messageListener;

    /**
     * Recommended constructor
     */
    public MainController() {
        controller = new ServiceController(); // create controller for Comapi operations
        data = new ChatStoreData(); // Create in-memory data storage
    }

    /**
     * Wraps ChatStore class into single transaction controller.
     *
     * @return ChatStore interface implementing a single transaction.
     */
    public ChatStoreImplementation getNewStoreTransaction() {
        return new ChatStoreImplementation(data, conversationListener, messageListener);
    }

    /**
     * Gets controller for Comapi operations.
     */
    public ServiceController getComapiService() {
        return controller;
    }

    /**
     * Sets Comapi Chat SDK client. Populated when SDK finishes initialisation.
     */
    public void setClient(ComapiChatClient client) {
        this.controller.setClient(client); // Sets Comapi client to application Comapi controller.
        this.data.setProfileId(client);
    }

    private void updateProfileId() {
        this.data.setProfileId(controller.getClient());
    }

    /**
     * Get active user profile id.
     *
     * @return Active user profile id.
     */
    public String getUserProfileId() {
        if (controller.getClient() != null && controller.getClient().getSession().isSuccessfullyCreated()) {
            return controller.getClient().getSession().getProfileId();
        }
        return null;
    }

    /**
     * Sets listener for messages for UI to display.
     *
     * @param conversationId Unique conversation id.
     * @param listener       Listener for messages for UI to display
     */
    public void setMessageListener(String conversationId, UIListener<UIMessageItem> listener) {
        messageListener = listener;
        listener.setData(data.getSortedMessages(conversationId));
    }

    /**
     * Remove UI listener.
     */
    public void removeMessageListener() {
        messageListener = null;
    }

    /**
     * Sets listener for conversations for UI to display.
     *
     * @param listener Listener for conversations for UI to display
     */
    public void setConversationListener(UIListener<ChatConversation> listener) {
        conversationListener = listener;
        listener.setData(data.getConversationsUI());
    }

    /**
     * Remove UI listener.
     */
    public void removeConversationListener() {
        conversationListener = null;
    }

    public void startSession() {
       controller.getService().startSession(new Callback<Session>() {

           @Override
           public void success(Session result) {

               Log.i(Const.TAG, "Successfully started session");
               updateProfileId();

               // Post login finished event to EventBus
               EventBus.getDefault().postSticky(new LoginEvent(result.getProfileId(), result.isSuccessfullyCreated()));
           }

           @Override
           public void error(Throwable t) {

               Log.e(Const.TAG, "Error starting session: " + t.getLocalizedMessage());
               updateProfileId();

               Session session = controller.getClient().getSession();
               // Post login finished event to EventBus
               EventBus.getDefault().postSticky(new LoginEvent(session != null ? session.getProfileId() : null, session != null && session.isSuccessfullyCreated()));
           }
       });
    }
}