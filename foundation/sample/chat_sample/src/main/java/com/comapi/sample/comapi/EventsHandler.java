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

import com.comapi.MessagingListener;
import com.comapi.internal.network.model.events.conversation.ParticipantAddedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantRemovedEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageSentEvent;

/**
 * Handler for socket events.
 *
 * @author Marcin Swierczek
 */
public class EventsHandler extends MessagingListener {

    /**
     * Wraps all {@link com.comapi.ComapiClient} methods used by the sample app.
     */
    private ComapiController controller;

    /**
     * Recommended constructor.
     *
     * @param controller Comapi APIs controller.
     */
    public EventsHandler(ComapiController controller) {
        this.controller = controller;
    }

    @Override
    public void onParticipantAdded(ParticipantAddedEvent event) {

        /*
         * Participant was added to a conversation.
         */

        // Notify listeners
        controller.notifyParticipantAdded(event.getConversationId(), event.getProfileId());
        // Get conversation details if the conversation was just created.
        controller.getConversation(event.getConversationId());
    }

    @Override
    public void onParticipantRemoved(ParticipantRemovedEvent event) {

        /*
         * Participant was removed from a conversation.
         */

        // Notify listeners
        controller.notifyParticipantRemoved(event.getConversationId(), event.getProfileId());
    }

    @Override
    public void onMessage(MessageSentEvent event) {

        /*
         New message received.
         */

        // Notify listeners.
        controller.notifyMessageAdded(event.getContext().getConversationId(), event.getConversationEventId(), event.getContext().getFromWhom().getId(), event.getContext().getFromWhom().getId().equals(controller.getProfileId()), event.getParts().get(0).getData());
    }
}