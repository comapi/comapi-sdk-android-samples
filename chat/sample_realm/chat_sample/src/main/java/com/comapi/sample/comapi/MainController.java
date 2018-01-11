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

import com.comapi.Callback;
import com.comapi.Session;
import com.comapi.chat.ComapiChatClient;
import com.comapi.sample.events.LoginEvent;
import com.comapi.sample.store.ChatStoreImplementation;

import org.greenrobot.eventbus.EventBus;

/**
 * Access to local persistence store and Comapi SDK interfaces.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class MainController {

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private final ComapiController controller;

    /**
     * Recommended constructor
     */
    public MainController() {
        controller = new ComapiController(); // create controller for Comapi operations
    }

    /**
     * Chat store implementation that support transactions.
     *
     * @return ChatStore interface implementing a single transaction.
     */
    public ChatStoreImplementation getNewStoreTransaction() {
        return new ChatStoreImplementation(getUserProfileId());
    }

    /**
     * Gets controller for Comapi operations.
     */
    public ComapiController getComapiController() {
        return controller;
    }

    /**
     * Sets Comapi Chat SDK client. Populated when SDK finishes initialisation.
     */
    public void setComapiClient(ComapiChatClient client) {
        this.controller.setClient(client);
    }

    /**
     * Get active user profile id.
     *
     * @return Active user profile id.
     */
    public String getUserProfileId() {
        if (controller.getClient() != null) {
            final Session session = controller.getClient().getSession();
            if (session != null && session.isSuccessfullyCreated()) {
                return controller.getClient().getSession().getProfileId();
            }
        }
        return null;
    }

    /**
     * Start the authenticated session for the user with profile id encoded in token taken from {@link AuthChallengeHandler}. Pass the login details to the UI components.
     */
    public void startSession() {
       controller.getService().startSession(new Callback<Session>() {

           @Override
           public void success(Session result) {
               // Post login finished event to EventBus
               EventBus.getDefault().postSticky(new LoginEvent(result.getProfileId(), result.isSuccessfullyCreated()));
           }

           @Override
           public void error(Throwable t) {
               final Session session = controller.getClient().getSession();
               // Post login finished event to EventBus
               EventBus.getDefault().postSticky(new LoginEvent(session != null ? session.getProfileId() : null, session != null && session.isSuccessfullyCreated()));
           }
       });
    }
}