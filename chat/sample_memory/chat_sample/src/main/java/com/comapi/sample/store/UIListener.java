package com.comapi.sample.store;

import java.util.Collection;

/**
 * Interface for all listeners registered by the UI components to controller responsible for calling Comapi methods.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public interface UIListener<T> {

    /**
     * Data passed to UI when SDK and controller finish making changes to chat data.
     *
     * @param data Chat data e.g. list of conversation or messages
     */
    void setData(Collection<T> data);

    /**
     * Metadata associated with result obtained in this listener. This will be e.g. conversationId for the collection of messages.
     *
     * @return Metadata associated with result obtained in this listener.
     */
    String getMetadata();
}