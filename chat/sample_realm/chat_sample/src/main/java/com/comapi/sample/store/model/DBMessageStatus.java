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

import com.comapi.chat.model.ChatMessageStatus;
import com.comapi.chat.model.LocalMessageStatus;

import io.realm.RealmObject;

/**
 * Realm database object describing a chat message status.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class DBMessageStatus extends RealmObject {

    private String profileId;

    private String name;

    /**
     * Constructor
     */
    public DBMessageStatus() {
    }

    /**
     * Constructor
     *
     * @param status Comapi Chat SDK message status to save.
     */
    DBMessageStatus(ChatMessageStatus status) {
        profileId = status.getProfileId();
        name = status.getMessageStatus().name();
    }

    /**
     * Constructor
     *
     * @param profileId Profile id of the user that updated message status.
     * @param status    Comapi Chat SDK message status to save.
     */
    DBMessageStatus(String profileId, LocalMessageStatus status) {
        this.profileId = profileId;
        this.name = status.name();
    }

    /**
     * Get profile id of the user that updated message status.
     *
     * @return Profile id of the user that updated message status.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Get comapi Chat SDK message status.
     *
     * @return Comapi Chat SDK message status.
     */
    public String getStatus() {
        return name;
    }
}
