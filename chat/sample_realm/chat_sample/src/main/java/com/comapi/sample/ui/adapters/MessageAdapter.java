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

package com.comapi.sample.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.comapi.sample.R;
import com.comapi.sample.store.model.DBChatMessage;
import com.comapi.sample.store.model.DBMessageStatus;
import com.comapi.sample.ui.holders.MessageViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Data adapter class creating views for conversation list.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public final class MessageAdapter extends RealmBaseAdapter<DBChatMessage> implements ListAdapter {

    public MessageAdapter(@Nullable OrderedRealmCollection<DBChatMessage> data) {
        super(data);
    }

    @Override
    public int getViewTypeCount() {
        // There are two types of rows in message list view - received and sent messages
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // Return one of two list view item types - received or sent messages.
        DBChatMessage item = getItem(position);
        return (item != null && item.isMyOwn()) ? 0 : 1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Message data to populate row view in the list
        DBChatMessage item = getItem(position);

        // References to this row views
        MessageViewHolder viewHolder;

        // Inflate layout only once
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            // Different list row layout for sent and received messages
            if (item != null && item.isMyOwn()) {
                convertView = inflater.inflate(R.layout.bubble_right, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.bubble_left, parent, false);
            }
            // Store references to views in this list row
            viewHolder = new MessageViewHolder();
            viewHolder.body = (TextView) convertView.findViewById(R.id.body);
            viewHolder.sender = (TextView) convertView.findViewById(R.id.sender);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(viewHolder);

        } else {
            // Get references to views in this list row
            viewHolder = (MessageViewHolder) convertView.getTag();
        }

        if (item != null) {
            // Sets message body into chat bubble
            viewHolder.body.setText(item.getBody());


            // Sets sender pro
            if (viewHolder.sender != null) {
                viewHolder.sender.setText(item.getSender());
            }

            if (viewHolder.status != null) {
                viewHolder.status.setText(buildStatusDescription(item));
            }

            if (viewHolder.time != null) {
                viewHolder.time.setText(buildDateString(item.getTimestamp()));
            }
        }

        return convertView;
    }

    /**
     * Build a string from a collection of status updates.
     *
     * @param message Message saved in ChatStoreData
     */
    private String buildStatusDescription(DBChatMessage message) {

        if (message.getStatusUpdates() != null) {
            StringBuilder sb = new StringBuilder();
            for (DBMessageStatus s : message.getStatusUpdates()) {
                sb.append(s.getProfileId());
                sb.append(" : ");
                sb.append(s.getStatus());
                sb.append("; ");
            }
            return sb.substring(0, Math.max(sb.length() - 2, 0));
        } else {
            return "unknown status";
        }
    }

    /**
     * Creates a date string in format "EEE, d MMM yyyy HH:mm:ss".
     *
     * @param time Unix UTC time.
     * @return date string for the UI
     */
    private String buildDateString(Long time) {

        if (time != null) {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(tz);
            return sdf.format(time);
        } else {
            return "unknown time";
        }
    }
}