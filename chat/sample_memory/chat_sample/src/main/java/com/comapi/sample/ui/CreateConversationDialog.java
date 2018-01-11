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

package com.comapi.sample.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.comapi.sample.R;

/**
 * Dialog to create a new conversation.
 *
 * @author Marcin Swierczek
 */
public class CreateConversationDialog extends DialogFragment {

    /**
     * Edit text to provide a name for a conversation to create.
     */
    private EditText nameEditText;

    /**
     * Interface to tell the Activity to create a new conversation.
     */
    interface CreateConversation {
        /**
         * Create conversation with a given name and random id.
         *
         * @param name Conversation name.
         */
        void createConversation(String name);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets styling for the dialog.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        } else {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_conversation_dialog, container, false);

        // Edit text to type in a name for the new conversation.
        nameEditText = (EditText) view.findViewById(R.id.name_edit_text);

        // Setup create conversation button.
        Button createButton = (Button) view.findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                String name = nameEditText.getText().toString();
                if (activity != null && activity instanceof CreateConversation && !TextUtils.isEmpty(name)) {
                    // Tell the calling Activity to create a new conversation with a given name and random id.
                    ((CreateConversation) activity).createConversation(name);
                    getDialog().dismiss();
                }
            }
        });

        // Setup cancel button
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }
}