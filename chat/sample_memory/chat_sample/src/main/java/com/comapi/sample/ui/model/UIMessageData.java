package com.comapi.sample.ui.model;

import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Holds message data displayed in the messages ListView row.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class UIMessageData {

    /**
     * Message sender
     */
    private String sender;

    /**
     * Message body
     */
    private String body;

    /**
     * Message sent time
     */
    private String time;

    /**
     * Message description
     */
    private String statusDescription;

    /**
     * True if the message is sent by the user registered in this app instance
     */
    private boolean isMyMessage;

    /**
     * Sets the ListView row data based on a message saved in ChatStoreData
     *
     * @param message     Message saved in ChatStoreData
     * @param isMyMessage True if the message is sent by the user registered in this app instance
     */
    public void setMessage(ChatMessage message, boolean isMyMessage) {
        sender = message.getFromWhom().getId();
        body = message.getParts().get(0).getData();
        time = getDateString(message.getSentOn());
        this.isMyMessage = isMyMessage;
        buildStatusDescription(message);
    }

    /**
     * Build a string from a collection of status updates.
     *
     * @param message Message saved in ChatStoreData
     */
    void buildStatusDescription(ChatMessage message) {

        StringBuilder sb = new StringBuilder();
        for (ChatMessageStatus s : message.getStatusUpdates()) {
            sb.append(s.getProfileId());
            sb.append(" : ");
            sb.append(s.getMessageStatus());
            sb.append("; ");
        }
        statusDescription = sb.substring(0, Math.max(sb.length() - 2, 0));
    }

    /**
     * Get message sender profile id.
     *
     * @return message sender profile id.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get message body.
     *
     * @return message body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get time when message was sent.
     *
     * @return time when message was sent.
     */
    public String getTime() {
        return time;
    }

    /**
     * Was message sent by the user registered in this app instance
     *
     * @return True if the message is sent by the user registered in this app instance
     */
    public boolean isMyMessage() {
        return isMyMessage;
    }

    /**
     * Get message statuses for the UI
     *
     * @return message statuses for the UI
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * Creates a date string in format "EEE, d MMM yyyy HH:mm:ss".
     *
     * @param time Unix UTC time.
     * @return date string for the UI
     */
    private String getDateString(Long time) {

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        sdf.setTimeZone(tz);
        return sdf.format(time);
    }
}