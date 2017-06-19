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

package com.comapi.sample.ui.model;

/**
 * Data holder for message in list view.
 *
 * @author Marcin Swierczek
 */
public class MessageItem {

    /**
     * Message body.
     */
    private String body;

    /**
     * Profile id of a message sender.
     */
    private String sender;

    /**
     * True if sent by the user registered locally.
     */
    private Boolean sentByMe;

    /**
     * Recommended constructor.
     *
     * @param body     Message body.
     * @param from     Profile id of a message sender.
     * @param sentByMe True if sent by the user registered locally.
     */
    public MessageItem(String body, String from, Boolean sentByMe) {
        this.body = body;
        this.sender = from;
        this.sentByMe = sentByMe;
    }

    /**
     * Gets Message body.
     *
     * @return Message body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Is sent by the user registered locally.
     *
     * @return True if sent by the user registered locally.
     */
    public Boolean getSentByMe() {
        return sentByMe;
    }

    /**
     * Gets Profile id of a message sender.
     *
     * @return Profile id of a message sender.
     */
    public String getSender() {
        return sender;
    }
}
