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

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.comapi.sample.R;
import com.comapi.sample.store.model.DBConversation;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Data adapter class creating views for conversation list.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public final class ConversationAdapter extends RealmBaseAdapter<DBConversation> implements ListAdapter {

    public ConversationAdapter(@Nullable OrderedRealmCollection<DBConversation> data) {
        super(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // References to this row views
        ConversationViewHolder viewHolder;

        // Inflate layout only once
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list_row, parent, false);
            viewHolder = new ConversationViewHolder();
            // Store references to views in this list row
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name);
        } else {
            // Get references to views in this list row
            viewHolder = (ConversationViewHolder) convertView.getTag();
        }

        // Conversation data to populate row view in the list
        DBConversation conversation = getItem(position);
        if (conversation != null) {
            viewHolder.nameTextView.setText(conversation.getName());
        }
        convertView.setTag(viewHolder);

        return convertView;
    }
}